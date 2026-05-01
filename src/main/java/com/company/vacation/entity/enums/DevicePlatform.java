package com.company.vacation.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DevicePlatform {
    ANDROID("android"),
    IOS("ios");

    private final String value;

    DevicePlatform(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }

    @JsonCreator
    public static DevicePlatform fromValue(String value) {
        for (DevicePlatform platform : values()) {
            if (platform.value.equalsIgnoreCase(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException("Unsupported platform: " + value);
    }
}
