package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class VaultEventDimension extends VaultEvent{
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
        playerVault.putString("Dimension", playerData.getLastDimension().location().toString());
        playerVault.put("Pos", playerData.getLastPos());
        playerVault.put("Rotation", playerData.getLastRot());

        inventoryVaults.put(vaultKey, playerVault);
        LOGGER.info("5.1  End of saveVault");
    }


    @Override
    protected void loadVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getLoadVaultKey();

        CompoundTag playerVault = getVault(player, vaultKey);
        ServerPlayer serverPlayer = (ServerPlayer) player;
        

        clearInventoryOnEmptyVault(player, playerVault);

        if (!validPlayerVaultLocation(serverPlayer, playerVault)) return;

        player.load(playerVault); /*Inventory, EnderItems, ForgeCaps, ForgeData, Attributes*/
        
        loadAdditionalData(serverPlayer, playerVault);
        
        if (teleportPlayer) {
            String playerDataDim = playerData.getCurrentDimension().location().toString();
            String playerVaultDim = playerVault.getString("Dimension");
            if (!playerDataDim.equals(playerVaultDim)) {
                LOGGER.error("5.2  VaultEventDimension.loadVault.teleport: teleport destination is in a different dimension");
                return;
            }

            teleportToLocation(serverPlayer, playerVault);

        }
        
        LOGGER.info("5.2  End of loadVault");
    }
}


