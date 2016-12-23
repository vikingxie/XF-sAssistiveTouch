package com.viking.xfat;

public enum DefaultPreference {
    BUTTON_TRANSPARENT("key_button_transparent", "0.25f"),
    LAST_VIRTUAL_X("key_virtual_coordinate_x", "0"),
    LAST_VIRTUAL_Y("key_virtual_coordinate_y", "0"),
    HOME_BEFORE_LOCK("key_home_before_close_screen", "true");

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