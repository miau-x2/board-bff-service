package com.example.board.bff.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class TokenFacade {
    private final SessionTokenContextRepository tokenContextRepository;
    private final TokenService tokenService;

    public GetAuthorizationResult getAuthorization(String sessionId) {
        return resolveAuthorization(sessionId, tokenService::getValidAccessToken);
    }

    public GetAuthorizationResult reissueAuthorization(String sessionId) {
        return resolveAuthorization(sessionId, tokenService::reissueAccessToken);
    }

    private GetAuthorizationResult resolveAuthorization(
            String sessionId,
            BiFunction<String, SessionRecord, GetAccessTokenResult> tokenResolver) {
        var sessionRecord = tokenContextRepository.findById(sessionId);
        if (sessionRecord.isEmpty()) {
            return new GetAuthorizationResult.Unauthorized();
        }

        var accessTokenResult = tokenResolver.apply(sessionId, sessionRecord.get());
        return mapAuthorizationResult(sessionId, accessTokenResult);
    }

    private GetAuthorizationResult mapAuthorizationResult(String sessionId, GetAccessTokenResult result) {
        return switch (result) {
            case GetAccessTokenResult.Success(var tokenRecord) -> {
                var saveResult = tokenContextRepository.save(sessionId, tokenRecord);
                yield switch (saveResult) {
                    case SaveAccessTokenResult.Success _ ->
                            new GetAuthorizationResult.Success(tokenRecord.tokenType() + " " + tokenRecord.accessToken());
                    case SaveAccessTokenResult.SessionInvalid _, SaveAccessTokenResult.SessionExpired _ ->
                            new GetAuthorizationResult.Unauthorized();
                    case SaveAccessTokenResult.SystemError _ ->
                            new GetAuthorizationResult.SystemError();
                };
            }
            case GetAccessTokenResult.SessionInvalid _,
                 GetAccessTokenResult.SessionExpired _,
                 GetAccessTokenResult.RefreshTokenExpired _,
                 GetAccessTokenResult.ReissueFailed _ -> new GetAuthorizationResult.Unauthorized();
            case GetAccessTokenResult.ReissueRetryable _,
                 GetAccessTokenResult.SystemError _ -> new GetAuthorizationResult.SystemError();
        };
    }
}
