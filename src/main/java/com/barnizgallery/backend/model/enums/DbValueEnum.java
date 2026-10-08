package com.barnizgallery.backend.model.enums;

/**
 * Implemented by every enum whose values are stored in the database
 * with a Spanish text (for example {@code "exhibida"}).
 */
public interface DbValueEnum {

    /** Text stored in the database column. */
    String getDbValue();
}
