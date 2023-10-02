package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;


public abstract class VaultEvent implements IVaultData {
    public final VaultType eventType;

    public VaultEvent(VaultType eventType) {
        this.eventType = eventType;
    }

    public final void execute(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String saveVaultKey = playerData.getSaveVaultKey();
        String loadVaultKey = playerData.getLoadVaultKey();
        String activeVaultKey = playerData.getActiveVaultKey();
        String previousVaultKey = playerData.getPreviousVaultKey();
        LOGGER.info("5.1  previousVaultKey: " + previousVaultKey);
        if (!VaultUtils.validKey(saveVaultKey)) {
            if (VaultUtils.validKey(activeVaultKey)) {
                saveVaultKey = activeVaultKey;
            } else {
                LOGGER.error("VaultEvent.execute -> saveVaultKey is null, aborting"); 
                return;}
        }
        
        LOGGER.info("5.1  VaultEvent.saveVault: " + saveVaultKey);
        saveVault(playerData);
        VaultUtils.putStringInventoryVaults(player, ACTIVE_VAULT, saveVaultKey);
        
        
        if (!VaultUtils.validKey(loadVaultKey)) return;
        
        
        LOGGER.info("5.2  VaultEvent.loadVault: " + loadVaultKey);
        VaultUtils.putStringInventoryVaults(player, PREVIOUS_VAULT, saveVaultKey);
        VaultUtils.putStringInventoryVaults(player, ACTIVE_VAULT, loadVaultKey);
        loadVault(playerData);
    }


    protected abstract void saveVault(PlayerData playerData);
    protected abstract void loadVault(PlayerData playerData);
    

    // Check if location data is valid
    public static boolean validPlayerVaultLocation(CompoundTag playerVault){
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);
        if (vaultPosition.size() != 3 ||
            vaultRotation.size() != 2 ||
            vaultDimension.isEmpty()
            ) {
            LOGGER.error("loadVault -> Invalid dimension: Dimension: " + vaultDimension);
            LOGGER.error("loadVault -> Invalid position: Pos: " + vaultPosition);
            LOGGER.error("loadVault -> Invalid rotation: Rotation: " + vaultRotation);
            return false;
        }
        return true;
    }

    // Returns vault data if it exists
    public static CompoundTag getVault(Player player, String vaultKey) {
        CompoundTag ForgeData = player.getPersistentData();

        // Create InventoryVaults if it doesn't exist
        boolean vaultExists = ForgeData.contains(VAULT_NAME);
        if (!vaultExists) {
            ForgeData.put(VAULT_NAME, new CompoundTag());
        }
        CompoundTag inventoryVault = ForgeData.getCompound(VAULT_NAME);

        // Create empty vault if it doesn't exist
        if (!inventoryVault.contains(vaultKey)) {
            inventoryVault.put(vaultKey, new CompoundTag());
        }
        return inventoryVault.getCompound(vaultKey);
    }

    // Teleport player
    public static void teleportToLocation(ServerPlayer serverPlayer, CompoundTag playerVault) {
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);

        ServerLevel targetWorld = getServerLevel(vaultDimension);
        if (targetWorld != null) {
            // Teleport the player
            double x = vaultPosition.getDouble(0);
            double y = vaultPosition.getDouble(1);
            double z = vaultPosition.getDouble(2);
            float rotPitch = vaultRotation.getFloat(0);
            float rotYaw = vaultRotation.getFloat(1);
            serverPlayer.teleportTo(targetWorld, x, y, z, rotYaw, rotPitch);
        }
    }

    // Returns a copy of the player's data with only the keys we want
    public static CompoundTag filterVaultData(Player player) {
        CompoundTag playerData = player.serializeNBT();

        // Store only the keys in VAULT_FILTER
        CompoundTag filteredData = new CompoundTag();
        for (String key : VAULT_FILTER) {
            if (playerData.contains(key)) {
                Tag value = playerData.get(key);
                if (value == null) continue;
                filteredData.put(key, value);
            }
        }
        return filteredData;
    }

    public static ServerLevel getServerLevel(ResourceKey<Level> dimensionKey) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimensionKey);
    }
    public static ServerLevel getServerLevel(ResourceLocation resourceLocation) {
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceLocation);
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimensionKey);
    }
    public static ServerLevel getServerLevel(String dimension) {
        return getServerLevel(new ResourceLocation(dimension));
    }
    
}
