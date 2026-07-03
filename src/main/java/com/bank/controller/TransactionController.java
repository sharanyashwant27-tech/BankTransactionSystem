package com.bank.controller;

import com.bank.dto.TransactionForm;
import com.bank.dto.TransferForm;
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
import java.util.Set;

@Controller
public class TransactionController {

    private static final String TAB_HISTORY = "history";
    private static final String TAB_MANAGE = "manage";
    private static final String TAB_SUMMARY = "summary";
    private static final String TAB_TRANSFER = "transfer";
    private static final Set<String> VALID_TABS = Set.of(TAB_HISTORY, TAB_MANAGE, TAB_SUMMARY, TAB_TRANSFER);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public String transactions(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) Long editId,
                               @RequestParam(defaultValue = TAB_HISTORY) String tab,
                               Model model) {
        String activeTab = resolveTab(tab);
        if (editId != null && TAB_HISTORY.equals(activeTab)) {
            activeTab = TAB_MANAGE;
        }

        preparePage(userDetails.getUsername(), editId, activeTab, model);

        if (!model.containsAttribute("createForm")) {
            TransactionForm createForm = new TransactionForm();
            createForm.setTransactionDate(LocalDate.now());
            model.addAttribute("createForm", createForm);
        }

        if (!model.containsAttribute("transferForm")) {
            model.addAttribute("transferForm", new TransferForm());
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
            preparePage(userDetails.getUsername(), null, TAB_MANAGE, model);
            model.addAttribute("createForm", form);
            model.addAttribute("transferForm", new TransferForm());
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
            preparePage(userDetails.getUsername(), null, TAB_MANAGE, model);
            model.addAttribute("createForm", form);
            model.addAttribute("transferForm", new TransferForm());
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions";
        }

        return "redirect:/transactions?tab=manage";
    }

    @PostMapping("/transactions/transfer")
    public String transfer(@AuthenticationPrincipal UserDetails userDetails,
                           @Valid @ModelAttribute("transferForm") TransferForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(userDetails.getUsername(), null, TAB_TRANSFER, model);
            model.addAttribute("transferForm", form);
            ensureCreateForm(model);
            return "transactions";
        }

        try {
            transactionService.transfer(
                    userDetails.getUsername(),
                    form.getRecipientUsername(),
                    form.getAmount(),
                    form.getNote()
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Transfer of $" + String.format("%.2f", form.getAmount())
                            + " to " + form.getRecipientUsername().trim() + " completed successfully."
            );
        } catch (IllegalArgumentException ex) {
            preparePage(userDetails.getUsername(), null, TAB_TRANSFER, model);
            model.addAttribute("transferForm", form);
            ensureCreateForm(model);
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions";
        }

        return "redirect:/transactions?tab=transfer";
    }

    @PostMapping("/transactions/{id}/update")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("editForm") TransactionForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            preparePage(userDetails.getUsername(), id, TAB_MANAGE, model);
            model.addAttribute("editForm", form);
            model.addAttribute("editId", id);
            ensureCreateForm(model);
            model.addAttribute("transferForm", new TransferForm());
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
            preparePage(userDetails.getUsername(), id, TAB_MANAGE, model);
            model.addAttribute("editForm", form);
            model.addAttribute("editId", id);
            ensureCreateForm(model);
            model.addAttribute("transferForm", new TransferForm());
            model.addAttribute("errorMessage", ex.getMessage());
            return "transactions";
        }

        return "redirect:/transactions?tab=manage";
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

        return "redirect:/transactions?tab=history";
    }

    private void preparePage(String username, Long editId, String activeTab, Model model) {
        model.addAttribute("username", username);
        model.addAttribute("transactions", transactionService.getTransactionsForUser(username));
        model.addAttribute("accountSummary", transactionService.getAccountSummary(username));
        model.addAttribute("transferRecipients", transactionService.getTransferRecipients(username));
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("activePage", TAB_TRANSFER.equals(activeTab) ? "transfer" : "history");

        if (editId != null && !model.containsAttribute("editForm")) {
            transactionService.getTransactionForUser(username, editId).ifPresent(t -> {
                model.addAttribute("editForm", toForm(t));
                model.addAttribute("editId", t.getId());
            });
        }
    }

    private String resolveTab(String tab) {
        if (tab == null || tab.isBlank()) {
            return TAB_HISTORY;
        }
        String normalized = tab.trim().toLowerCase();
        return VALID_TABS.contains(normalized) ? normalized : TAB_HISTORY;
    }

    private void ensureCreateForm(Model model) {
        if (!model.containsAttribute("createForm")) {
            TransactionForm createForm = new TransactionForm();
            createForm.setTransactionDate(LocalDate.now());
            model.addAttribute("createForm", createForm);
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
