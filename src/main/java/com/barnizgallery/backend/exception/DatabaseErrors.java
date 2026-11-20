package com.barnizgallery.backend.exception;

import java.sql.SQLException;

/**
 * Recognizes the errors raised by the business rules written in the database (PL/pgSQL {@code RAISE EXCEPTION},
 * SQLSTATE {@code P0001}), so services can answer them with a clear 409 instead of a 500.
 */
public final class DatabaseErrors {

    private static final String RAISE_EXCEPTION = "P0001";

    private DatabaseErrors() {
    }

    /** True when some cause of the exception is a rule raised by a database function or trigger. */
    public static boolean isRuleViolation(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && RAISE_EXCEPTION.equals(sql.getSQLState())) {
                return true;
            }
        }
        return false;
    }
}
