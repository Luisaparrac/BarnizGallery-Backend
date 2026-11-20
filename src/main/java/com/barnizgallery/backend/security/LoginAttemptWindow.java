package com.barnizgallery.backend.security;

import java.time.Instant;

/**
 * Failed logins of one email and IP since {@code startedAt}.
 */
record LoginAttemptWindow(int failures, Instant startedAt) {

    LoginAttemptWindow addFailure() {
        return new LoginAttemptWindow(failures + 1, startedAt);
    }
}
