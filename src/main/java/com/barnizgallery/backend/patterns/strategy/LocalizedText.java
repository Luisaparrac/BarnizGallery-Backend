package com.barnizgallery.backend.patterns.strategy;

import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.Language;

/**
 * Picks the Spanish or English text according to the user's preferred language.
 */
final class LocalizedText {

    private LocalizedText() {
    }

    static String pick(User user, String spanish, String english) {
        return user.getPreferredLanguage() == Language.EN ? english : spanish;
    }
}
