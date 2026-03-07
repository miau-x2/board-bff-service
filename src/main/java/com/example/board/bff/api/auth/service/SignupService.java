package com.example.board.bff.api.auth.service;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.api.auth.exception.AuthSignupErrorCode;
import com.example.board.bff.controller.dto.request.SignupRequest;
import com.example.board.bff.api.auth.service.result.SignupResult;
import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.commons.response.ApiResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SignupService {
    private static final String TOKEN_EXPIRED_MESSAGE = "이메일 인증이 만료되었습니다. 다시 인증해주세요.";
    private static final String TOKEN_INVALID_MESSAGE = "이메일 인증이 유효하지 않습니다. 다시 인증해주세요.";
    private static final String EMAIL_DOMAIN_NOT_ALLOWED_MESSAGE = "지메일과 네이버메일만 사용할 수 있습니다.";
    private static final String USERNAME_DUPLICATED_MESSAGE = "이미 사용 중인 아이디입니다.";
    private static final String EMAIL_DUPLICATED_MESSAGE = "이미 사용 중인 이메일입니다.";
    private static final String NICKNAME_DUPLICATED_MESSAGE = "이미 사용 중인 닉네임입니다.";
    private static final String SYSTEM_ERROR_MESSAGE = "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.";
    private final AuthApiClient authApiClient;
    private final FeignExceptions feignExceptions;

    public SignupResult signup(String token, SignupRequest request) {
        try {
            authApiClient.signUp(token, request);
            return new SignupResult.Success();
        } catch (FeignException e) {
            return feignExceptions.extractErrorResponse(e)
                    .map(this::handleDownstreamError)
                    .orElse(new SignupResult.SystemError(SYSTEM_ERROR_MESSAGE));
        }
    }

    private SignupResult handleDownstreamError(ApiResponse<Void> response) {
        var code = AuthSignupErrorCode.from(response.code());
        if(code == null) {
            return new SignupResult.SystemError(SYSTEM_ERROR_MESSAGE);
        }

        return switch (code) {
            case TOKEN_EXPIRED -> new SignupResult.TokenExpired(TOKEN_EXPIRED_MESSAGE);
            case TOKEN_INVALID -> new SignupResult.TokenInvalid(TOKEN_INVALID_MESSAGE);
            case EMAIL_DOMAIN_NOT_ALLOWED -> new SignupResult.EmailDomainNotAllowed(EMAIL_DOMAIN_NOT_ALLOWED_MESSAGE);
            case USERNAME_DUPLICATED -> new SignupResult.UsernameDuplicated(USERNAME_DUPLICATED_MESSAGE);
            case EMAIL_DUPLICATED -> new SignupResult.EmailDuplicated(EMAIL_DUPLICATED_MESSAGE);
            case NICKNAME_DUPLICATED -> new SignupResult.NicknameDuplicated(NICKNAME_DUPLICATED_MESSAGE);
        };
    }
}
