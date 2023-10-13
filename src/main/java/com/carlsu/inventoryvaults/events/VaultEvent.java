package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.compatibility.CosArmor;
import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;


public abstract class VaultEvent implements IVaultData, CreativeDimension {
    public final VaultType eventType;

    public VaultEvent(VaultType eventType) {
        this.eventType = eventType;
    }

    protected abstract boolean isValidEvent(PlayerData playerData);

    protected abstract void saveVault(PlayerData playerData);

    protected abstract void loadVault(PlayerData playerData);
    
    
    public final void execute(PlayerData playerData) {
        if (!isValidEvent(playerData)) return;

        // LOGGER.info(" Executing VaultEvent " + eventType);
        ServerPlayer player = playerData.getPlayer();
        String saveVaultKey = playerData.getSaveVaultKey();
        String loadVaultKey = playerData.getLoadVaultKey();
        String activeVaultKey = playerData.getActiveVaultKey();
        // String previousVaultKey = playerData.getPreviousVaultKey();
        
        if (!VaultUtils.validKey(saveVaultKey)) {
            if (VaultUtils.validKey(activeVaultKey)) {
                saveVaultKey = activeVaultKey;
            } else {
                LOGGER.error("VaultEvent('" + eventType + "').execute -> saveVaultKey is null, aborting"); 
                return;}
        }
        
        saveVault(playerData);
        VaultUtils.PlayerVaultData.setString(player, ACTIVE_VAULT, saveVaultKey);
        VaultUtils.PlayerVaultData.get(player);
        
        if (!VaultUtils.validKey(loadVaultKey)) {
            LOGGER.error("! VaultEvent('" + eventType + "').execute: -> loadVaultKey is null, aborting");
            return;
        }
        
        VaultUtils.PlayerVaultData.setString(player, PREVIOUS_VAULT, saveVaultKey);
        VaultUtils.PlayerVaultData.setString(player, ACTIVE_VAULT, loadVaultKey);
        
        clearInventoryOnEmptyLoadVault(playerData);
        if (!validPlayerVaultLocation(playerData)) return;
        loadVault(playerData);

        // if current dimension is creative, set creative mode
        if (player.getLevel().dimension() == CREATIVE_KEY) {
            if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE){
                player.setGameMode(GameType.CREATIVE);
            }
        }
    }


    // Check if location data is valid
    public static boolean validPlayerVaultLocation(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        CompoundTag playerVault = getVault(player, playerData.getLoadVaultKey());
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ResourceKey<Level> vaultDimensionKey = VaultUtils.getResourceKey(vaultDimension);
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (vaultPosition.size() != 3) {
            LOGGER.error("! loadVault -> Invalid position: Pos: " + vaultPosition);
            return false;
        }
        if (!server.levelKeys().contains(vaultDimensionKey)) {
            LOGGER.error("! loadVault -> Invalid dimension: Dimension: " + vaultDimension);
            return false;
        }
        return true;
    }


    // Returns vault data if it exists
    public static CompoundTag getVault(ServerPlayer player, String vaultKey) {

        CompoundTag inventoryVaults = VaultUtils.PlayerVaultData.get(player);

        // Create empty vault if it doesn't exist
        if (!inventoryVaults.contains(vaultKey)) {
            inventoryVaults.put(vaultKey, new CompoundTag());
        }
        CompoundTag playerVault = inventoryVaults.getCompound(vaultKey);

        if (playerVault.isEmpty()) {
            return playerVault;
        }
        
        if (!inventoryVaults.contains(LOAD_ROTATION)) inventoryVaults.putBoolean(LOAD_ROTATION, true);
        boolean loadRotation = inventoryVaults.getBoolean(LOAD_ROTATION);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);
        
        // If stored rotation is empty or "LoadRotation" is false, use current rotation
        if (!loadRotation || vaultRotation.isEmpty()) {
            playerVault.put("Rotation", player.serializeNBT().getList("Rotation", 5));
        }

        return playerVault;
    }


    // Teleport player
    public static void teleportToLocation(ServerPlayer serverPlayer, CompoundTag playerVault) {
        String vaultDimension = playerVault.getString("Dimension");
        ListTag vaultPosition = playerVault.getList("Pos", 6);
        ListTag vaultRotation = playerVault.getList("Rotation", 5);
        
        ServerLevel level = VaultUtils.getServerLevel(vaultDimension);
        if (level != null) {
            // Teleport the player
            double x = vaultPosition.getDouble(0);
            double y = vaultPosition.getDouble(1);
            double z = vaultPosition.getDouble(2);
            float yaw = vaultRotation.getFloat(0);
            float pitch = vaultRotation.getFloat(1);

            ResourceKey<Level> vaultDimensionKey = VaultUtils.getResourceKey(playerVault.getString("Dimension"));
            if (y < -64) {
                if (vaultDimensionKey.equals(CREATIVE_KEY)) {
                    x = CREATIVE_SPAWN.getDouble(0);
                    y = CREATIVE_SPAWN.getDouble(1);
                    z = CREATIVE_SPAWN.getDouble(2);
                    yaw = 0.0F;
                    pitch = 0.0F;
                }
            }
            serverPlayer.teleportTo(level, x, y, z, yaw, pitch);
        }
    }


    // Returns a copy of the player's data with only the keys we want
    public static CompoundTag filterVaultData(ServerPlayer player) {
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
        filteredData = CosArmor.injectCosArmor(player, filteredData);
        return filteredData;
    }

    
    public static void loadAdditionalData(ServerPlayer serverPlayer, CompoundTag playerVault) {
        serverPlayer.setGameMode(GameType.byId(playerVault.getInt("playerGameType")));
        serverPlayer.setHealth(playerVault.getFloat("Health"));
        serverPlayer.getFoodData().setFoodLevel(playerVault.getInt("foodLevel"));
        serverPlayer.getFoodData().setSaturation(playerVault.getFloat("foodSaturationLevel"));
        serverPlayer.getFoodData().setExhaustion(playerVault.getFloat("foodExhaustionLevel"));
        serverPlayer.experienceLevel = playerVault.getInt("XpLevel");
        serverPlayer.experienceProgress = playerVault.getFloat("XpP");
        serverPlayer.getAbilities().loadSaveData(playerVault);
        CosArmor.cosLoad(serverPlayer, playerVault);
    }
    


    public static void clearInventoryOnEmptyLoadVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        CompoundTag playerVault = getVault(player, playerData.getLoadVaultKey());
        if (playerVault.isEmpty()) {
            player.getInventory().clearContent();
            CosArmor.commandClear(player);
        } 
        else if (playerVault.getCompound(CosArmor.cosKey).isEmpty()){
            CosArmor.commandClear(player);
        }
    }


}
