package com.example.board.bff.api.auth.service;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.controller.dto.request.LogoutRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {
    private final AuthApiClient authApiClient;

    public void logout(LogoutRequest request) {
        authApiClient.logout(request);
    }
}
