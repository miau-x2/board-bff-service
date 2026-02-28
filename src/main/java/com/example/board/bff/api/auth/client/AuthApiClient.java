package com.example.board.bff.api.auth.client;

import com.example.board.bff.api.auth.client.config.AuthFeignConfig;
import com.example.board.bff.api.auth.client.response.AvailabilityResponse;
import com.example.board.bff.api.auth.client.response.SignupEmailSendResponse;
import com.example.board.bff.api.auth.client.response.SignupEmailVerifyResponse;
import com.example.board.bff.api.auth.controller.dto.EmailSendRequest;
import com.example.board.bff.api.auth.controller.dto.EmailVerifyRequest;
import com.example.board.bff.api.auth.controller.dto.MemberSignupRequest;
import com.example.board.bff.commons.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth-service", contextId = "authApiClient", configuration = AuthFeignConfig.class)
public interface AuthApiClient {
    @PostMapping("/auth/signup")
    ApiResponse<Void> signUp(@RequestHeader("X-Signup-Token") String token, @RequestBody MemberSignupRequest request);
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
}
