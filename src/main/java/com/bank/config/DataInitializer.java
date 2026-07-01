package com.bank.config;

import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin123";
    public static final String ADMIN_EMAIL = "admin@bank.com";

    private static final String DEFAULT_PASSWORD = "admin123";

    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           UserService userService,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUser(ADMIN_USERNAME, ADMIN_PASSWORD, ADMIN_EMAIL, List.of(
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

        seedUser("admin1", DEFAULT_PASSWORD, "admin1@bank.com", List.of(
                tx("Freelance payment", 1200.00, "2026-07-03"),
                tx("Book purchase", 35.99, "2026-07-01"),
                tx("Fuel refill", 60.00, "2026-06-27"),
                tx("Mobile recharge", 25.00, "2026-06-22")
        ));

        seedUser("admin2", DEFAULT_PASSWORD, "admin2@bank.com", List.of(
                tx("Bonus credit", 800.00, "2026-07-02"),
                tx("Medical bill", 150.00, "2026-06-30"),
                tx("Coffee shop", 12.50, "2026-06-26"),
                tx("Bus pass", 45.00, "2026-06-18")
        ));

        seedUser("admin3", DEFAULT_PASSWORD, "admin3@bank.com", List.of(
                tx("Rent payment", 950.00, "2026-07-01"),
                tx("Internet bill", 55.00, "2026-06-29"),
                tx("Gym membership", 40.00, "2026-06-24"),
                tx("Pharmacy", 28.75, "2026-06-19")
        ));

        seedUser("admin4", DEFAULT_PASSWORD, "admin4@bank.com", List.of(
                tx("Client invoice", 2100.00, "2026-07-04"),
                tx("Office supplies", 78.25, "2026-06-28"),
                tx("Parking fee", 15.00, "2026-06-23"),
                tx("Team lunch", 92.00, "2026-06-17")
        ));

        seedUser("admin5", DEFAULT_PASSWORD, "admin5@bank.com", List.of(
                tx("Stock dividend", 640.00, "2026-07-02"),
                tx("Home repair", 220.00, "2026-06-27"),
                tx("Streaming subscription", 14.99, "2026-06-21"),
                tx("Gift purchase", 65.00, "2026-06-16")
        ));
    }

    private void seedUser(String username, String password, String email, List<TransactionSeed> seeds) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userService.register(username, password, email));

        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        seedTransactions(user, seeds);
    }

    private void seedTransactions(User user, List<TransactionSeed> seeds) {
        List<Transaction> existing = transactionRepository.findByUserOrderByTransactionDateDesc(user);

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
        transactionRepository.save(transaction);
    }

    private TransactionSeed tx(String description, double amount, String date) {
        return new TransactionSeed(description, amount, LocalDate.parse(date));
    }

    private record TransactionSeed(String description, double amount, LocalDate date) {
    }
}
