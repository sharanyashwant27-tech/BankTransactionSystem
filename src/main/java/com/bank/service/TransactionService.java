package com.bank.service;

import com.bank.dto.AccountSummary;
import com.bank.entity.Transaction;
import com.bank.entity.User;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsForUser(String username) {
        User user = requireUser(username);
        return transactionRepository.findByUserOrderByTransactionDateDesc(user);
    }

    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionForUser(String username, Long id) {
        User user = requireUser(username);
        return transactionRepository.findByIdAndUser(id, user);
    }

    @Transactional(readOnly = true)
    public AccountSummary getAccountSummary(String username) {
        User user = requireUser(username);
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);

        double openingBalance = user.getOpeningBalance() != null ? user.getOpeningBalance() : 10000.0;
        double totalSpending = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        double leftAmount = openingBalance - totalSpending;

        return new AccountSummary(
                user.getUsername(),
                openingBalance,
                totalSpending,
                leftAmount,
                transactions.size()
        );
    }

    @Transactional
    public Transaction createTransaction(String username, String description, Double amount, LocalDate transactionDate) {
        validateAmount(amount);
        User user = requireUser(username);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setDescription(description.trim());
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate != null ? transactionDate : LocalDate.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(String username, Long id, String description, Double amount, LocalDate transactionDate) {
        validateAmount(amount);
        Transaction transaction = requireOwnedTransaction(username, id);

        transaction.setDescription(description.trim());
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(String username, Long id) {
        Transaction transaction = requireOwnedTransaction(username, id);
        transactionRepository.delete(transaction);
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private Transaction requireOwnedTransaction(String username, Long id) {
        User user = requireUser(username);
        return transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found or access denied"));
    }

    private void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
