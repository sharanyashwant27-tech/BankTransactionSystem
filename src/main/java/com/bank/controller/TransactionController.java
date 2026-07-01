package com.bank.controller;

import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionController {

    private final TransactionRepository repository;
    private final UserService userService;

    public TransactionController(TransactionRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @GetMapping("/transactions")
    public String transactions(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("username", user.getUsername());
        model.addAttribute(
                "transactions",
                repository.findByUserOrderByTransactionDateDesc(user)
        );
        return "transactions";
    }
}
