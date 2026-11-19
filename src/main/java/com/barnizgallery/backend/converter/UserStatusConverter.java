package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.UserStatus;

import jakarta.persistence.Converter;

/**
 * Converts {@link UserStatus} to its Spanish database value and back.
 */
@Converter
public class UserStatusConverter extends DbValueEnumConverter<UserStatus> {

    public UserStatusConverter() {
        super(UserStatus.class);
    }
}
