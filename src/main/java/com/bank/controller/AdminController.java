package com.bank.controller;

import com.bank.dto.IncentiveCreditForm;
import com.bank.dto.SalaryCreditForm;
import com.bank.entity.User;
import com.bank.service.AdminService;
import com.bank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    @GetMapping
    public String adminPanel(@AuthenticationPrincipal UserDetails userDetails,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(userDetails, redirectAttributes);
        if (denied != null) {
            return denied;
        }

        preparePage(model);
        if (!model.containsAttribute("salaryForm")) {
            model.addAttribute("salaryForm", newSalaryForm());
        }
        if (!model.containsAttribute("incentiveForm")) {
            model.addAttribute("incentiveForm", newIncentiveForm());
        }
        model.addAttribute("activePage", "admin");
        return "admin";
    }

    @PostMapping("/salary")
    public String creditSalary(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute("salaryForm") SalaryCreditForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(userDetails, redirectAttributes);
        if (denied != null) {
            return denied;
        }

        if (bindingResult.hasErrors()) {
            preparePage(model);
            model.addAttribute("incentiveForm", newIncentiveForm());
            model.addAttribute("activePage", "admin");
            return "admin";
        }

        try {
            adminService.creditSalary(
                    userDetails.getUsername(),
                    form.getRecipientUsername(),
                    form.getAmount(),
                    form.getCreditDate(),
                    form.getNote()
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Salary of $" + String.format("%.2f", form.getAmount())
                            + " credited to " + form.getRecipientUsername()
                            + " for " + form.getCreditDate() + ".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/incentive")
    public String creditIncentive(@AuthenticationPrincipal UserDetails userDetails,
                                  @Valid @ModelAttribute("incentiveForm") IncentiveCreditForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        String denied = denyUnlessAdmin(userDetails, redirectAttributes);
        if (denied != null) {
            return denied;
        }

        if (bindingResult.hasErrors()) {
            preparePage(model);
            model.addAttribute("salaryForm", newSalaryForm());
            model.addAttribute("activePage", "admin");
            return "admin";
        }

        try {
            adminService.creditPerformanceIncentive(
                    userDetails.getUsername(),
                    form.getRecipientUsername(),
                    form.getPerformanceLevel(),
                    form.getAmount(),
                    form.getCreditDate(),
                    form.getNote()
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Performance incentive of $" + String.format("%.2f", form.getAmount())
                            + " credited to " + form.getRecipientUsername()
                            + " (" + form.getPerformanceLevel() + ").");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin";
    }

    private String denyUnlessAdmin(UserDetails userDetails, RedirectAttributes redirectAttributes) {
        if (userDetails == null || !userService.isAdmin(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Access denied. Only administrators can open the Administration page.");
            return "redirect:/home";
        }
        return null;
    }

    private void preparePage(Model model) {
        List<User> recipients = adminService.getCreditRecipients();
        Map<String, Double> incentiveAmounts = adminService.getDefaultIncentiveAmounts();
        model.addAttribute("recipients", recipients);
        model.addAttribute("incentiveAmounts", incentiveAmounts);
    }

    private SalaryCreditForm newSalaryForm() {
        SalaryCreditForm form = new SalaryCreditForm();
        form.setCreditDate(LocalDate.now());
        return form;
    }

    private IncentiveCreditForm newIncentiveForm() {
        IncentiveCreditForm form = new IncentiveCreditForm();
        form.setCreditDate(LocalDate.now());
        form.setPerformanceLevel(IncentiveCreditForm.GOOD);
        form.setAmount(adminService.getSuggestedIncentiveAmount(IncentiveCreditForm.GOOD));
        return form;
    }
}
