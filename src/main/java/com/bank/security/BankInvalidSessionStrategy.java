package com.bank.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BankInvalidSessionStrategy implements InvalidSessionStrategy {

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        if ("/login".equals(requestUri) || "/return-to-login".equals(requestUri)) {
            request.getSession(true);
            response.sendRedirect(contextPath + requestUri);
            return;
        }

        if ("/session-expired".equals(requestUri)) {
            request.getSession(true);
            response.sendRedirect(contextPath + "/session-expired");
            return;
        }

        response.sendRedirect(contextPath + "/session-expired");
    }
}
