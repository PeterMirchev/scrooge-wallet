package com.scrooge.service;

import com.scrooge.exception.ResourceAlreadyExistException;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.Pocket;
import com.scrooge.model.User;
import com.scrooge.model.Wallet;
import com.scrooge.web.mapper.PocketMapper;
import com.scrooge.repository.PocketRepository;
import com.scrooge.web.dto.PocketCreateRequest;
import com.scrooge.web.dto.PocketUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.*;

@Service
public class PocketService {

    private final PocketRepository pocketRepository;
    private final UserService userService;
    private final WalletService walletService;
    private final AuditLogService auditLogService;

    public PocketService(PocketRepository pocketRepository, UserService userService, WalletService walletService, AuditLogService auditLogService) {

        this.pocketRepository = pocketRepository;
        this.userService = userService;
        this.walletService = walletService;
        this.auditLogService = auditLogService;
    }

    public Pocket getPocketById(UUID pocketId) {

        return pocketRepository.findById(pocketId)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_POCKET_ID.formatted(pocketId)));
    }

    public Pocket create(PocketCreateRequest request, UUID userId) {

        User user = userService.getUserById(userId);

        checkIfPocketNameExists(user, request.getName());

        Pocket pocket = PocketMapper.mapToPocket(request);
        pocket.setUser(user);
        pocket = pocketRepository.save(pocket);

        String logMessage = String.format("Pocket with name %s created.", pocket.getName());
        auditLogService.log("CREATE_POCKET", logMessage, user);

        return pocket;
    }

    public Pocket update(PocketUpdateRequest request, UUID pocketId, UUID userId) {

        User user = userService.getUserById(userId);

        checkIfPocketNameExists(user, request.getName());

        Pocket pocket = getPocketById(pocketId);

        updatePocket(request, pocket);

        String logMessage = String.format("Pocket with name %s updated.", pocket.getName());
        auditLogService.log("UPDATE_POCKET", logMessage, user);

        return pocketRepository.save(pocket);
    }

    public Pocket deposit(BigDecimal amount, UUID pocketId, UUID userId) {

        User user = userService.getUserById(userId);

        Pocket pocket = getPocketById(pocketId);

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(AMOUNT_MUST_BE_GREATER_THAN_ZERO);
        }

        pocket.setBalance(pocket.getBalance().add(amount));

        String logMessage = String.format("Deposit of %s to Pocket %s.", pocket.getName(), amount);
        auditLogService.log("DEPOSIT", logMessage, user);

        return pocketRepository.save(pocket);
    }

    @Transactional
    public Pocket withdraw(UUID pocketId, UUID walletId, UUID userId) {

        User user = userService.getUserById(userId);

        Pocket pocket = getPocketById(pocketId);

        Wallet wallet = walletService.getWalletById(walletId);

        if (!wallet.getCurrency().equals(pocket.getCurrency())) {
            throw new IllegalArgumentException(CURRENCIES_NOT_EQUAL);
        }

        walletService.increaseWalletBalance(wallet.getId(), pocket.getBalance());
        pocket.setBalance(BigDecimal.ZERO);

        String logMessage = String.format("Withdrawal from Pocket %s.", pocket.getName());
        auditLogService.log("WITHDRAWAL", logMessage, user);

        return pocketRepository.save(pocket);
    }

    void delete(UUID pocketId) {

        Pocket pocket = getPocketById(pocketId);

        User user = userService.getUserById(pocket.getUser().getId());

        pocketRepository.delete(pocket);

        auditLogService.log("DELETED_POCKET", "Pocket successfully deleted", user);
    }

    protected static void updatePocket(PocketUpdateRequest request, Pocket pocket) {

        pocket.setName(request.getName());
        pocket.setGoalDescription(request.getGoalDescription());
        pocket.setTargetAmount(request.getTargetAmount());
    }

    protected static void checkIfPocketNameExists(User user, String request) {

        boolean pocketExist = user.getPockets()
                .stream().anyMatch(p -> p.getName().equals(request));

        if (pocketExist) {
            throw new ResourceAlreadyExistException("Pocket name %s already exist.".formatted(request));
        }
    }
}
