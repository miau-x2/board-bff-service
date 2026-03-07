package com.example.board.bff.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenFacade {
    private final SessionTokenContextRepository tokenContextRepository;
    private final TokenService tokenService;

    public GetAuthorizationResult getAuthorization(String sessionId) {
        var sessionRecord = tokenContextRepository.findById(sessionId);
        if(sessionRecord.isEmpty()) {
            return new GetAuthorizationResult.Unauthorized();
        }
        var accessTokenResult = tokenService.getValidAccessToken(sessionId, sessionRecord.get());
        return switch (accessTokenResult) {
            case GetAccessTokenResult.Success(var tokenRecord) -> {
                var isSave = tokenContextRepository.save(sessionId, tokenRecord);
                if(isSave) {
                    yield new GetAuthorizationResult.Success(tokenRecord.tokenType() + " " + tokenRecord.accessToken());
                }
                yield new GetAuthorizationResult.Unauthorized();
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
