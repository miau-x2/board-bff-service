package com.example.board.bff.token;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.controller.dto.request.ReissueRequest;
import com.example.board.bff.api.auth.exception.AuthReissueErrorCode;
import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.config.retry.RetryableReissueException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class TokenService {
    private static final Duration ACCESS_TOKEN_EXPIRY_BUFFER = Duration.ofSeconds(30);
    private final AuthApiClient authApiClient;
    private final FeignExceptions feignExceptions;
    private final ReissueSingleFlight singleFlight;
    private final RetryTemplate retryTemplate;

    public TokenService(
            AuthApiClient authApiClient,
            FeignExceptions feignExceptions,
            ReissueSingleFlight singleFlight,
            @Qualifier("authApiRetryTemplate")
            RetryTemplate retryTemplate) {
        this.authApiClient = authApiClient;
        this.feignExceptions = feignExceptions;
        this.singleFlight = singleFlight;
        this.retryTemplate = retryTemplate;
    }

    public GetAccessTokenResult getValidAccessToken(String sessionId, SessionRecord record) {
        if (record == null
                || record.memberId() == null
                || record.tokenType() == null
                || record.sessionExpiresAt() == null) {
            return new GetAccessTokenResult.SessionInvalid();
        }
        if (Instant.now().isAfter(record.sessionExpiresAt())) {
            return new GetAccessTokenResult.SessionExpired();
        }
        if (record.refreshToken() == null || record.refreshToken().isBlank() || record.refreshTokenExpiresAt() == null) {
            return new GetAccessTokenResult.SessionInvalid();
        }
        if (Instant.now().isAfter(record.refreshTokenExpiresAt())) {
            return new GetAccessTokenResult.RefreshTokenExpired();
        }

        // 액세스 토큰이 유효한 경우
        if (record.accessToken() != null
                && !record.accessToken().isBlank()
                && record.accessTokenExpiresAt() != null
                && Instant.now().plus(ACCESS_TOKEN_EXPIRY_BUFFER).isBefore(record.accessTokenExpiresAt())) {
            return new GetAccessTokenResult.Success(new TokenRecord(
                    record.accessToken(),
                    record.accessTokenExpiresAt(),
                    record.refreshToken(),
                    record.refreshTokenExpiresAt(),
                    record.tokenType()
            ));
        }

        // 액세스 토큰이 만료 되었거나 유효하지 않은 경우 재발급
        return singleFlight.run(sessionId, () -> reissueWithRetry(record.memberId(), record.refreshToken()));
    }

    private GetAccessTokenResult reissueWithRetry(Long memberId, String refreshToken) {
        try {
            return retryTemplate.execute(() -> {
                var result = callReissueApi(memberId, refreshToken);
                if(result instanceof GetAccessTokenResult.ReissueRetryable) {
                    throw new RetryableReissueException("재시도 가능한 액세스 토큰 재발급 예외");
                }
                return result;
            });
        } catch (RetryException e) {
            log.warn("액세스 토큰 재발급 재시도 실패: {}", e.getMessage(), e);
            return new GetAccessTokenResult.SystemError();
        }
    }

    private GetAccessTokenResult callReissueApi(Long memberId, String refreshToken) {
        try {
            var downstreamResponse = authApiClient.reissue(new ReissueRequest(memberId, refreshToken)).data();
            log.info("액세스 토큰 재발급 완료");
            return new GetAccessTokenResult.Success(new TokenRecord(
                    downstreamResponse.accessToken(),
                    downstreamResponse.accessTokenExpiresAt(),
                    downstreamResponse.refreshToken(),
                    downstreamResponse.refreshTokenExpiresAt(),
                    downstreamResponse.tokenType())
            );
        } catch (FeignException e) {
            return feignExceptions.extractErrorResponse(e)
                    .map(response -> {
                        var code = AuthReissueErrorCode.from(response.code());
                        if (code == null) {
                            log.warn("액세스 토큰 재발급 실패");
                            return new GetAccessTokenResult.SystemError();
                        }
                        return switch (code) {
                            case REFRESH_TOKEN_INVALID -> {
                                log.warn("액세스 토큰 재발급 실패. 유효하지 않은 리프레시 토큰");
                                yield new GetAccessTokenResult.ReissueFailed();
                            }
                            case TOKEN_REISSUE_TEMPORARILY_UNAVAILABLE -> {
                                log.warn("재시도 가능한 액세스 토큰 재발급 실패");
                                yield new GetAccessTokenResult.ReissueRetryable();
                            }
                        };
                    })
                    .orElse(new GetAccessTokenResult.SystemError());
        }
    }
}