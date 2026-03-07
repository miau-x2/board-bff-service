package com.example.board.bff.token;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Slf4j
@Component
public class ReissueSingleFlight {
    private final ConcurrentMap<String, CompletableFuture<GetAccessTokenResult>> inFlight = new ConcurrentHashMap<>();

    public GetAccessTokenResult run(String key, Supplier<GetAccessTokenResult> supplier) {
        while (true) {
            var inProgress = inFlight.get(key);
            // 현재 토큰 재발급을 진행중인 future가 존재하는 경우 완료될 때까지 대기
            if(inProgress != null) {
                return inProgress.join();
            }
            // 현재 토큰 재발급을 진행중인 future가 없는 경우 재발급 요청
            var tokenReissueFuture = new CompletableFuture<GetAccessTokenResult>();
            var raced = inFlight.putIfAbsent(key, tokenReissueFuture);
            if(raced == null) {
                try{
                    var tokenReissueResult = supplier.get();
                    tokenReissueFuture.complete(tokenReissueResult);
                    return tokenReissueResult;
                } catch (Exception e) {
                    log.warn("액세스 토큰 재발급 작업 실패", e);
                    var fallback = new GetAccessTokenResult.SystemError();
                    tokenReissueFuture.complete(fallback);
                    return fallback;
                } finally {
                    inFlight.remove(key, tokenReissueFuture);
                }
            }
        }
    }
}
