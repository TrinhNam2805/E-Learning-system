package com.elearning.controller;

import com.elearning.service.PasswordResetService;
import com.elearning.service.UserService;
import com.elearning.util.RedirectUrls;
import com.elearning.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private static final String SESSION_LOGIN_REDIRECT = "LOGIN_REDIRECT";

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String redirect,
                            HttpServletRequest request,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password.");
        if (logout != null) model.addAttribute("message", "You have been logged out.");

        if (redirect != null && RedirectUrls.isSafeRelativePath(redirect)) {
            model.addAttribute("redirectAfterLogin", redirect);
            request.getSession().setAttribute(SESSION_LOGIN_REDIRECT, redirect);
        } else if (error != null) {
            Object s = request.getSession().getAttribute(SESSION_LOGIN_REDIRECT);
            if (s instanceof String && RedirectUrls.isSafeRelativePath((String) s)) {
                model.addAttribute("redirectAfterLogin", s);
            }
        } else {
            request.getSession().removeAttribute(SESSION_LOGIN_REDIRECT);
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String redirect,
                               HttpServletRequest request,
                               Model model) {
        if (redirect != null && RedirectUrls.isSafeRelativePath(redirect)) {
            model.addAttribute("redirectAfterLogin", redirect);
            request.getSession().setAttribute(SESSION_LOGIN_REDIRECT, redirect);
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String confirm,
                             @RequestParam(required = false) String redirect,
                             RedirectAttributes ra) {
        if (!password.equals(confirm)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/register" + redirectQuery(redirect);
        }
        String pwdErr = PasswordPolicy.validate(password).orElse(null);
        if (pwdErr != null) {
            ra.addFlashAttribute("error", pwdErr);
            return "redirect:/register" + redirectQuery(redirect);
        }
        try {
            userService.register(fullName, email, password);
            ra.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login" + redirectQuery(redirect);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/register" + redirectQuery(redirect);
        }
    }

    private static String redirectQuery(String redirect) {
        if (redirect == null || !RedirectUrls.isSafeRelativePath(redirect)) {
            return "";
        }
        try {
            return "?redirect=" + URLEncoder.encode(redirect, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPasswordSubmit(@RequestParam String email, RedirectAttributes ra) {
        passwordResetService.requestReset(email);
        ra.addFlashAttribute("success",
                "If an account exists for that email, we sent password reset instructions.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("error", "Invalid or missing reset link.");
            return "auth/reset-password";
        }
        if (!passwordResetService.validateToken(token).isPresent()) {
            model.addAttribute("error", "This reset link is invalid or has expired.");
            return "auth/reset-password";
        }
        model.addAttribute("token", token);
        model.addAttribute("passwordHint", PasswordPolicy.requirementsHint());
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPasswordSubmit(@RequestParam String token,
                                      @RequestParam String password,
                                      @RequestParam String confirm,
                                      RedirectAttributes ra) {
        if (!password.equals(confirm)) {
            ra.addFlashAttribute("error", "Passwords do not match.");
            return "redirect:/reset-password?token=" + token;
        }
        String pwdErr = PasswordPolicy.validate(password).orElse(null);
        if (pwdErr != null) {
            ra.addFlashAttribute("error", pwdErr);
            return "redirect:/reset-password?token=" + token;
        }
        try {
            passwordResetService.resetPassword(token, password);
            ra.addFlashAttribute("success", "Password updated. You can log in now.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}
