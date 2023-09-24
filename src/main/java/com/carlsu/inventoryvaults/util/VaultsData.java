package com.carlsu.inventoryvaults.util;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class VaultsData {
    public static final String VAULT_NAME = "InventoryVaults";
    public static final ResourceKey<Level> CREATIVE_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("_dimensions", "creative"));

    public static String DEFAULT_VAULT = "main";
    public static String CREATIVE_VAULT = "creative";

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
            "Dimension"
            // "Pos",
            // "Rotation",
            // "Motion"
    );
}
