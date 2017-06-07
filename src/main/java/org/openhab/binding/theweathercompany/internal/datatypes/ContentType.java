package org.openhab.binding.theweathercompany.internal.datatypes;

// Enum for content-type header
public enum ContentType {
    text("text/plain"),
    json("application/json"),
    xml("application/xml"),
    bin("application/octet-stream");

    ContentType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    private String mType;

}