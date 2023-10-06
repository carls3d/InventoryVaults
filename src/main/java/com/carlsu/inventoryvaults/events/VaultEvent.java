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
            LOGGER.error("5.2  VaultEvent.execute -> loadVaultKey is null, aborting");
            return;
        }
        
        
        LOGGER.info("5.2  VaultEvent.loadVault: " + loadVaultKey);
        VaultUtils.putStringInventoryVaults(player, PREVIOUS_VAULT, saveVaultKey);
        VaultUtils.putStringInventoryVaults(player, ACTIVE_VAULT, loadVaultKey);
        
        loadVault(playerData);

        // if current dimension is creative, set creative mode
        if (player.getLevel().dimension() == CREATIVE_KEY) {
            if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE){
                player.setGameMode(GameType.CREATIVE);
            }
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
    


    public static void clearInventoryOnEmptyVault(ServerPlayer player, CompoundTag playerVault) {
        if (playerVault.isEmpty()) {
            player.getInventory().clearContent();
            CosArmor.commandClear(player);
        } 
        else if (playerVault.getCompound(CosArmor.cosKey).isEmpty()){
            CosArmor.commandClear(player);
        }
    }


}
