package com.scrooge.web.mapper;

import com.scrooge.model.User;
import com.scrooge.model.enums.Country;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;

import java.time.LocalDateTime;

public class UserMapper {


    public static User mapToUser(UserCreateRequest request) {

        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static void mapUserUpdateToUser(User user, UserUpdateRequest request) {

        if (request.getFirstName().length() > 0) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName().length() > 0) {
            user.setLastName(request.getLastName());
        }

        if (request.getCountry().length() > 0) {
            user.setCountry(Country.valueOf(request.getCountry()));
        }

        if (request.getPhoneNumber().length() > 0) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user.setUpdatedOn(LocalDateTime.now());
    }
}
