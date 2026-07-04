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
    public List<String> getTransferRecipients(String currentUsername) {
        return userRepository.findAll().stream()
                .map(User::getUsername)
                .filter(name -> name != null && !name.equalsIgnoreCase(currentUsername))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountSummary getAccountSummary(String username) {
        User user = requireUser(username);
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user);

        double openingBalance = user.getOpeningBalance() != null ? user.getOpeningBalance() : 10000.0;
        double totalSpending = transactions.stream()
                .filter(t -> !t.isCredit())
                .mapToDouble(this::amountOrZero)
                .sum();
        double totalCredits = transactions.stream()
                .filter(Transaction::isCredit)
                .mapToDouble(this::amountOrZero)
                .sum();
        double leftAmount = openingBalance - totalSpending + totalCredits;

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
        transaction.setType(Transaction.TYPE_DEBIT);
        transaction.setTransactionDate(transactionDate != null ? transactionDate : LocalDate.now());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void transfer(String fromUsername, String recipientUsername, Double amount, String note) {
        validateAmount(amount);

        if (recipientUsername == null || recipientUsername.isBlank()) {
            throw new IllegalArgumentException("Recipient username is required");
        }

        String recipient = recipientUsername.trim();
        if (fromUsername.equalsIgnoreCase(recipient)) {
            throw new IllegalArgumentException("You cannot transfer money to yourself");
        }

        User sender = requireUser(fromUsername);
        User receiver = userRepository.findByUsername(recipient)
                .orElseThrow(() -> new IllegalArgumentException("Recipient user not found: " + recipient));

        AccountSummary senderSummary = getAccountSummary(fromUsername);
        if (senderSummary.getLeftAmount() < amount) {
            throw new IllegalArgumentException("Insufficient balance. Available: $"
                    + String.format("%.2f", senderSummary.getLeftAmount()));
        }

        String cleanNote = note != null ? note.trim() : "";
        LocalDate today = LocalDate.now();

        String debitDescription = cleanNote.isEmpty()
                ? "Transfer to " + receiver.getUsername()
                : "Transfer to " + receiver.getUsername() + " — " + cleanNote;
        String creditDescription = cleanNote.isEmpty()
                ? "Transfer from " + sender.getUsername()
                : "Transfer from " + sender.getUsername() + " — " + cleanNote;

        Transaction debit = new Transaction();
        debit.setUser(sender);
        debit.setDescription(debitDescription);
        debit.setAmount(amount);
        debit.setType(Transaction.TYPE_DEBIT);
        debit.setTransactionDate(today);
        transactionRepository.save(debit);

        Transaction credit = new Transaction();
        credit.setUser(receiver);
        credit.setDescription(creditDescription);
        credit.setAmount(amount);
        credit.setType(Transaction.TYPE_CREDIT);
        credit.setTransactionDate(today);
        transactionRepository.save(credit);
    }

    @Transactional
    public Transaction updateTransaction(String username, Long id, String description, Double amount, LocalDate transactionDate) {
        validateAmount(amount);
        Transaction transaction = requireOwnedTransaction(username, id);

        transaction.setDescription(description.trim());
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);
        if (transaction.getType() == null || transaction.getType().isBlank()) {
            transaction.setType(Transaction.TYPE_DEBIT);
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteTransaction(String username, Long id) {
        Transaction transaction = requireOwnedTransaction(username, id);
        if (transaction.isCredit() && AdminService.isAdminIssuedCredit(transaction.getDescription())) {
            throw new IllegalArgumentException("Administrative credits cannot be deleted");
        }
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

    private double amountOrZero(Transaction transaction) {
        Double amount = transaction.getAmount();
        return amount != null ? amount : 0.0;
    }
}
