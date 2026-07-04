package com.bank.service;

import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminService {

    public static final String SALARY_PREFIX = "Salary credit";
    public static final String INCENTIVE_PREFIX = "Performance incentive";

    private static final Map<String, Double> DEFAULT_INCENTIVE_AMOUNTS = Map.of(
            "OUTSTANDING", 1500.0,
            "EXCELLENT", 1000.0,
            "GOOD", 500.0,
            "SATISFACTORY", 250.0
    );

    private static final Set<String> VALID_PERFORMANCE_LEVELS = DEFAULT_INCENTIVE_AMOUNTS.keySet();

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getCreditRecipients() {
        return userRepository.findAll().stream()
                .sorted((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(left.getUsername(), right.getUsername()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getDefaultIncentiveAmounts() {
        return DEFAULT_INCENTIVE_AMOUNTS;
    }

    @Transactional(readOnly = true)
    public double getSuggestedIncentiveAmount(String performanceLevel) {
        return DEFAULT_INCENTIVE_AMOUNTS.getOrDefault(normalizePerformanceLevel(performanceLevel), 0.0);
    }

    @Transactional
    public Transaction creditSalary(String adminUsername, String recipientUsername, Double amount,
                                    LocalDate creditDate, String note) {
        validateAdmin(adminUsername);
        validateAmount(amount);
        User recipient = requireRecipient(recipientUsername);

        String description = buildSalaryDescription(note);
        return saveAdminCredit(recipient, description, amount, creditDate);
    }

    @Transactional
    public Transaction creditPerformanceIncentive(String adminUsername, String recipientUsername,
                                                  String performanceLevel, Double amount,
                                                  LocalDate creditDate, String note) {
        validateAdmin(adminUsername);
        validateAmount(amount);

        String level = normalizePerformanceLevel(performanceLevel);
        if (!VALID_PERFORMANCE_LEVELS.contains(level)) {
            throw new IllegalArgumentException("Invalid performance rating: " + performanceLevel);
        }

        User recipient = requireRecipient(recipientUsername);
        String description = buildIncentiveDescription(level, note);
        return saveAdminCredit(recipient, description, amount, creditDate);
    }

    public static boolean isAdminIssuedCredit(String description) {
        if (description == null || description.isBlank()) {
            return false;
        }
        String normalized = description.trim();
        return normalized.regionMatches(true, 0, SALARY_PREFIX, 0, SALARY_PREFIX.length())
                || normalized.regionMatches(true, 0, INCENTIVE_PREFIX, 0, INCENTIVE_PREFIX.length());
    }

    private Transaction saveAdminCredit(User recipient, String description, Double amount, LocalDate creditDate) {
        Transaction transaction = new Transaction();
        transaction.setUser(recipient);
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TYPE_CREDIT);
        transaction.setTransactionDate(creditDate != null ? creditDate : LocalDate.now());
        return transactionRepository.save(transaction);
    }

    private String buildSalaryDescription(String note) {
        String cleanNote = note != null ? note.trim() : "";
        if (cleanNote.isEmpty()) {
            return SALARY_PREFIX;
        }
        return SALARY_PREFIX + " — " + cleanNote;
    }

    private String buildIncentiveDescription(String performanceLevel, String note) {
        String label = formatPerformanceLabel(performanceLevel);
        String cleanNote = note != null ? note.trim() : "";
        if (cleanNote.isEmpty()) {
            return INCENTIVE_PREFIX + " (" + label + ")";
        }
        return INCENTIVE_PREFIX + " (" + label + ") — " + cleanNote;
    }

    private String formatPerformanceLabel(String performanceLevel) {
        return performanceLevel.substring(0, 1).toUpperCase()
                + performanceLevel.substring(1).toLowerCase();
    }

    private String normalizePerformanceLevel(String performanceLevel) {
        if (performanceLevel == null || performanceLevel.isBlank()) {
            throw new IllegalArgumentException("Performance rating is required");
        }
        return performanceLevel.trim().toUpperCase();
    }

    private void validateAdmin(String adminUsername) {
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
        if (!admin.isAdmin()) {
            throw new IllegalArgumentException("Only administrators can issue salary or incentive credits");
        }
    }

    private User requireRecipient(String recipientUsername) {
        if (recipientUsername == null || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username is required");
        }

        return userRepository.findByUsername(recipientUsername.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + recipientUsername));
    }

    private void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
