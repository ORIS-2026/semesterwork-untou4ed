package ru.itis.entities;

public enum GroupMemberStatus {
    MEMBER("member"),
    ADMIN("admin"),
    CREATOR("creator");

    private final String value;

    GroupMemberStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
