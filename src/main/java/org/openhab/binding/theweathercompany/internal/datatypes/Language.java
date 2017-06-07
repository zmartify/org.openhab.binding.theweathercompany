package org.openhab.binding.theweathercompany.internal.datatypes;

public enum Language {
    ENGLISH("en"),
    FRENCH("fr"),
    GERMAN("de"),
    SPANISH("es"),
    ITALIAN("it"),
    ARABIC("ar"),
    BENGALI("bn"),
    CATALAN("ca"),
    CZECH("cs"),
    DANISH("da"),
    GREEK("el"),
    PERSIAN("fa"),
    FINNISH("fi"),
    HEBREW("he"),
    HINDI("hi"),
    CROATIAN("hr"),
    HUNGARIAN("hu"),
    INDONESIAN("in"),
    HEBREW_IW("iw"),
    JAPANESE("ja"),
    KAZAKH("kk"),
    KOREAN("ko"),
    MALAY("ms"),
    DUTCH("nl");

    private final String code;

    private Language(String code) {
        this.code = code;
    }

    public String get() {
        return code;
    }
}
