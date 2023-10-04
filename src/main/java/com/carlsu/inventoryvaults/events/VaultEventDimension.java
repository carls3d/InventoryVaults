package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class VaultEventDimension extends VaultEvent implements CreativeDimension{
    public final Boolean teleportPlayer;
    public VaultEventDimension() {
        this(true); 
    }
    public VaultEventDimension(Boolean teleport) {
        super(VaultType.DIMENSION_CHANGE);
        this.teleportPlayer = teleport;
    }
    // if (saveVaultKey == null && loadVaultKey != DEFAULT_VAULT) {saveVaultKey = DEFAULT_VAULT;}
    

    @Override
    protected void saveVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getSaveVaultKey();

        CompoundTag forgeData = player.getPersistentData();
        if (!forgeData.contains(VAULT_NAME)) {
            forgeData.put(VAULT_NAME, new CompoundTag());
        }
        CompoundTag inventoryVaults = forgeData.getCompound(IVaultData.VAULT_NAME);
        CompoundTag playerVault = filterVaultData(player);
        
        // Set location from location before dimension change
        VaultUtils.setVaultLocation(playerVault, playerData);


        inventoryVaults.put(vaultKey, playerVault);
        LOGGER.info("5.1  End of saveVault");
    }

    @Override
    protected void loadVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getLoadVaultKey();

        CompoundTag playerVault = getVault(player, vaultKey);
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (playerVault == null) {
            LOGGER.error("5.2  loadVault(Player player, String vaultKey) -> playerVault is null");
            return;
        }


        if (!validPlayerVaultLocation(playerVault)) return;
        

        player.load(playerVault); /*Inventory, EnderItems, ForgeCaps, ForgeData, Attributes*/
        
        serverPlayer.setGameMode(GameType.byId(playerVault.getInt("playerGameType")));
        serverPlayer.setHealth(playerVault.getFloat("Health"));
        serverPlayer.getFoodData().setFoodLevel(playerVault.getInt("foodLevel"));
        serverPlayer.getFoodData().setSaturation(playerVault.getFloat("foodSaturationLevel"));
        serverPlayer.getFoodData().setExhaustion(playerVault.getFloat("foodExhaustionLevel"));
        serverPlayer.experienceLevel = playerVault.getInt("XpLevel");
        serverPlayer.experienceProgress = playerVault.getFloat("XpP");
        
        if (teleportPlayer) {
            LOGGER.info("5.2  VaultEventDimension.loadVault.teleport");
            // StringTag playerDataDim = StringTag.valueOf(playerData.getCurrentDimension().location().toString());
            String playerDataDim = playerData.getCurrentDimension().location().toString();
            String playerVaultDim = playerVault.getString("Dimension");
            if (!playerDataDim.equals(playerVaultDim)) {
                LOGGER.error("5.2  VaultEventDimension.loadVault.teleport: teleport destination is in a different dimension");
                return;
            }
            ListTag vaultPosition = playerVault.getList("Pos", 6);
            playerVault.getString("Dimension").equals(CREATIVE_KEY.location().toString());
            double y = vaultPosition.getDouble(1);
            if (y < -64) {
                playerVault.put("Pos", CREATIVE_SPAWN);
            };
            teleportToLocation(serverPlayer, playerVault);
        }
        LOGGER.info("5.2  End of loadVault");
    }
}


