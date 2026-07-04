package com.bank.config;

import com.bank.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavModelAdvice {

    private final UserService userService;

    public NavModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : null;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userService.isAdmin(userDetails.getUsername());
    }
}
