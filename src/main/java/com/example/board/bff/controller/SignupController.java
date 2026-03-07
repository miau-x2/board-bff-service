package com.example.board.bff.controller;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.controller.dto.response.AvailabilityResponse;
import com.example.board.bff.controller.dto.response.SignupEmailSendResponse;
import com.example.board.bff.controller.dto.response.SignupEmailVerifyResponse;
import com.example.board.bff.controller.dto.request.EmailSendRequest;
import com.example.board.bff.controller.dto.request.EmailVerifyRequest;
import com.example.board.bff.controller.dto.request.SignupRequest;
import com.example.board.bff.controller.dto.validation.SignupValidationSequence;
import com.example.board.bff.api.auth.service.SignupService;
import com.example.board.bff.api.auth.service.result.SignupResult;
import com.example.board.bff.commons.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.example.board.bff.api.utils.ErrorUtils.addFieldErrorReturnView;
import static com.example.board.bff.api.utils.ErrorUtils.addGlobalErrorReturnView;

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
public class SignupController {
    private static final String SIGNUP_VIEW_NAME = "auth/signup";
    private final SignupService signupService;
    private final AuthApiClient authApiClient;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("signupFormData")) {
            model.addAttribute("signupFormData", SignupRequest.empty());
        }
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @CookieValue(value = "reg_tkt", required = false) String token,
            @Validated(SignupValidationSequence.class)
            @ModelAttribute("signupFormData") SignupRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        if (token == null || token.isBlank()) {
            return addGlobalErrorReturnView(bindingResult, "token.expired", "이메일 인증이 만료되었습니다. 다시 인증해주세요.", SIGNUP_VIEW_NAME);
        }

        var result = signupService.signup(token, request);

        return switch (result) {
            case SignupResult.Success _ -> "redirect:/";
            case SignupResult.TokenExpired(var message) -> addGlobalErrorReturnView(bindingResult, "token.expired", message, SIGNUP_VIEW_NAME);
            case SignupResult.TokenInvalid(var message) -> addGlobalErrorReturnView(bindingResult, "token.invalid", message, SIGNUP_VIEW_NAME);
            case SignupResult.EmailDomainNotAllowed(var message) -> addFieldErrorReturnView(bindingResult, "email", "email.domainNotAllowed", message, SIGNUP_VIEW_NAME);
            case SignupResult.UsernameDuplicated(var message) -> addFieldErrorReturnView(bindingResult, "username", "username.duplicate", message, SIGNUP_VIEW_NAME);
            case SignupResult.EmailDuplicated(var message) -> addFieldErrorReturnView(bindingResult, "email", "email.domainNotAllowed", message, SIGNUP_VIEW_NAME);
            case SignupResult.NicknameDuplicated(var message) -> addFieldErrorReturnView(bindingResult, "nickname", "nickname.duplicate", message, SIGNUP_VIEW_NAME);
            case SignupResult.SystemError(var message) -> addGlobalErrorReturnView(bindingResult, "signup.system", message, SIGNUP_VIEW_NAME);
        };
    }

    @GetMapping("/signup/members/check-username")
    @ResponseBody
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkUsernameAvailability(@RequestParam("username") String username) {
        return ResponseEntity.ok(authApiClient.checkUsernameAvailability(username));
    }

    @GetMapping("/signup/members/check-email")
    @ResponseBody
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmailAvailability(@RequestParam("email") String email) {
        return ResponseEntity.ok(authApiClient.checkEmailAvailability(email));
    }

    @GetMapping("/signup/members/check-nickname")
    @ResponseBody
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkNicknameAvailability(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok(authApiClient.checkNicknameAvailability(nickname));
    }

    @PostMapping("/signup/email-verification")
    @ResponseBody
    public ResponseEntity<ApiResponse<SignupEmailSendResponse>> sendOtp(@RequestBody EmailSendRequest request) {
        return ResponseEntity.ok(authApiClient.sendOtp(request));
    }

    @PostMapping("/signup/email-verification/verify")
    @ResponseBody
    public ResponseEntity<ApiResponse<SignupEmailVerifyResponse>> verifyOtp(@RequestBody EmailVerifyRequest request) {
        var downstreamResponse = authApiClient.verifyOtp(request);
        var emailVerifyResponse = downstreamResponse.data();
        var cookie = ResponseCookie
                .from("reg_tkt", emailVerifyResponse.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(downstreamResponse);
    }
}