package com.carlsu.inventoryvaults.util;

import java.util.HashMap;

import javax.annotation.Nullable;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.ibm.icu.impl.Pair;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.world.entity.player.Player;

public class VaultUtils implements IVaultData{
    
    /* Vault data getters */
    public static CompoundTag getInventoryVaults(Player player) {
        CompoundTag forgeData = player.getPersistentData();
        // Create InventoryVaults if it doesn't exist
        if (!forgeData.contains(VAULT_NAME)) {
            forgeData.put(VAULT_NAME, new CompoundTag());
        }
        CompoundTag inventoryVaults = forgeData.getCompound(VAULT_NAME);
        return inventoryVaults;
    }
    public static CompoundTag getVault(Player player, String locationKey) {
        CompoundTag inventoryVaults = getInventoryVaults(player);
        // Create empty vault if it doesn't exist
        if (!inventoryVaults.contains(locationKey)) {
            inventoryVaults.put(locationKey, new CompoundTag());
        }
        return inventoryVaults.getCompound(locationKey);
    }

    public static String getVaultsString(Player player, String locationKey) {
        CompoundTag inventoryVaults = getInventoryVaults(player);
        return inventoryVaults.getString(locationKey);
    }
    
    public static HashMap<String, Pair<Byte, TagType<?>>> getTagTypes(Player player, @Nullable String locationKey) {
        CompoundTag inventoryVaults = getInventoryVaults(player);
        CompoundTag vault = inventoryVaults.getCompound(locationKey);

        HashMap<String, Pair<Byte, TagType<?>>> tagTypes = new HashMap<>();

        for (String key : vault.getAllKeys()) {
            Tag tag = vault.get(key);
            if (tag != null) {
                Pair<Byte, TagType<?>> pair = Pair.of(tag.getId(), tag.getType());
                tagTypes.put(key, pair);
            } else LOGGER.error("getTagTypes -> tag is null: " + key);
        }
        return tagTypes;
    }
    

    /*  Vault data setters */
    public static void putInventoryVaults(Player player, String locationKey, CompoundTag data) {
        CompoundTag inventoryVaults = getInventoryVaults(player);
        inventoryVaults.put(locationKey, data);
    }
    public static void putStringInventoryVaults(Player player, String locationKey, String data) {
        CompoundTag inventoryVaults = getInventoryVaults(player);
        inventoryVaults.putString(locationKey, data);
    }
    public static void setVaultLocation(CompoundTag vaultData, PlayerData playerData) {
        vaultData.putString("Dimension", playerData.getLastDimension().location().toString());
        vaultData.put("Pos", playerData.getLastPos());
        vaultData.put("Rotation", playerData.getLastRot());
    }


    /* Nbt key verifiers */
    public static boolean validKey(String key) {
        return key != null && !key.equals("");
    }
    public static String ifElseString(boolean condition, String ifTrue, String ifFalse) {
        return condition ? ifTrue : ifFalse;
    }
    public static String ifElseValidKey(String key, String ifValid, String ifNotValid) {
        return ifElseString(validKey(key), ifValid, ifNotValid);
    }
    
}
