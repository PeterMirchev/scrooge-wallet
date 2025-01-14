package com.scrooge.repository;

import com.scrooge.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Query("""
        SELECT w FROM Wallet w
        WHERE w.user.id = :userId
    """)
    List<Wallet> findAllByUserId(UUID userId);
}
