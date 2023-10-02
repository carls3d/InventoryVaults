package com.carlsu.inventoryvaults.types;

public enum VaultType {
    
    MANUAL("Manual"),
    DIMENSION_CHANGE("DimensionChange"),
    GAMEMODE_CHANGE("GamemodeChange");

    private final String value;

    private VaultType(String value) {
        this.value = value;
    }

    
    public String getValue() {
        return value;
    }

    public static VaultType fromString(String value) {
        for (VaultType type : VaultType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

}
