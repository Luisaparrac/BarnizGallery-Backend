package com.barnizgallery.backend.enums;

import java.util.Locale;

/**
 * Stable codes of the API errors. The frontend decides by the {@code code} of the error body (for example
 * {@code artwork_not_found}); the {@code message} is only text for people. Each code has its text in the
 * {@code messages_*.properties} bundles under the key {@code error.<code>}.
 */
public enum ErrorCode {

    // Generic request errors
    RESOURCE_NOT_FOUND,
    VALIDATION_FAILED,
    MALFORMED_REQUEST,
    INVALID_PARAMETER,
    METHOD_NOT_ALLOWED,
    DATA_CONFLICT,
    INTERNAL_ERROR,

    // Authentication and authorization
    UNAUTHORIZED,
    ACCESS_DENIED,
    NOT_OWNER,
    TOO_MANY_LOGIN_ATTEMPTS,
    EMAIL_ALREADY_REGISTERED,
    INVALID_CREDENTIALS,
    ACCOUNT_SUSPENDED,
    INVALID_REFRESH_TOKEN,
    INVALID_EMAIL_TOKEN,
    PASSWORD_TOO_LONG,

    // Missing resources
    ARTWORK_NOT_FOUND,
    AUCTION_NOT_FOUND,
    MASTER_NOT_FOUND,
    NOTIFICATION_NOT_FOUND,
    PHOTO_NOT_FOUND,
    STORE_NOT_FOUND,
    STORE_TEMPLATE_NOT_FOUND,
    TASTE_PROFILE_NOT_FOUND,
    USER_NOT_FOUND,

    // Optional integrations
    AI_DISABLED,
    AI_STRATEGY_DISABLED,
    PRICE_SUGGESTION_UNAVAILABLE,
    STORAGE_DISABLED,
    STORAGE_UNAVAILABLE,

    // Image uploads
    IMAGE_REQUIRED,
    IMAGE_TOO_LARGE,
    IMAGE_TYPE_NOT_ALLOWED,

    // Business settings
    SETTING_NOT_CONFIGURED,
    SETTING_INVALID,

    // Payments
    PAYMENT_CURRENCY_NOT_SUPPORTED,
    PAYMENT_GATEWAY_DISABLED,
    PAYMENT_GATEWAY_UNAVAILABLE,
    PAYMENT_GATEWAY_NOT_FOUND,
    INVALID_WEBHOOK_SIGNATURE,
    WEBHOOK_NOT_SUPPORTED,

    // Catalogs and recommendations
    CATALOG_NOT_FOUND,
    CATALOG_ENTRY_NOT_FOUND,
    CATALOG_CODE_REQUIRED,
    CATALOG_CODE_EXISTS,
    AVATAR_IMAGE_REQUIRED,
    BUDGET_RANGE_INVALID,
    TEMPLATE_JSON_INVALID,
    UNKNOWN_COLOR_CODES,
    UNKNOWN_SHAPE_CODES,
    UNKNOWN_STYLE_CODES,
    UNKNOWN_MOTIF_CODES,
    UNKNOWN_BUDGET_RANGE,
    UNKNOWN_AVATAR_CODE,
    UNKNOWN_STRATEGY,
    UNKNOWN_RECOMMENDATION_TARGET,

    // Artisan verification
    ARTISAN_PROFILE_NOT_FOUND,
    VERIFICATION_STEP_NOT_FOUND,
    STEP_WITHOUT_EVIDENCE,
    STEP_ALREADY_APPROVED,
    REVIEW_DECISION_INVALID,
    VERIFICATION_INCOMPLETE,

    // Stores and interactions
    MASTER_ALREADY_HAS_STORE,
    DURATION_NEGATIVE,
    DURATION_REQUIRED,
    INTERACTION_RECORDED_BY_SERVER,
    INTERACTION_TARGET_INVALID,

    // Auctions and bids
    ARTWORK_REQUIRED,
    ARTWORK_NOT_EXHIBITED,
    ARTWORK_ALREADY_IN_AUCTION,
    CURRENCY_REQUIRED,
    AUCTION_DATES_REQUIRED,
    AUCTION_END_BEFORE_START,
    AUCTION_END_IN_PAST,
    BASE_PRICE_NEGATIVE,
    BASE_PRICE_REQUIRED,
    BASE_PRICE_NOT_SUGGESTED,
    AUCTION_ALREADY_ACTIVE,
    AUCTION_NOT_STARTED,
    AUCTION_FINISHED,
    AUCTION_CANCELLED,
    SCHEDULED_AUCTION_CANNOT_FINISH,
    AUCTION_WITHOUT_CURRENCY,
    BID_CURRENCY_MISMATCH,
    BID_TOO_LOW,
    BID_RATE_LIMITED;

    /** Code sent to the frontend, for example {@code artwork_not_found}. */
    public String code() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Key of the text in the message bundles. */
    public String messageKey() {
        return "error." + code();
    }
}
