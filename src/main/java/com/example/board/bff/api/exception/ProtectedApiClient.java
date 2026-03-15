package com.example.board.bff.api.exception;

import com.example.board.bff.token.GetAuthorizationResult;
import com.example.board.bff.token.TokenFacade;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ProtectedApiClient {
    private final TokenFacade tokenFacade;

    public <T> T execute(
            String sessionId,
            Function<String, T> downstreamCall,
            Function<FeignException, T> non401ErrorHandler,
            Supplier<T> unauthorizedResult,
            Supplier<T> systemErrorResult) {
        var authResult = tokenFacade.getAuthorization(sessionId);
        return switch (authResult) {
            case GetAuthorizationResult.Success(var authorization) -> internalExecute(
                    sessionId,
                    authorization,
                    downstreamCall,
                    non401ErrorHandler,
                    unauthorizedResult,
                    systemErrorResult
            );
            case GetAuthorizationResult.Unauthorized _ -> unauthorizedResult.get();
            case GetAuthorizationResult.SystemError _ -> systemErrorResult.get();
        };
    }

    private <T> T internalExecute(
            String sessionId,
            String authorization,
            Function<String, T> downstreamCall,
            Function<FeignException, T> non401ErrorHandler,
            Supplier<T> unauthorizedResult,
            Supplier<T> systemErrorResult) {
        try {
            return downstreamCall.apply(authorization);
        } catch (UnauthorizedTokenException _) {
            var reissueResult = tokenFacade.reissueAuthorization(sessionId);
            return switch (reissueResult) {
                case GetAuthorizationResult.Success(var refreshed) -> {
                    try {
                        yield downstreamCall.apply(refreshed);
                    } catch (UnauthorizedTokenException _) {
                        yield unauthorizedResult.get();
                    } catch (FeignException e) {
                        yield non401ErrorHandler.apply(e);
                    }
                }
                case GetAuthorizationResult.Unauthorized _ -> unauthorizedResult.get();
                case GetAuthorizationResult.SystemError _ -> systemErrorResult.get();
            };
        } catch (FeignException non401) {
            return non401ErrorHandler.apply(non401);
        }
    }
}
