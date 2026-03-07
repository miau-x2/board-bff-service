package com.example.board.bff.controller;

import com.example.board.bff.controller.dto.request.LoginRequest;
import com.example.board.bff.commons.utils.FlashMapKey;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginForm(
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            Model model
    ) {
        if (!model.containsAttribute("loginFormData")) {
            model.addAttribute("loginFormData", LoginRequest.empty());
        }
        if (!model.containsAttribute("redirect")) {
            model.addAttribute("redirect", redirect);
        }

        var flashMap = RequestContextUtils.getInputFlashMap(request);
        if (flashMap != null) {
            if (flashMap.containsKey(FlashMapKey.LOGIN_FIELD_ERROR_MESSAGE)) {
                model.addAttribute("loginFieldErrorMessage", flashMap.get(FlashMapKey.LOGIN_FIELD_ERROR_MESSAGE));
            }
            if (flashMap.containsKey(FlashMapKey.LOGIN_GLOBAL_ERROR_MESSAGE)) {
                model.addAttribute("loginGlobalErrorMessage", flashMap.get(FlashMapKey.LOGIN_GLOBAL_ERROR_MESSAGE));
            }
        }

        return "auth/login";
    }
}