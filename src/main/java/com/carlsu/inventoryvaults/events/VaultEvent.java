package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;


public abstract class VaultEvent implements IVaultData, CreativeDimension {
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
        
        
        if (!VaultUtils.validKey(loadVaultKey)) {
            // Invalid loadVaultKey, skip loading
            return;
        }
        
        
        LOGGER.info("5.2  VaultEvent.loadVault: " + loadVaultKey);
        VaultUtils.putStringInventoryVaults(player, PREVIOUS_VAULT, saveVaultKey);
        VaultUtils.putStringInventoryVaults(player, ACTIVE_VAULT, loadVaultKey);
        
        loadVault(playerData);
        // if current dimension is creative, set creative mode
        if (player.getLevel().dimension() == CREATIVE_KEY) {
            LOGGER.info("5.2  VaultEvent.loadVault: " + loadVaultKey + " -> Setting creative mode");
            player.setGameMode(GameType.CREATIVE);
            
        }
    }


    protected abstract void saveVault(PlayerData playerData);
    protected abstract void loadVault(PlayerData playerData);
    

    // Check if location data is valid
    public static boolean validPlayerVaultLocation(ServerPlayer player, CompoundTag playerVault){
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ResourceKey<Level> vaultDimensionKey = VaultUtils.getResourceKey(vaultDimension);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (vaultPosition.size() != 3) {
            LOGGER.error("5.2  ! loadVault -> Invalid position: Pos: " + vaultPosition);
            return false;
        }
        if (!server.levelKeys().contains(vaultDimensionKey)) {
            LOGGER.error("5.2  ! loadVault -> Invalid dimension: Dimension: " + vaultDimension);
            return false;
        }
        return true;
    }

    // Returns vault data if it exists
    public static CompoundTag getVault(ServerPlayer player, String vaultKey) {
        // CompoundTag ForgeData = player.getPersistentData();

        // // Create InventoryVaults if it doesn't exist
        // boolean vaultExists = ForgeData.contains(VAULT_NAME);
        // if (!vaultExists) {
        //     ForgeData.put(VAULT_NAME, new CompoundTag());
        // }
        CompoundTag inventoryVaults = VaultUtils.getInventoryVaults(player);

        // Create empty vault if it doesn't exist
        if (!inventoryVaults.contains(vaultKey)) {
            inventoryVaults.put(vaultKey, new CompoundTag());
        }
        CompoundTag playerVault = inventoryVaults.getCompound(vaultKey);

        if (playerVault.isEmpty()) {
            return playerVault;
        }
        
        ListTag playerRot = player.serializeNBT().getList("Rotation", 5);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);

        boolean loadRot = inventoryVaults.getBoolean(LOAD_ROTATION);
        boolean rotEmpty = vaultRotation.isEmpty();

        if (!loadRot || rotEmpty) {
            playerVault.put("Rotation", playerRot);
        }

        return playerVault;
    }

    // Teleport player
    public static void teleportToLocation(ServerPlayer serverPlayer, CompoundTag playerVault) {
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);
        
        CompoundTag inventoryVaults = VaultUtils.getInventoryVaults(serverPlayer);
        if (!inventoryVaults.contains(LOAD_ROTATION)) {
            inventoryVaults.putBoolean("loadRotation", true);
        }
        


        ServerLevel level = VaultUtils.getServerLevel(vaultDimension);
        if (level != null) {
            // Teleport the player
            double x = vaultPosition.getDouble(0);
            double y = vaultPosition.getDouble(1);
            double z = vaultPosition.getDouble(2);
            LOGGER.info("5.2  teleportToLocation -> x: " + x);
            LOGGER.info("5.2  player -> x: " + serverPlayer.getX());
            // double x2 = serverPlayer.getX();
            // double y2 = serverPlayer.getY();
            // double z2 = serverPlayer.getZ();
            // boolean loadRot = inventoryVaults.getBoolean(LOAD_ROTATION);
            // boolean rotEmpty = vaultRotation.isEmpty();
            
            // float yaw = (loadRot && !rotEmpty) ? vaultRotation.getFloat(0) : serverPlayer.getYRot();
            // float pitch = (loadRot && !rotEmpty) ? vaultRotation.getFloat(1) : serverPlayer.getXRot();
            // float yaw = rotEmpty ? serverPlayer.getYRot() : vaultRotation.getFloat(0);
            // float pitch = rotEmpty ? serverPlayer.getXRot() : vaultRotation.getFloat(1);
            // float yaw = serverPlayer.getYRot();
            // float pitch = serverPlayer.getXRot();
            float yaw = vaultRotation.getFloat(0);
            float pitch = vaultRotation.getFloat(1);
          
            serverPlayer.teleportTo(level, x, y, z, yaw, pitch);
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
}
