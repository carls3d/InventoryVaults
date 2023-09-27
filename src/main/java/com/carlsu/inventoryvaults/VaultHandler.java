package com.carlsu.inventoryvaults;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.util.VaultsData;
import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.logging.LogUtils;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class VaultHandler {
    static final Logger LOGGER = LogUtils.getLogger();

    public static void saveVault(Player player, String vaultKey) {
        saveVault(player, vaultKey, null);}
    public static void saveVault(Player player, String vaultKey, CompoundTag locationData) {
        CompoundTag filteredData = serializeVault(player);
        if (locationData != null) {
            filteredData.put("Pos", locationData.get("Pos"));
            filteredData.put("Rotation", locationData.get("Rotation"));
            filteredData.put("Dimension", locationData.get("Dimension"));
        }
        
        LOGGER.info("Saving vault: "+ vaultKey);
        
        CompoundTag forgeData = player.getPersistentData();
        CompoundTag inventoryVaults = forgeData.getCompound(VaultsData.VAULT_NAME);
        inventoryVaults.put(vaultKey, filteredData);
    }



    public static void loadVault(Player player, String vaultKey) {
        loadVault(player, vaultKey, true);}
    public static void loadVault(Player player, String vaultKey, boolean changeDimension) {
        CompoundTag playerVault = getVault(player, vaultKey);
        
        // Abort if vault doesn't exist
        if (playerVault == null) {
            LOGGER.error(
                "\nloadVault -> Vault doesn't exist"+ 
                "\n\tName: "+ player.getName().getString() +" -> VaultKey: "+ vaultKey +"\n"); 
            return; 
        }

        if (changeDimension) {
            // If changing to or from creative dimension, only teleport player
            ModDimension.CREATIVE_KEY.location();
            // ResourceKey<Level> currentDimension = player.level.dimension();
        }

        LOGGER.info("\n\n"+player.getName().getString() +" Loading vault: "+ vaultKey+"\n");
        
        



        // Inventory, EnderItems, ForgeCaps, ForgeData, Attributes
        player.load(playerVault);

        ServerPlayer serverPlayer = (ServerPlayer) player;

        serverPlayer.setGameMode(GameType.byId(playerVault.getInt("playerGameType")));
        serverPlayer.setHealth(playerVault.getFloat("Health"));
        serverPlayer.getFoodData().setFoodLevel(playerVault.getInt("foodLevel"));
        serverPlayer.getFoodData().setSaturation(playerVault.getFloat("foodSaturationLevel"));
        serverPlayer.getFoodData().setExhaustion(playerVault.getFloat("foodExhaustionLevel"));
        serverPlayer.experienceLevel = playerVault.getInt("XpLevel");
        serverPlayer.experienceProgress = playerVault.getFloat("XpP");
        ListTag pos = playerVault.getList("Pos", 6);
        ListTag rot = playerVault.getList("Rotation", 5);
        String dimension = playerVault.getString("Dimension");
        
        if (pos.size() != 3 || rot.size() != 2) {
            LOGGER.error(
                "\n\nloadVault -> "+player.getName().toString()+ " -> Invalid position & rotation"+
                "\n\tPos: "+ pos.toString() +
                "\n\tRot: "+ rot.toString() +
                "\n");
            return;
        }
        if (!changeDimension) {return;}

        if (!dimension.isEmpty()) {
            ServerLevel targetWorld = getServerLevel(new ResourceLocation(dimension));
            if (targetWorld != null) {
                // Teleport the player
                double x = pos.getDouble(0);
                double y = pos.getDouble(1);
                double z = pos.getDouble(2);
                float rotYaw = rot.getFloat(0);
                float rotPitch = rot.getFloat(1);
                // serverPlayer.teleportTo(targetWorld, x, y, z, rotYaw, rotPitch);
                serverPlayer.teleportTo(targetWorld, x, y, z, rotYaw, rotPitch);

            } else {
                LOGGER.error(
                    "\nloadVault -> Invalid dimension"+
                    "\n\tDimension: "+ dimension +
                    "\n");
            }
        }
    }

    // Returns vault data if it exists
    private static CompoundTag getVault(Player player, String vaultKey) {
        CompoundTag ForgeData = player.getPersistentData();
        
        // Create InventoryVaults if it doesn't exist
        boolean vaultExists = ForgeData.contains(VaultsData.VAULT_NAME);
        CompoundTag inventoryVault = vaultExists ? ForgeData.getCompound(VaultsData.VAULT_NAME) : new CompoundTag();

        // Create empty vault if it doesn't exist
        if (!inventoryVault.contains(vaultKey)) {
            CompoundTag newVault = new CompoundTag();
            inventoryVault.put(vaultKey, newVault);
        } 

        return inventoryVault.getCompound(vaultKey);
    }

    // Returns a copy of the player's data with only the keys we want
    public static CompoundTag serializeVault(Player player) {
        CompoundTag playerData = player.serializeNBT().copy();
        
        // Store only the keys in VAULT_FILTER
        CompoundTag filteredData = new CompoundTag();
        for (String key : VaultsData.VAULT_FILTER) {
            if (playerData.contains(key)) {
                filteredData.put(key, playerData.get(key));
            }
        }
        return filteredData;
    }


    public static ServerLevel getServerLevel(ResourceLocation resourceLocation) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation);
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimensionKey);
    }





    // public static CompoundTag getVaultStorage(Player player) {
    //     CompoundTag vaultData = getVaultStoragePath();
    //     String playerName = player.getName().getString();
    //     CompoundTag playerVault = (CompoundTag) vaultData.get(playerName);
    //     return playerVault;
    // }
    // public static void setVaultStorage(Player player, String vaultKey) {
    //     ResourceLocation vaultLoc = new ResourceLocation("minecraft", "vault");
    //     CommandStorage commandStorage = ServerLifecycleHooks.getCurrentServer().getCommandStorage();
    //     CompoundTag vaultData = commandStorage.get(vaultLoc);
    //     String name = player.getName().getString();
    //     vaultData.put(name, serializeVaultStorage(player));
    //     // Tag vault = new CompoundTag().put(vaultKey, playerVault);
    //     // vaultData.merge(playerVault);
    //     // commandStorage.set(vaultLoc, vaultData);
    // }
    // public static void setVaultStorage(Player player) {
    //     setVaultStorage(player, VaultsData.DEFAULT_VAULT);
    // }
    // public static CompoundTag serializeVaultStorage(Player player) {
    //     CompoundTag playerData = player.serializeNBT().copy();
    //     CompoundTag filteredData = new CompoundTag();
    //     // { Health:20.0f, Hunger:20, XpP:6, ...}
    //     for (String key : VaultsData.VAULT_FILTER) {
    //         if (playerData.contains(key)) {
    //             filteredData.put(key, playerData.get(key));
    //         }
    //     }

    //     // {playername:{ Health:20.0f, Hunger:20, XpP:6, ...}}
    //     String name = player.getName().getString();
    //     CompoundTag playerVault = new CompoundTag();
    //     playerVault.put(name, filteredData);
    //     return playerVault;
    // }
    // public static CompoundTag getVaultStoragePath(String a, String b) {
    //     ResourceLocation vaultLoc = new ResourceLocation(a, b);
    //     CommandStorage commandStorage = ServerLifecycleHooks.getCurrentServer().getCommandStorage();
    //     CompoundTag vaultData = commandStorage.get(vaultLoc);
    //     return vaultData;
    // }
    // public static CompoundTag getVaultStoragePath() {
    //     return getVaultStoragePath("minecraft", "vault");
    // }
}
