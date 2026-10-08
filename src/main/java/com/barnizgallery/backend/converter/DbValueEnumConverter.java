package com.barnizgallery.backend.converter;

import com.barnizgallery.backend.enums.DbValueEnum;

import jakarta.persistence.AttributeConverter;

/**
 * Base JPA converter between an English Java enum and the Spanish text
 * stored in the database. Each concrete converter only passes its enum class.
 *
 * @param <E> enum type
 */
public abstract class DbValueEnumConverter<E extends Enum<E> & DbValueEnum>
        implements AttributeConverter<E, String> {

    private final Class<E> enumType;

    protected DbValueEnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        for (E constant : enumType.getEnumConstants()) {
            if (constant.getDbValue().equals(dbData)) {
                return constant;
            }
        }
        throw new IllegalArgumentException(
                "Unknown value '" + dbData + "' for " + enumType.getSimpleName());
    }
}
