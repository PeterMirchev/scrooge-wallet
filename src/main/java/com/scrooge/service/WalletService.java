package com.scrooge.service;

import com.scrooge.exception.InsufficientAmountException;
import com.scrooge.exception.ResourceAlreadyExistException;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.model.enums.TransactionType;
import com.scrooge.repository.WalletRepository;
import com.scrooge.web.dto.WalletCreateRequest;
import com.scrooge.web.dto.WalletUpdateRequest;
import com.scrooge.web.mapper.WalletMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.*;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AuditLogService auditLogService;

    public WalletService(WalletRepository walletRepository, UserService userService, TransactionService transactionService, AuditLogService auditLogService) {

        this.walletRepository = walletRepository;
        this.userService = userService;
        this.transactionService = transactionService;
        this.auditLogService = auditLogService;
    }


    public Wallet getWalletById(UUID walletId) {

        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_WALLET_ID.formatted(walletId)));
    }

    public List<Wallet> getAllWalletsByUserId(UUID userId) {

        return walletRepository.findAllByUserId(userId);
    }

    public Wallet create(WalletCreateRequest request, UUID userId) {

        User user = userService.getUserById(userId);

        checkIfWalletNameExists(user, request.getName());

        Wallet wallet = WalletMapper.mapToWallet(request);

        if (user.getWallets().size() > 0) {
            wallet.setMainWallet(false);
        } else {
            wallet.setMainWallet(true);
        }

        wallet.setUser(user);

        String logMessage = String.format("Wallet with name %s created.", wallet.getName());
        auditLogService.log("CREATE_WALLET", logMessage, user);

        return walletRepository.save(wallet);
    }

    public Wallet update(WalletUpdateRequest request, UUID walletId, UUID userId) {

        User user = userService.getUserById(userId);

        checkIfWalletNameExists(user, request.getName());

        Wallet wallet = getWalletById(walletId);

        wallet.setName(request.getName());
        wallet.setUpdatedOn(LocalDateTime.now());

        String logMessage = String.format("Wallet name updated to %s", request.getName());
        auditLogService.log("WALLET_UPDATED", logMessage, user);

        return walletRepository.save(wallet);
    }

    public Wallet setMainWallet(UUID walletId, UUID userId) {

        User user = userService.getUserById(userId);
        Wallet wallet = getWalletById(walletId);

        user.getWallets().forEach(w -> w.setMainWallet(false));

        wallet.setMainWallet(true);

        return walletRepository.save(wallet);
    }

    public BigDecimal getWalletBalance(UUID walletId) {

        Wallet wallet = getWalletById(walletId);

        return wallet.getBalance();
    }

    public Wallet deposit(UUID walletId, UUID userId, BigDecimal amount) {

        User user = userService.getUserById(userId);

        Wallet wallet = getWalletById(walletId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InsufficientAmountException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.DEPOSIT);

        wallet.setBalance(wallet.getBalance().add(amount));

        String logMessage = String.format("%.2f %s deposited to wallet %s",amount, wallet.getCurrency(), wallet.getName());
        auditLogService.log("DEPOSIT", logMessage, user);

        return walletRepository.save(wallet);
    }

    public Wallet withdraw(UUID walletId, UUID userId, BigDecimal amount) {

        User user = userService.getUserById(userId);

        Wallet wallet = getWalletById(walletId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(INSUFFICIENT_BALANCE);
        }

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.WITHDRAWAL);

        wallet.setBalance(wallet.getBalance().subtract(amount));

        String logMessage = String.format("Withdrawal of %s from wallet %s", amount, wallet.getName());
        auditLogService.log("WITHDRAWAL", logMessage, user);

        return walletRepository.save(wallet);
    }

    public void transferMoneyBetweenWallets(UUID senderWallet, UUID recipientWallet, BigDecimal amount) {

        Wallet wallet = getWalletById(senderWallet);
        Wallet recipient = getWalletById(recipientWallet);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(INSUFFICIENT_BALANCE);
        }



        wallet.setBalance(wallet.getBalance().subtract(amount));
        recipient.setBalance(recipient.getBalance().add(amount));

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.WITHDRAWAL);
        transactionService.setTransactionToWallet(recipient, amount, TransactionType.DEPOSIT);

        String message  = "Transfer %.2f %s from wallet %s to wallet %s".formatted(amount, wallet.getCurrency(), wallet.getName(), recipient.getName());

        auditLogService.log("INTERNAL_TRANSFER", message, wallet.getUser());

        walletRepository.save(wallet);
        walletRepository.save(recipient);
    }


    public void increaseWalletBalance(UUID walletId, BigDecimal amount) {

        Wallet wallet = getWalletById(walletId);

        wallet.setBalance(wallet.getBalance().add(amount));

        walletRepository.save(wallet);
    }

    protected void checkIfWalletNameExists(User user, String walletName) {

        boolean walletExists = user.getWallets().stream()
                .anyMatch(w -> w.getName().equals(walletName));

        if (walletExists) {
            throw new ResourceAlreadyExistException("Wallet name [%s] already exists".formatted(walletName));
        }
    }
}
