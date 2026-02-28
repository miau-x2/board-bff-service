package com.example.board.bff.api.auth.controller;

import com.example.board.bff.api.auth.client.AuthApiClient;
import com.example.board.bff.api.auth.client.response.AuthServiceErrorCode;
import com.example.board.bff.api.auth.client.response.AvailabilityResponse;
import com.example.board.bff.api.auth.client.response.SignupEmailSendResponse;
import com.example.board.bff.api.auth.client.response.SignupEmailVerifyResponse;
import com.example.board.bff.api.auth.controller.dto.EmailSendRequest;
import com.example.board.bff.api.auth.controller.dto.EmailVerifyRequest;
import com.example.board.bff.api.auth.controller.dto.MemberSignupRequest;
import com.example.board.bff.api.auth.controller.dto.validation.SignupValidationSequence;
import com.example.board.bff.api.exception.FeignExceptions;
import com.example.board.bff.commons.response.ApiResponse;
import feign.FeignException;
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

@Slf4j
@Validated
@Controller
@RequiredArgsConstructor
public class SignupController {
    private final AuthApiClient authApiClient;
    private final FeignExceptions feignExceptions;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("signupFormData")) {
            model.addAttribute("signupFormData", MemberSignupRequest.empty());
        }
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(
            @CookieValue(value = "reg_tkt", required = false) String token,
            @Validated(SignupValidationSequence.class)
            @ModelAttribute("signupFormData")
            MemberSignupRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        if (token == null || token.isBlank()) {
            return signupGlobalError(bindingResult,"token.expired","이메일 인증이 만료되었습니다. 다시 인증해주세요.");
        }

        try {
            authApiClient.signUp(token, request);
            return "redirect:/";
        } catch (FeignException e) {
            return feignExceptions.extractErrorResponse(e)
                    .flatMap(response -> AuthServiceErrorCode.fromCode(response.code()))
                    .map(code -> switch (code) {
                        case TOKEN_EXPIRED -> signupGlobalError(bindingResult, "token.expired", "이메일 인증이 만료되었습니다. 다시 인증해주세요.");
                        case TOKEN_INVALID -> signupGlobalError(bindingResult, "token.invalid", "이메일 인증이 유효하지 않습니다. 다시 인증해주세요.");
                        case EMAIL_DOMAIN_NOT_ALLOWED -> signupFieldError(bindingResult, "email", "email.domainNotAllowed", "지메일과 네이버메일만 사용할 수 있습니다.");
                        case USERNAME_DUPLICATED -> signupFieldError(bindingResult, "username", "username.duplicate", "이미 사용 중인 아이디입니다.");
                        case EMAIL_DUPLICATED -> signupFieldError(bindingResult, "email", "email.duplicate", "이미 사용 중인 이메일입니다.");
                        case NICKNAME_DUPLICATED -> signupFieldError(bindingResult, "nickname", "nickname.duplicate", "이미 사용 중인 닉네임입니다.");
                        case INTERNAL_SERVER_ERROR -> signupGlobalError(bindingResult, "signup.system", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요.");
                    })
                    .orElseGet(() -> signupGlobalError(bindingResult, "system", "현재 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요."));
        }
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

    private void addFieldError(BindingResult bindingResult, String field, String errorCode, String defaultMessage) {
        bindingResult.rejectValue(field, errorCode, defaultMessage);
    }

    private void addGlobalError(BindingResult bindingResult, String errorCode, String defaultMessage) {
        bindingResult.reject(errorCode, defaultMessage);
    }

    private String signupFieldError(BindingResult bindingResult, String field, String errorCode, String defaultMessage) {
        addFieldError(bindingResult, field, errorCode, defaultMessage);
        return "auth/signup";
    }

    private String signupGlobalError(BindingResult bindingResult, String errorCode, String defaultMessage) {
        addGlobalError(bindingResult, errorCode, defaultMessage);
        return "auth/signup";
    }
}
