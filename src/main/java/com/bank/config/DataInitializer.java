package com.bank.config;

import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final Set<String> CREDIT_DESCRIPTIONS = Set.of(
            "Salary deposit",
            "Cashback reward",
            "Freelance payment",
            "Bonus credit",
            "Client invoice",
            "Stock dividend"
    );

    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;
    private final String defaultPassword;

    public DataInitializer(UserRepository userRepository,
                           UserService userService,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.seed.admin-username}") String adminUsername,
                           @Value("${app.seed.admin-password}") String adminPassword,
                           @Value("${app.seed.admin-email}") String adminEmail,
                           @Value("${app.seed.default-password}") String defaultPassword) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
        this.defaultPassword = defaultPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword.isBlank() || defaultPassword.isBlank()) {
            log.warn("Demo user seeding skipped: set APP_SEED_ADMIN_PASSWORD and APP_SEED_DEFAULT_PASSWORD.");
            syncExistingRoles();
            return;
        }

        syncExistingRoles();
        seedAdminUser(adminUsername, adminPassword, adminEmail, 15000.00, List.of(
                tx("Salary deposit", 5000.00, "2026-07-01"),
                tx("Grocery shopping", 125.50, "2026-07-02"),
                tx("Electricity bill", 89.99, "2026-06-28"),
                tx("ATM withdrawal", 200.00, "2026-06-25"),
                tx("Online transfer", 350.00, "2026-06-20"),
                tx("Restaurant payment", 45.75, "2026-06-15"),
                tx("Investment transfer", 1500.00, "2026-06-10"),
                tx("Insurance premium", 220.00, "2026-06-05"),
                tx("Cashback reward", 35.00, "2026-06-01"),
                tx("Hotel booking", 310.00, "2026-05-28")
        ));

        seedEmployeeUser("admin1", defaultPassword, "admin1@bank.com", 8000.00, List.of(
                tx("Freelance payment", 1200.00, "2026-07-03"),
                tx("Book purchase", 35.99, "2026-07-01"),
                tx("Fuel refill", 60.00, "2026-06-27"),
                tx("Mobile recharge", 25.00, "2026-06-22")
        ));

        seedEmployeeUser("admin2", defaultPassword, "admin2@bank.com", 6000.00, List.of(
                tx("Bonus credit", 800.00, "2026-07-02"),
                tx("Medical bill", 150.00, "2026-06-30"),
                tx("Coffee shop", 12.50, "2026-06-26"),
                tx("Bus pass", 45.00, "2026-06-18")
        ));

        seedEmployeeUser("admin3", defaultPassword, "admin3@bank.com", 7000.00, List.of(
                tx("Rent payment", 950.00, "2026-07-01"),
                tx("Internet bill", 55.00, "2026-06-29"),
                tx("Gym membership", 40.00, "2026-06-24"),
                tx("Pharmacy", 28.75, "2026-06-19")
        ));

        seedEmployeeUser("admin4", defaultPassword, "admin4@bank.com", 12000.00, List.of(
                tx("Client invoice", 2100.00, "2026-07-04"),
                tx("Office supplies", 78.25, "2026-06-28"),
                tx("Parking fee", 15.00, "2026-06-23"),
                tx("Team lunch", 92.00, "2026-06-17")
        ));

        seedEmployeeUser("admin5", defaultPassword, "admin5@bank.com", 9000.00, List.of(
                tx("Stock dividend", 640.00, "2026-07-02"),
                tx("Home repair", 220.00, "2026-06-27"),
                tx("Streaming subscription", 14.99, "2026-06-21"),
                tx("Gift purchase", 65.00, "2026-06-16")
        ));
    }

    private void syncExistingRoles() {
        for (User user : userRepository.findAll()) {
            String expectedRole = adminUsername.equalsIgnoreCase(user.getUsername())
                    ? User.ROLE_ADMIN
                    : User.ROLE_USER;
            if (user.getRole() == null || !expectedRole.equalsIgnoreCase(user.getRole())) {
                user.setRole(expectedRole);
                userRepository.save(user);
                log.info("Updated role for {} to {}", user.getUsername(), expectedRole);
            }
        }
    }

    private void seedAdminUser(String username, String password, String email, double openingBalance, List<TransactionSeed> seeds) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userService.registerAdmin(username, password, email));

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setOpeningBalance(openingBalance);
        user.setRole(User.ROLE_ADMIN);
        userRepository.save(user);

        seedTransactions(user, seeds);
    }

    private void seedEmployeeUser(String username, String password, String email, double openingBalance, List<TransactionSeed> seeds) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userService.register(username, password, email));

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setOpeningBalance(openingBalance);
        user.setRole(User.ROLE_USER);
        userRepository.save(user);

        seedTransactions(user, seeds);
    }

    private void seedTransactions(User user, List<Transaction> existing, List<TransactionSeed> seeds) {
        if (existing.isEmpty()) {
            saveTransactions(user, seeds);
            return;
        }

        for (TransactionSeed seed : seeds) {
            boolean alreadyExists = existing.stream().anyMatch(t ->
                    seed.description().equals(t.getDescription())
                            && Double.valueOf(seed.amount()).equals(t.getAmount())
                            && seed.date().equals(t.getTransactionDate()));

            if (!alreadyExists) {
                saveTransaction(user, seed);
            }
        }
    }

    private void seedTransactions(User user, List<TransactionSeed> seeds) {
        List<Transaction> existing = transactionRepository.findByUserOrderByTransactionDateDesc(user);
        seedTransactions(user, existing, seeds);
    }

    private void saveTransactions(User user, List<TransactionSeed> seeds) {
        for (TransactionSeed seed : seeds) {
            saveTransaction(user, seed);
        }
    }

    private void saveTransaction(User user, TransactionSeed seed) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setDescription(seed.description());
        transaction.setAmount(seed.amount());
        transaction.setTransactionDate(seed.date());
        transaction.setType(seed.type());
        transactionRepository.save(transaction);
    }

    private TransactionSeed tx(String description, double amount, String date) {
        String type = CREDIT_DESCRIPTIONS.contains(description)
                ? Transaction.TYPE_CREDIT
                : Transaction.TYPE_DEBIT;
        return new TransactionSeed(description, amount, LocalDate.parse(date), type);
    }

    private record TransactionSeed(String description, double amount, LocalDate date, String type) {
    }
}
