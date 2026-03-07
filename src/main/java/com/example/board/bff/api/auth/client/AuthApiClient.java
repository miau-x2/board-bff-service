package com.example.board.bff.api.auth.client;

import com.example.board.bff.commons.response.ApiResponse;
import com.example.board.bff.controller.dto.request.*;
import com.example.board.bff.controller.dto.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service", contextId = "authApiClient")
public interface AuthApiClient {
    @PostMapping("/auth/signup")
    ApiResponse<Void> signUp(@RequestHeader("X-Signup-Token") String token, @RequestBody SignupRequest request);

    @GetMapping("/auth/members/check-username")
    ApiResponse<AvailabilityResponse> checkUsernameAvailability(@RequestParam("username") String username);

    @GetMapping("/auth/members/check-email")
    ApiResponse<AvailabilityResponse> checkEmailAvailability(@RequestParam("email") String email);

    @GetMapping("/auth/members/check-nickname")
    ApiResponse<AvailabilityResponse> checkNicknameAvailability(@RequestParam("nickname") String nickname);

    @PostMapping("/auth/signup/email-verification")
    ApiResponse<SignupEmailSendResponse> sendOtp(@RequestBody EmailSendRequest request);

    @PostMapping("/auth/signup/email-verification/verify")
    ApiResponse<SignupEmailVerifyResponse> verifyOtp(@RequestBody EmailVerifyRequest request);

    @PostMapping("/auth/login")
    ApiResponse<LoginResponse> login(@RequestBody LoginRequest request);

    @PostMapping("/auth/token/reissue")
    ApiResponse<ReissueResponse> reissue(@RequestBody ReissueRequest request);
}
