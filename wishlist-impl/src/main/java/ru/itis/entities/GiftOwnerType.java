package ru.itis.entities;

public enum GiftOwnerType {
    WISHLIST("wishlist"),
    COMPILATION("compilation");

    private final String value;

    GiftOwnerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
