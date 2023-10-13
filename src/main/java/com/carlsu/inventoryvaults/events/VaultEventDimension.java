package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.VaultUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;


public class VaultEventDimension extends VaultEvent{
    public final Boolean teleportPlayer = true;

    public VaultEventDimension() {
        super(VaultType.DIMENSION_CHANGE);
    }

    @Override
    protected boolean isValidEvent(PlayerData playerData) {
        boolean hasChangedSave = !playerData.getSaveVaultKey().equals(playerData.getLoadVaultKey());
        boolean hasChangedDimension = !playerData.getCurrentDimension().equals(playerData.getLastDimension());
        boolean activeKeyEqualsSaveKey = playerData.getSaveVaultKey().equals(playerData.getActiveVaultKey());
        return hasChangedSave && hasChangedDimension && activeKeyEqualsSaveKey;
    }

    @Override
    protected void saveVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getSaveVaultKey();

        CompoundTag playerVault = filterVaultData(player);
        playerVault.merge(playerData.getPreviousLocation());
        VaultUtils.PlayerVaultData.setData(player, vaultKey, playerVault);
    }


    @Override
    protected void loadVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getLoadVaultKey();

        CompoundTag playerVault = getVault(player, vaultKey);
        ServerPlayer serverPlayer = (ServerPlayer) player;
        
        player.load(playerVault); /*Inventory, EnderItems, ForgeCaps, ForgeData, Attributes, etc..*/
        
        loadAdditionalData(serverPlayer, playerVault);
        
        if (teleportPlayer) {
            String playerDataDim = playerData.getCurrentDimension().location().toString();
            String playerVaultDim = playerVault.getString("Dimension");
            if (!playerDataDim.equals(playerVaultDim)) {
                LOGGER.warn("VaultEventDimension.loadVault.teleport: teleport destination is in a different dimension -> aborting");
                return;
            }

            teleportToLocation(serverPlayer, playerVault);

        }
    }

}


