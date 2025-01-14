package com.scrooge.web.mapper;

import com.scrooge.model.User;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;

import java.time.LocalDateTime;

public class UserMapper {


    public static User mapToUser(UserCreateRequest request) {

        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static void mapUserUpdateToUser(User user, UserUpdateRequest request) {

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setUpdatedOn(LocalDateTime.now());
    }
}
