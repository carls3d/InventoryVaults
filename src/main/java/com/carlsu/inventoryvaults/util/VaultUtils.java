package com.carlsu.inventoryvaults.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Nullable;

import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtPathArgument.NbtPath;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class VaultUtils implements IVaultData{
    
    /** InventoryVaults getters & setters */
    public static class PlayerVaultData {
        /** Playerdata.ForgeData.InventoryVaults */
        public static CompoundTag get(Player player) {
        CompoundTag forgeData = player.getPersistentData();
        // Create InventoryVaults if it doesn't exist
        if (!forgeData.contains(VAULT_NAME)) {
            forgeData.put(VAULT_NAME, new CompoundTag());
        }
        return forgeData.getCompound(VAULT_NAME);
        }
        /** Playerdata.ForgeData.InventoryVaults */
        public static void set(Player player, CompoundTag inventoryVaults) {
            CompoundTag forgeData = player.getPersistentData();
            forgeData.put(VAULT_NAME, inventoryVaults);
        }
        /** Playerdata.ForgeData.InventoryVaults.locationKey */
        public static void setData(Player player, String locationKey, CompoundTag data) {
            CompoundTag inventoryVaults = get(player);
            inventoryVaults.put(locationKey, data);
        }
        /** Playerdata.ForgeData.InventoryVaults.locationKey */
        public static void setString(Player player, String locationKey, String data) {
            CompoundTag inventoryVaults = get(player);
            inventoryVaults.putString(locationKey, data);
        }
        /** Playerdata.ForgeData.InventoryVaults.locationKey */
        public static String getString(Player player, String locationKey) {
            CompoundTag inventoryVaults = get(player);
            return inventoryVaults.getString(locationKey);
        }
        /** Playerdata.ForgeData.InventoryVaults.locationKey */
        public static String getStringOrDefault(Player player, String locationKey, String defaultValue) {
            CompoundTag inventoryVaults = get(player);
            String vaultsString = inventoryVaults.getString(locationKey);
            return ifElseValidKey(vaultsString, vaultsString, defaultValue);
        }

    }


    public static class Vault {
        public static CompoundTag get(Player player, String vaultKey) throws RuntimeException {
            // Check if vaultkey is in invalid keys
            if (INVALID_KEYS.contains(vaultKey)) {
                throw new RuntimeException("Vault key is invalid: " + vaultKey);
            }
            
            CompoundTag inventoryVaults = PlayerVaultData.get(player);
            // Create empty vault if it doesn't exist
            if (!inventoryVaults.contains(vaultKey)) {
                inventoryVaults.put(vaultKey, new CompoundTag());
            }
            Tag tag = inventoryVaults.get(vaultKey);
            if (tag != null && tag.getId() != 10) {
            }
            // CompoundTag vault = inventoryVaults.get(vaultKey).getType();
            return inventoryVaults.getCompound(vaultKey);
        }
    }


    /* Nbt key verifiers */
    public static boolean validKey(String key) {
        return key != null && !key.isEmpty();
        // return key != null && !key.equals("");  
    }
    public static String ifElseString(boolean condition, String ifTrue, String ifFalse) {
        return condition ? ifTrue : ifFalse;
    }
    public static String ifElseValidKey(String key, String ifValid, String ifNotValid) {
        return ifElseString(validKey(key), ifValid, ifNotValid);
    }


    /* Dimension type getters */
    public static ServerLevel getServerLevel(String dimension) {
        return getServerLevel(new ResourceLocation(dimension));
    }
    public static ServerLevel getServerLevel(ResourceLocation resourceLocation) {
        ResourceKey<Level> dimensionKey = getResourceKey(resourceLocation);
        return getServerLevel(dimensionKey);
    }
    public static ServerLevel getServerLevel(ResourceKey<Level> dimensionKey) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimensionKey);
    }

    public static ResourceKey<Level> getResourceKey(String dimension) {
        return getResourceKey(new ResourceLocation(dimension));
    }
    public static ResourceKey<Level> getResourceKey(ResourceLocation resourceLocation) {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation);
    }


    /* Nbt helpers */
    public static NbtPath getPath(String path) throws CommandSyntaxException {
        return getPath(new StringReader(path));
    }
    public static NbtPath getPath(StringReader stringReader) throws CommandSyntaxException {
        NbtPathArgument nbtPathArg = new NbtPathArgument();
        return nbtPathArg.parse(stringReader);
    }
    public static Tag parsePath(CompoundTag sourceData, String str) throws CommandSyntaxException {
        return parsePath(sourceData, getPath(str));
    }
    public static Tag parsePath(CompoundTag sourceData, StringReader stringReader) throws CommandSyntaxException {
        return parsePath(sourceData, getPath(stringReader));
    }
    public static Tag parsePath(CompoundTag sourceData, NbtPath path) throws CommandSyntaxException {
        Collection<Tag> tags = path.get(sourceData);
        Iterator<Tag> iterator = tags.iterator();
        Tag tag = iterator.next();
        if (!iterator.hasNext()) return tag;
        
        throw NbtPathArgument.ERROR_INVALID_NODE.create();
    }

    
    public static HashMap<String, Pair<Byte, TagType<?>>> getTagTypes(Player player, @Nullable String locationKey) {
        CompoundTag inventoryVaults = PlayerVaultData.get(player);
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

}
