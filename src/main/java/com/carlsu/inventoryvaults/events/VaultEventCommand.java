package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class VaultEventCommand extends VaultEvent{

    public VaultEventCommand() {
        super(VaultType.MANUAL);
    }


    @Override
    protected void saveVault(PlayerData playerData) {
        ServerPlayer player = playerData.getPlayer();
        String vaultKey = playerData.getSaveVaultKey();
        
        CompoundTag forgeData = player.getPersistentData();
        if (!forgeData.contains(VAULT_NAME)) {
            forgeData.put(VAULT_NAME, new CompoundTag());
        }
        CompoundTag inventoryVaults = forgeData.getCompound(VAULT_NAME);
        CompoundTag vaultData = filterVaultData(player);

        inventoryVaults.put(vaultKey, vaultData);
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

        teleportToLocation(serverPlayer, playerVault);

        LOGGER.info("5.2  End of loadVault");
    }
}

