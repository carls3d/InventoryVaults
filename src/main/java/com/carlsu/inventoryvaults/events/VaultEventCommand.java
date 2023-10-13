package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.VaultUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;


public class VaultEventCommand extends VaultEvent{

    public VaultEventCommand() {
        super(VaultType.MANUAL);
    }

    @Override
    protected boolean isValidEvent(PlayerData playerData) {
        return !playerData.getSaveVaultKey().isEmpty();
    }

    @Override
    protected void saveVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getSaveVaultKey();
        
        CompoundTag playerVault = filterVaultData(player);
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

        teleportToLocation(serverPlayer, playerVault);

    }
}

