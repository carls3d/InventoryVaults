package com.carlsu.inventoryvaults.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;


public class VaultsData {
    public static final String VAULT_NAME = "InventoryVaults";
    public static final String DEFAULT_VAULT = "main";
    
    public static final Set<String> VAULT_FILTER = ImmutableSet.of (
            "Health",
            "foodLevel",
            "foodSaturationLevel",
            "foodExhaustionLevel",
            "SelectedItemSlot",
            "XpP",
            "XpLevel",
            "playerGameType",
            "ActiveEffects",
            "Inventory",
            "EnderItems",
            "ForgeCaps",
            // "ForgeData",
            "Attributes",
            "Dimension",
            "Pos",
            "Rotation"
            // "Motion"
    );
}
