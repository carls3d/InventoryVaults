package com.carlsu.inventoryvaults;

import java.util.Set;

import com.carlsu.inventoryvaults.commands.VaultCommands;
import com.carlsu.inventoryvaults.util.VaultsData;

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
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraftforge.server.ServerLifecycleHooks;

public class VaultHandler {
    public static final Set<String> VAULT_FILTER = VaultsData.VAULT_FILTER;
    private static final String VAULT_NAME = VaultsData.VAULT_NAME;
    private static final String DEFAULT_VAULT = VaultsData.DEFAULT_VAULT;

    public static void saveVault(Player player) {
        saveVault(player, DEFAULT_VAULT);
    }
    public static void saveVault(Player player, String vaultKey) {
        CompoundTag playerVault = serializeVault(player);
        CompoundTag ForgeData = player.getPersistentData();
        CompoundTag inventoryVault = ForgeData.copy().getCompound(VAULT_NAME);
        inventoryVault.put(vaultKey, playerVault);
        ForgeData.put(VAULT_NAME, inventoryVault);
    }

    public static void loadVault(Player player) {
        loadVault(player, DEFAULT_VAULT, false);
    }
    public static void loadVault(Player player, String vaultKey) {
        loadVault(player, vaultKey, false);
    }
    public static void loadVault(Player player, String vaultKey, boolean changeDimension) {
        CompoundTag playerVault = getVault(player, vaultKey);
        if (playerVault == null) {
            return;
        }
        // Inventory stuff
        player.load(playerVault);
        ServerPlayer serverPlayer = (ServerPlayer) player;
        String dimension = playerVault.getString("Dimension");

        serverPlayer.setGameMode(GameType.byId(playerVault.getInt("playerGameType")));
        // serverPlayer.setExperiencePoints(playerVault.getInt("XpP"));
        // serverPlayer.setExperienceLevels(0);
        serverPlayer.setHealth(playerVault.getFloat("Health"));
        serverPlayer.getFoodData().setFoodLevel(playerVault.getInt("foodLevel"));
        serverPlayer.getFoodData().setSaturation(playerVault.getFloat("foodSaturationLevel"));
        serverPlayer.getFoodData().setExhaustion(playerVault.getFloat("foodExhaustionLevel"));
        serverPlayer.experienceLevel = playerVault.getInt("XpLevel");
        serverPlayer.experienceProgress = playerVault.getFloat("XpP");
        

        ListTag pos = playerVault.getList("Pos", 6);
        ListTag rot = playerVault.getList("Rotation", 5);
        if (pos.size() == 3 && rot.size() == 2) {
            double x = pos.getDouble(0);
            double y = pos.getDouble(1);
            double z = pos.getDouble(2);
            float rotationYaw = rot.getFloat(0);
            float rotationPitch = rot.getFloat(1);

            if (changeDimension && !dimension.isEmpty()) {
                ResourceKey<Level> dimensionKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dimension));
                ServerLevel targetWorld = serverPlayer.getLevel().getServer().getLevel(dimensionKey);
                if (targetWorld != null) {
                    // Teleport the player
                    serverPlayer.teleportTo(targetWorld, x, y, z, rotationYaw, rotationPitch);
                } else {
                    VaultCommands.sendFailure("Dimension not found");
                }
            } else {
                // serverPlayer.teleportTo(serverPlayer.getLevel(), x, y, z, rotationYaw, rotationPitch);
            }
        } else {
            VaultCommands.sendFailure("Invalid position & rotation");
        }
    }

    private static CompoundTag getVault(Player player, String vaultKey) {
        CompoundTag ForgeData = player.getPersistentData().copy();
        if (ForgeData.getAllKeys().contains(VAULT_NAME) && ForgeData.getCompound(VAULT_NAME).contains(vaultKey)) {
            return ForgeData.getCompound(VAULT_NAME).getCompound(vaultKey);
            // if (ForgeData.getCompound(VAULT_NAME).contains(vaultKey)) {
            //     return ForgeData.getCompound(VAULT_NAME).getCompound(vaultKey);
            // } else {
            //     // No vault with key vaultKey exists
            //     return null;
            // }
        } else {
            // No VAULT_NAME exists in ForgeData
            return null;
        }
    }
    // public static void deserializeVault(Player player, CompoundTag vault) {
    //     player.load(vault);
    // }

    public static CompoundTag serializeVault(Player player) {
        // Returns a copy of the player's data with only the keys we want
        CompoundTag playerData = player.serializeNBT().copy();
        // CompoundTag playerData = player.serializeNBT();
        CompoundTag filteredData = new CompoundTag();
        for (String key : VAULT_FILTER) {
            if (playerData.contains(key)) {
                filteredData.put(key, playerData.get(key));
            }
        }
        // CompoundTag filteredDataForgeData = filteredData.getCompound("ForgeData");
        // filteredDataForgeData.put(VAULT_NAME, filteredData.get(VAULT_NAME));

        // filteredData.put("ForgeData", filteredDataForgeData);
        return filteredData;
    }








    public static CompoundTag getVaultStorage(Player player) {
        CompoundTag vaultData = getVaultStoragePath();
        String playerName = player.getName().getString();
        CompoundTag playerVault = (CompoundTag) vaultData.get(playerName);
        return playerVault;
    }
    public static void setVaultStorage(Player player, String vaultKey) {
        ResourceLocation vaultLoc = new ResourceLocation("minecraft", "vault");
        CommandStorage commandStorage = ServerLifecycleHooks.getCurrentServer().getCommandStorage();
        CompoundTag vaultData = commandStorage.get(vaultLoc);
        String name = player.getName().getString();
        vaultData.put(name, serializeVaultStorage(player));
        // Tag vault = new CompoundTag().put(vaultKey, playerVault);
        // vaultData.merge(playerVault);
        // commandStorage.set(vaultLoc, vaultData);
    }
    public static void setVaultStorage(Player player) {
        setVaultStorage(player, DEFAULT_VAULT);
    }
    public static CompoundTag serializeVaultStorage(Player player) {
        CompoundTag playerData = player.serializeNBT().copy();
        CompoundTag filteredData = new CompoundTag();
        // { Health:20.0f, Hunger:20, XpP:6, ...}
        for (String key : VAULT_FILTER) {
            if (playerData.contains(key)) {
                filteredData.put(key, playerData.get(key));
            }
        }

        // {playername:{ Health:20.0f, Hunger:20, XpP:6, ...}}
        String name = player.getName().getString();
        CompoundTag playerVault = new CompoundTag();
        playerVault.put(name, filteredData);
        return playerVault;
    }
    public static CompoundTag getVaultStoragePath(String a, String b) {
        ResourceLocation vaultLoc = new ResourceLocation(a, b);
        CommandStorage commandStorage = ServerLifecycleHooks.getCurrentServer().getCommandStorage();
        CompoundTag vaultData = commandStorage.get(vaultLoc);
        return vaultData;
    }
    public static CompoundTag getVaultStoragePath() {
        return getVaultStoragePath("minecraft", "vault");
    }
}
