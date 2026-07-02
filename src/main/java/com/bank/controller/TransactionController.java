package com.bank.controller;

import com.bank.dto.TransactionForm;
import com.bank.entity.Transaction;
import com.bank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public String transactions(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) Long editId,
                               Model model) {
        preparePage(userDetails.getUsername(), editId, model);

        if (!model.containsAttribute("createForm")) {
            TransactionForm createForm = new TransactionForm();
            createForm.setTransactionDate(LocalDate.now());
            model.addAttribute("createForm", createForm);
        }

        return "transactions";
    }

    @PostMapping("/transactions/create")
    public String create(@AuthenticationPrincipal UserDetails userDetails,
                         @Valid @ModelAttribute("createForm") TransactionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(userDetails.getUsername(), null, model);
            model.addAttribute("createForm", form);
            return "transactions";
        }

        try {
            transactionService.createTransaction(
                    userDetails.getUsername(),
                    form.getDescription(),
                    form.getAmount(),
                    form.getTransactionDate()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Transaction created successfully.");
        } catch (IllegalArgumentException ex) {
            preparePage(userDetails.getUsername(), null, model);
            model.addAttribute("createForm", form);
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions";
        }

        return "redirect:/transactions";
    }

    @PostMapping("/transactions/{id}/update")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("editForm") TransactionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(userDetails.getUsername(), id, model);
            model.addAttribute("editForm", form);
            model.addAttribute("editId", id);
            return "transactions";
        }

        try {
            transactionService.updateTransaction(
                    userDetails.getUsername(),
                    id,
                    form.getDescription(),
                    form.getAmount(),
                    form.getTransactionDate()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully.");
        } catch (IllegalArgumentException ex) {
            preparePage(userDetails.getUsername(), id, model);
            model.addAttribute("editForm", form);
            model.addAttribute("editId", id);
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions";
        }

        return "redirect:/transactions";
    }

    @PostMapping("/transactions/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            transactionService.deleteTransaction(userDetails.getUsername(), id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/transactions";
    }

    private void preparePage(String username, Long editId, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("transactions", transactionService.getTransactionsForUser(username));
        model.addAttribute("accountSummary", transactionService.getAccountSummary(username));

        if (editId != null && !model.containsAttribute("editForm")) {
            transactionService.getTransactionForUser(username, editId).ifPresent(t -> {
                model.addAttribute("editForm", toForm(t));
                model.addAttribute("editId", t.getId());
            });
        }
    }

    private TransactionForm toForm(Transaction transaction) {
        TransactionForm form = new TransactionForm();
        form.setId(transaction.getId());
        form.setDescription(transaction.getDescription());
        form.setAmount(transaction.getAmount());
        form.setTransactionDate(transaction.getTransactionDate());
        return form;
    }
}
