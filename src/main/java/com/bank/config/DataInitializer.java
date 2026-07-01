package com.bank.config;

import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TransactionRepository transactionRepository;

    public DataInitializer(UserRepository userRepository,
                           UserService userService,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin@bank.com", List.of(
                tx("Salary deposit", 5000.00, "2026-07-01"),
                tx("Grocery shopping", 125.50, "2026-07-02"),
                tx("Electricity bill", 89.99, "2026-06-28"),
                tx("ATM withdrawal", 200.00, "2026-06-25"),
                tx("Online transfer", 350.00, "2026-06-20"),
                tx("Restaurant payment", 45.75, "2026-06-15")
        ));

        seedUser("admin1", "admin1@bank.com", List.of(
                tx("Freelance payment", 1200.00, "2026-07-03"),
                tx("Book purchase", 35.99, "2026-07-01"),
                tx("Fuel refill", 60.00, "2026-06-27"),
                tx("Mobile recharge", 25.00, "2026-06-22")
        ));

        seedUser("admin2", "admin2@bank.com", List.of(
                tx("Bonus credit", 800.00, "2026-07-02"),
                tx("Medical bill", 150.00, "2026-06-30"),
                tx("Coffee shop", 12.50, "2026-06-26"),
                tx("Bus pass", 45.00, "2026-06-18")
        ));

        seedUser("admin3", "admin3@bank.com", List.of(
                tx("Rent payment", 950.00, "2026-07-01"),
                tx("Internet bill", 55.00, "2026-06-29"),
                tx("Gym membership", 40.00, "2026-06-24"),
                tx("Pharmacy", 28.75, "2026-06-19")
        ));

        seedUser("admin4", "admin4@bank.com", List.of(
                tx("Client invoice", 2100.00, "2026-07-04"),
                tx("Office supplies", 78.25, "2026-06-28"),
                tx("Parking fee", 15.00, "2026-06-23"),
                tx("Team lunch", 92.00, "2026-06-17")
        ));

        seedUser("admin5", "admin5@bank.com", List.of(
                tx("Stock dividend", 640.00, "2026-07-02"),
                tx("Home repair", 220.00, "2026-06-27"),
                tx("Streaming subscription", 14.99, "2026-06-21"),
                tx("Gift purchase", 65.00, "2026-06-16")
        ));
    }

    private void seedUser(String username, String email, List<TransactionSeed> seeds) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userService.register(username, "admin123", email));

        if (!transactionRepository.findByUserOrderByTransactionDateDesc(user).isEmpty()) {
            return;
        }

        for (TransactionSeed seed : seeds) {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setDescription(seed.description());
            transaction.setAmount(seed.amount());
            transaction.setTransactionDate(seed.date());
            transactionRepository.save(transaction);
        }
    }

    private TransactionSeed tx(String description, double amount, String date) {
        return new TransactionSeed(description, amount, LocalDate.parse(date));
    }

    private record TransactionSeed(String description, double amount, LocalDate date) {
    }
}
