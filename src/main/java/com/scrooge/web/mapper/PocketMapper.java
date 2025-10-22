package com.scrooge.web.mapper;

import com.scrooge.model.Pocket;
import com.scrooge.web.dto.pocket.PocketCreateRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PocketMapper {
    public static Pocket mapToPocket(PocketCreateRequest request) {

        return Pocket.builder()
                .name(request.getName())
                .goalDescription(request.getGoalDescription())
                .targetAmount(request.getTargetAmount())
                .balance(BigDecimal.ZERO)
                .currency(request.getCurrency())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }
}
