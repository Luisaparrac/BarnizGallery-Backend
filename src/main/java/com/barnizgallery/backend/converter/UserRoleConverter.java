package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.UserRole;

import jakarta.persistence.Converter;

/**
 * Converts {@link UserRole} to its Spanish database value and back.
 */
@Converter
public class UserRoleConverter extends DbValueEnumConverter<UserRole> {

    public UserRoleConverter() {
        super(UserRole.class);
    }
}
