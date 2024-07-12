package tech.siloxa.clipboard.domain.enumeration;

import java.util.Locale;

public enum Language {

    EN(Locale.ENGLISH);

    final Locale locale;

    Language(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }
}
