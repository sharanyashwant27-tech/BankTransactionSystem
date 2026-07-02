package com.bank.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginRedirectHandlers implements AuthenticationFailureHandler, LogoutSuccessHandler {

    public static final String LOGIN_ERROR_ATTR = "loginError";
    public static final String LOGOUT_MESSAGE_ATTR = "logoutMessage";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
            throws IOException {
        request.getSession().setAttribute(LOGIN_ERROR_ATTR, Boolean.TRUE);
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        request.getSession().setAttribute(LOGOUT_MESSAGE_ATTR, Boolean.TRUE);
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
