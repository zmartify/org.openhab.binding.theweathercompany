package org.openhab.binding.theweathercompany.internal.datatypes;

public enum Units {
    ENGLISH("e", "english"),
    METRIC("m", "metric"),
    HYBRID("h", "hybrid");

    private final String code;
    private final String description;

    private Units(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Units valueOfString(String code) {
        for (Units c : Units.values()) {
            if (code.equalsIgnoreCase(c.code)) {
                return c;
            }
        }
        // Set ENGLISH as default
        return ENGLISH;
    }

    public String get() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}
