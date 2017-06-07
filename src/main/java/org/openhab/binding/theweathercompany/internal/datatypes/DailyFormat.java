package org.openhab.binding.theweathercompany.internal.datatypes;

public enum DailyFormat {
    THREE_DAY("3day"),
    FIVE_DAY("5day"),
    SEVEN_DAY("7day"),
    TEN_DAY("10day");

    private final String format;

    private DailyFormat(String format) {
        this.format = format;
    }

    public String get() {
        return format;
    }
}
