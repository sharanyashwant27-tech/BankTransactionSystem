package com.bank.controller;

import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public DashboardController(TransactionRepository transactionRepository, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
    }

    @GetMapping("/security")
    public String security(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        return "security";
    }

    @GetMapping("/spending")
    public String spending(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);

        double total = sumAmounts(transactions);
        double average = transactions.isEmpty() ? 0 : total / transactions.size();
        double highest = maxAmount(transactions);
        double lowest = minAmount(transactions);

        List<Transaction> topSpending = transactions.stream()
                .sorted(Comparator.comparing(DashboardController::amountOrZero).reversed())
                .limit(5)
                .toList();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("transactions", transactions);
        model.addAttribute("topSpending", topSpending);
        model.addAttribute("totalSpending", total);
        model.addAttribute("averageSpending", average);
        model.addAttribute("highestSpending", highest);
        model.addAttribute("lowestSpending", lowest);
        model.addAttribute("transactionCount", transactions.size());
        model.addAttribute("maxAmount", highest > 0 ? highest : 1);

        return "spending";
    }

    private static double sumAmounts(List<Transaction> transactions) {
        double total = 0;
        for (Transaction transaction : transactions) {
            total += amountOrZero(transaction);
        }
        return total;
    }

    private static double maxAmount(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(DashboardController::amountOrZero)
                .max()
                .orElse(0);
    }

    private static double minAmount(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble(DashboardController::amountOrZero)
                .min()
                .orElse(0);
    }

    private static double amountOrZero(Transaction transaction) {
        Double amount = transaction.getAmount();
        return amount != null ? amount : 0.0;
    }
}
