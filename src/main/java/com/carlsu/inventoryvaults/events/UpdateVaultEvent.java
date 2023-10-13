package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;

import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.server.ServerLifecycleHooks;

public class UpdateVaultEvent extends Event {
    private final PlayerData playerData;
    private final VaultType EVENT_TYPE;
    private final String saveVaultKey;
    private final String loadVaultKey;

    public UpdateVaultEvent(PlayerData playerData, VaultType eventType) {
        this.playerData = playerData;
        this.EVENT_TYPE = eventType;
        this.saveVaultKey = playerData.getSaveVaultKey();
        this.loadVaultKey = playerData.getLoadVaultKey();
    }
   
    public String getSaveVaultKey() {
        return saveVaultKey;
    }
    public String getLoadVaultKey() {
        return loadVaultKey;
    }
    public Player getPlayer() {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerData.getUUID());
    }
    public PlayerData getPlayerData() {
        return playerData;
    }
    public String getActiveVaultKey() {
        return playerData.getActiveVaultKey();
    }
    public ListTag getLastCapturedPos() {
        return playerData.getLastPos();
    }
    public ListTag getLastCapturedRot() {
        return playerData.getLastRot();
    }
    public ResourceKey<Level> getLastCapturedDimension() {
        return playerData.getLastDimension();
    }
    public ResourceKey<Level> getCurrentDimension() {
        return playerData.getCurrentDimension();
    }
    public VaultType getEventType() {
        return EVENT_TYPE;
    }
}
