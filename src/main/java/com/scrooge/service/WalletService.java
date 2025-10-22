package com.scrooge.service;

import com.scrooge.config.client.exchangerate.ExchangeRateResponse;
import com.scrooge.exception.*;
import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.model.enums.TransactionType;
import com.scrooge.repository.WalletRepository;
import com.scrooge.web.dto.wallet.WalletCreateRequest;
import com.scrooge.web.mapper.WalletMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.scrooge.exception.ExceptionMessages.*;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserService userService;
    private final TransactionService transactionService;
    private final AuditLogService auditLogService;
    private final PocketService pocketService;
    private final ExchangeRateService exchangeRateService;

    public WalletService(WalletRepository walletRepository,
                         UserService userService,
                         TransactionService transactionService,
                         AuditLogService auditLogService,
                         PocketService pocketService, ExchangeRateService exchangeRateService) {

        this.walletRepository = walletRepository;
        this.userService = userService;
        this.transactionService = transactionService;
        this.auditLogService = auditLogService;
        this.pocketService = pocketService;
        this.exchangeRateService = exchangeRateService;
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

    public void setMainWallet(UUID walletId, UUID userId) {

        User user = userService.getUserById(userId);
        Wallet wallet = getWalletById(walletId);

        String message = "Main wallet changed from %s to %s".formatted(user.getWallets()
                .stream()
                .filter(Wallet::isMainWallet)
                .findFirst(), wallet.getName());

        user.getWallets().forEach(w -> w.setMainWallet(false));

        wallet.setMainWallet(true);

        auditLogService.log("SET_MAIN_WALLET", message, user);

        walletRepository.save(wallet);
    }

    public Wallet deposit(UUID walletId, UUID userId, BigDecimal amount) {

        User user = userService.getUserById(userId);

        Wallet wallet = getWalletById(walletId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            transactionService.setTransactionToWallet(wallet, amount, TransactionType.DEPOSIT, false);
            throw new InvalidInternalTransferAmountException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.DEPOSIT, true);

        wallet.setBalance(wallet.getBalance().add(amount));

        String logMessage = String.format("%.2f %s deposited to wallet %s", amount, wallet.getCurrency(), wallet.getName());
        auditLogService.log("DEPOSIT", logMessage, user);

        return walletRepository.save(wallet);
    }

    public void transferMoneyBetweenWallets(UUID senderWallet, UUID recipientWallet, BigDecimal amount) {

        Wallet wallet = getWalletById(senderWallet);
        Wallet recipient = getWalletById(recipientWallet);

        amountAdnWalletValidation(amount, wallet);

        wallet.setBalance(wallet.getBalance().subtract(amount));

        if (!wallet.getCurrency().equals(recipient.getCurrency())) {
            amount = convertAmount(wallet.getCurrency(), recipient.getCurrency(), amount);
        }

        recipient.setBalance(recipient.getBalance().add(amount));

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.INTERNAL_TRANSACTION, true);
        transactionService.setTransactionToWallet(recipient, amount, TransactionType.DEPOSIT, true);

        String message = "Transfer %.2f %s from wallet %s to wallet %s".formatted(amount, wallet.getCurrency(), wallet.getName(), recipient.getName());

        auditLogService.log("INTERNAL_TRANSFER", message, wallet.getUser());

        walletRepository.save(wallet);
        walletRepository.save(recipient);
    }

    public void transferMoneyByEmail(UUID senderWalletId, BigDecimal amount, User user, String receiverEmail) {

        Wallet senderWallet = getWalletById(senderWalletId);
        User receiverUser;

        try {
            receiverUser = userService.getUserByEmail(receiverEmail);
        } catch (InvalidUserEmailException e) {
            transactionService.setTransactionToWallet(senderWallet, amount, TransactionType.TRANSFER, false);
            auditLogService.log("TRANSFER", "Transaction fail due to invalid email address - %s".formatted(receiverEmail), user);
            throw new InvalidUserEmailException(INVALID_USER_EMAIL.formatted(receiverEmail));
        }

        if (receiverUser.getWallets().isEmpty()) {
            transactionService.setTransactionToWallet(senderWallet, amount, TransactionType.TRANSFER, false);
            auditLogService.log("TRANSFER", "%s cannot accept money right now.".formatted(receiverEmail), user);
            throw new ReceiverHasNoWalletException("%s cannot accept money right now.".formatted(receiverEmail));
        }

        Wallet receiverWallet = receiverUser
                .getWallets()
                .stream()
                .filter(Wallet::isMainWallet).
                findFirst().get();

        amountAndWalletAmountValidation(amount, senderWallet);

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));

        if (!senderWallet.getCurrency().equals(receiverWallet.getCurrency())) {
            amount = convertAmount(senderWallet.getCurrency(), receiverWallet.getCurrency(), amount);
        }

        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        String senderMessage = "%.2f %s amount sent to user %s".formatted(amount, receiverWallet.getCurrency(), receiverEmail);
        String receiverMessage = "%.2f %s amount received from user %s".formatted(amount, receiverWallet.getCurrency(), user.getEmail());
        auditLogService.log("TRANSFER", senderMessage, user);
        auditLogService.log("TRANSFER", receiverMessage, receiverUser);

        transactionService.setTransactionToWallet(senderWallet, amount, TransactionType.TRANSFER, true);
        transactionService.setTransactionToWallet(receiverWallet, amount, TransactionType.TRANSFER, true);

        walletRepository.save(receiverWallet);
        walletRepository.save(senderWallet);
    }

    @Transactional
    public void transferMoneyToPocket(UUID walletId, BigDecimal amount, User user, UUID pocketId) {

        Wallet wallet = getWalletById(walletId);
        Pocket pocket = pocketService.getPocketById(pocketId);

        amountAndWalletAmountValidation(amount, wallet);


        wallet.setBalance(wallet.getBalance().subtract(amount));

        if (!wallet.getCurrency().equals(pocket.getCurrency())) {
            amount = convertAmount(wallet.getCurrency(), pocket.getCurrency(), amount);
        }

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.WITHDRAWAL, true);

        String message = "Deposit %.2f %s from wallet %s to pocket %s"
                .formatted(amount, pocket.getCurrency(), wallet.getName(), pocket.getName());
        auditLogService.log("DEPOSIT_TO_POCKET", message, user);

        pocketService.deposit(amount, pocketId, user.getId());
        walletRepository.save(wallet);
    }

    @Transactional
    public void depositFromPocket(UUID walletId, UUID pocketId, UUID userId) {

        Wallet wallet = getWalletById(walletId);
        Pocket pocket = pocketService.getPocketById(pocketId);
        User user = userService.getUserById(userId);

        BigDecimal amount = pocket.getBalance();

        if (!wallet.getCurrency().equals(pocket.getCurrency())) {
            amount = convertAmount(wallet.getCurrency(), pocket.getCurrency(), amount);
        }

        transactionService.setTransactionToWallet(wallet, amount, TransactionType.INTERNAL_TRANSACTION, true);
        String messageLog = "Internal transaction from pocket %s with %.2f %s amount to wallet %s. Pocket with name %s automatically deleted after transaction."
                .formatted(pocket.getName(), amount, pocket.getCurrency(), wallet.getName(), pocket.getName());
        auditLogService.log("TRANSACTION_FROM_POCKET", messageLog, user);

        wallet.setBalance(wallet.getBalance().add(amount));

        user.getPockets().remove(pocket);

        pocketService.delete(pocket);
    }

    @Transactional
    public void deleteWallet(UUID walletId, User user) {

        Wallet currentWallet = getWalletById(walletId);

        if (currentWallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            auditLogService.log("UNSUPPORTED_OPERATION", "Attempt to delete wallet denied: The wallet contains insufficient balance. Please withdraw funds before deletion.", user);
            throw new WalletAmountMustBeZeroException(WALLET_AMOUNT_MUST_BE_ZERO);
        }

        user.getWallets().removeIf(wallet -> wallet.getId().equals(walletId));

        if (currentWallet.isMainWallet() && user.getWallets().size() >= 1) {

            Wallet newMainWallet = user.getWallets()
                    .stream()
                    .sorted(Comparator.comparing(Wallet::getCreatedOn))
                    .toList()
                    .get(0);

            newMainWallet.setMainWallet(true);
            walletRepository.save(newMainWallet);
        }

        walletRepository.delete(currentWallet);

        String message = "%s wallet successfully deleted".formatted(currentWallet.getName());
        auditLogService.log("WALLET_DELETED", message, user);
    }

    protected void amountAndWalletAmountValidation(BigDecimal amount, Wallet senderWallet) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            transactionService.setTransactionToWallet(senderWallet, amount, TransactionType.INTERNAL_TRANSACTION, false);
            auditLogService.log("TRANSACTION", "Transaction fail due to negative amount.", senderWallet.getUser());
            throw new InsufficientAmountException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            transactionService.setTransactionToWallet(senderWallet, amount, TransactionType.INTERNAL_TRANSACTION, false);
            auditLogService.log("TRANSACTION", "Transaction fail due to insufficient funds.", senderWallet.getUser());
            throw new InsufficientTransferAmountException(INSUFFICIENT_BALANCE);
        }
    }

    protected void amountAdnWalletValidation(BigDecimal amount, Wallet wallet) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            transactionService.setTransactionToWallet(wallet, amount, TransactionType.INTERNAL_TRANSACTION, false);
            auditLogService.log("INTERNAL_TRANSACTION", "Internal transaction fail due to negative amount.", wallet.getUser());
            throw new InternalTransactionException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            transactionService.setTransactionToWallet(wallet, amount, TransactionType.INTERNAL_TRANSACTION, false);
            auditLogService.log("INTERNAL_TRANSACTION", "Internal transaction fail due to insufficient funds.", wallet.getUser());
            throw new InternalTransactionException(INSUFFICIENT_BALANCE);
        }
    }

    protected BigDecimal convertAmount(Currency sender, Currency receiver, BigDecimal amount) {

        ExchangeRateResponse response = exchangeRateService.getExchangeRate(sender);

        BigDecimal rate = new BigDecimal(String.valueOf(response.getConversionRates().get(String.valueOf(receiver))));

        amount = amount.multiply(rate);

        return amount;
    }

    protected void checkIfWalletNameExists(User user, String walletName) {

        boolean walletExists = user.getWallets().stream()
                .anyMatch(w -> w.getName().equals(walletName));

        if (walletExists) {
            throw new ResourceAlreadyExistException("Wallet name [%s] already exists".formatted(walletName));
        }
    }
}
