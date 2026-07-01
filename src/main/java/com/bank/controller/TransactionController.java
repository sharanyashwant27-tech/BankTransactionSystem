package com.bank.controller;

import com.bank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TransactionController {

    @Autowired
    TransactionRepository repository;

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute(
                "transactions",
                repository.findAll()
        );

        return "transactions";
    }
}
