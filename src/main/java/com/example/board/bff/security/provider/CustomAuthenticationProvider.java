package com.example.board.bff.security.provider;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.api.auth.exception.AuthLoginErrorCode;
import com.example.board.bff.controller.dto.request.LoginRequest;
import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.security.authentication.CustomAuthenticationToken;
import com.example.board.bff.security.authentication.TokenPair;
import com.example.board.bff.security.exception.AccountDormantException;
import com.example.board.bff.commons.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private static final String CREDENTIALS_EMPTY_ERROR_MESSAGE = "아이디 또는 비밀번호를 입력해주세요.";
    private static final String DEFAULT_LOGIN_ERROR_MESSAGE = "아이디 또는 비밀번호가 일치하지 않습니다.";
    private static final String ACCOUNT_DORMANT_ERROR_MESSAGE = "휴면 계정입니다.";
    private static final String DEFAULT_SYSTEM_ERROR_MESSAGE = "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.";
    private static final String ROLE_PREFIX = "ROLE_";
    private final AuthApiClient authApiClient;
    private final FeignExceptions feignExceptions;

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if(!(authentication.getPrincipal() instanceof String username) || username.isBlank()) {
            throw new BadCredentialsException(CREDENTIALS_EMPTY_ERROR_MESSAGE);
        }
        if(!(authentication.getCredentials() instanceof String password) || password.isBlank()) {
            throw new BadCredentialsException(CREDENTIALS_EMPTY_ERROR_MESSAGE);
        }
        try {
            var downstreamResponse = authApiClient.login(new LoginRequest((String) authentication.getPrincipal(), (String) authentication.getCredentials())).data();
            return CustomAuthenticationToken.authenticated(
                    downstreamResponse.memberId(),
                    null,
                    List.of(new SimpleGrantedAuthority(ROLE_PREFIX + downstreamResponse.role())),
                    new TokenPair(downstreamResponse.accessToken(),
                            downstreamResponse.accessTokenExpiresAt(),
                            downstreamResponse.refreshToken(),
                            downstreamResponse.refreshTokenExpiresAt(),
                            downstreamResponse.tokenType())
            );
        } catch (FeignException e) {
            throw feignExceptions.extractErrorResponse(e)
                    .map(this::handleDownstreamError)
                    .orElseThrow(() -> new AuthenticationServiceException(DEFAULT_SYSTEM_ERROR_MESSAGE));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private AuthenticationException handleDownstreamError(ApiResponse<Void> response) {
        var code = AuthLoginErrorCode.from(response.code());
        if(code == null) {
            return new AuthenticationServiceException(DEFAULT_SYSTEM_ERROR_MESSAGE);
        }
        return switch (code) {
            case BAD_CREDENTIALS, ACCOUNT_PENDING, ACCOUNT_WITHDRAWN -> new BadCredentialsException(DEFAULT_LOGIN_ERROR_MESSAGE);
            case ACCOUNT_DORMANT -> new AccountDormantException(ACCOUNT_DORMANT_ERROR_MESSAGE);
        };
    }
}
