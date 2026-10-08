package com.barnizgallery.backend.patterns.strategy;

import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.model.enums.Language;

/**
 * Picks the Spanish or English text according to the visitor's preferred language.
 */
final class LocalizedText {

    private LocalizedText() {
    }

    static String pick(Visitor visitor, String spanish, String english) {
        return visitor.getPreferredLanguage() == Language.EN ? english : spanish;
    }
}
