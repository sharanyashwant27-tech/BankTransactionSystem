package com.bank.config;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavModelAdvice {

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : null;
    }
}
