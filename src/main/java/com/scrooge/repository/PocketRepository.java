package com.scrooge.repository;

import com.scrooge.model.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PocketRepository extends JpaRepository<Pocket, UUID> {
}
