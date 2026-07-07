package com.bank.controller;

import com.bank.security.LoginRedirectHandlers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute(LoginRedirectHandlers.LOGOUT_MESSAGE_ATTR))) {
            model.addAttribute("logoutMessage", true);
            session.removeAttribute(LoginRedirectHandlers.LOGOUT_MESSAGE_ATTR);
        }
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError() {
        return "login-error";
    }

    @GetMapping("/session-expired")
    public String sessionExpired(HttpServletRequest request) {
        clearSession(request);
        return "session-expired";
    }

    @GetMapping("/return-to-login")
    public String returnToLogin(HttpServletRequest request) {
        clearSession(request);
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    private void clearSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        request.getSession(true);
    }
}
