package com.viking.xfat;

public enum DefaultPreference {
    BUTTON_TRANSPARENT("key_button_transparent", "0.25f");

    private String key;
    private String defaultValue;

    private DefaultPreference(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}