package com.carlsu.inventoryvaults.types;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerData implements IVaultData{
    // Keep track of player's current vault to prevent clashing events
    private final UUID playerUUID;
    private String saveVaultKey;
    private String loadVaultKey;
    private String activeVaultKey;
    private String previousVaultKey;
    private ListTag lastPos;
    private ListTag lastRot;
    @Nonnull private ResourceKey<Level> currentDimension;
    private ResourceKey<Level> lastDimension;

    public PlayerData (@Nonnull Player player, @Nonnull ResourceKey<Level> currentDimension) {
        this.playerUUID = player.getUUID();
        this.currentDimension = currentDimension;
        this.lastDimension = this.currentDimension;
        this.updateActiveVaultKey();
    }

    private PlayerData (
            UUID playerUUID, 
            String saveVaultKey, 
            String loadVaultKey, 
            String activeVaultKey, 
            String previousVaultKey, 
            ListTag lastPos, 
            ListTag lastRot, 
            ResourceKey<Level> lastDimension, 
            @Nonnull ResourceKey<Level> currentDimension
            ) {
        this.playerUUID = playerUUID;
        this.saveVaultKey = saveVaultKey;
        this.loadVaultKey = loadVaultKey;
        this.activeVaultKey = activeVaultKey;
        this.previousVaultKey = previousVaultKey;
        this.lastPos = lastPos;
        this.lastRot = lastRot;
        this.lastDimension = lastDimension;
        this.currentDimension = currentDimension;
    }
    
    public PlayerData copy() {
        return new PlayerData(
            this.playerUUID, 
            this.saveVaultKey, 
            this.loadVaultKey, 
            this.activeVaultKey, 
            this.previousVaultKey, 
            this.lastPos, 
            this.lastRot, 
            this.lastDimension, 
            this.currentDimension
            );
    }
    public void setSaveVaultKey(String saveVaultKey) {
        this.saveVaultKey = saveVaultKey;
    }
    public void setLoadVaultKey(String loadVaultKey) {
        this.loadVaultKey = loadVaultKey;
    }
    public String getSaveVaultKey() {
        this.saveVaultKey = VaultUtils.ifElseValidKey(saveVaultKey, saveVaultKey, getActiveVaultKey());
        // return saveVaultKey != null ? saveVaultKey : getActiveVaultKey();
        return this.saveVaultKey;
    }
    public String getLoadVaultKey() {
        return loadVaultKey;
    }
    public ServerPlayer getPlayer() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.getPlayerList().getPlayer(playerUUID);
    }
    public String getActiveVaultKey() {
        if (!VaultUtils.validKey(activeVaultKey)) {
            LOGGER.info("getActiveVaultKey -> activeVaultKey is null, updating");
            updateActiveVaultKey();
        }
        // Set this.activeVaultKey to "main" if (null or "") (Does not set in NBT)
        this.activeVaultKey = VaultUtils.ifElseValidKey(activeVaultKey, activeVaultKey, DEFAULT_VAULT);
        return this.activeVaultKey;
    }    
    public String getPreviousVaultKey() {
        if (!VaultUtils.validKey(this.previousVaultKey)) {
            updatePreviousVaultKey();
        }
        return this.previousVaultKey;
    }
    public UUID getUUID() {
        return playerUUID;
    }
    public ListTag getLastPos() {
        return lastPos;
    }
    public ListTag getLastRot() {
        return lastRot;
    }
    public ResourceKey<Level> getLastDimension() {
        return lastDimension;
    }
    public ResourceKey<Level> getCurrentDimension() {
        return currentDimension;
    }

    public void setLastPos(ListTag lastPos) {
        this.lastPos = lastPos;
    }
    public void setLastRot(ListTag lastRot) {
        this.lastRot = lastRot;
    }
    public void setLastDimension(@Nonnull ResourceKey<Level> lastDimension) {
        this.lastDimension = lastDimension;
    }
    public void setCurrentDimension(@Nonnull ResourceKey<Level> currentDimension) {
        this.currentDimension = currentDimension;
    }

    public void setActiveVaultKey(String vaultKey) {
        this.activeVaultKey = vaultKey;
    }
    public void setPreviousVaultKey(String vaultKey) {
        this.previousVaultKey = vaultKey;
    }

    public void updateActiveVaultKey() {
        String nbtActiveVaultKey = VaultUtils.PlayerVaultData.getString(getPlayer(), ACTIVE_VAULT);
        // LOGGER.info("updateActiveVaultKey -> nbtActiveVaultKey: "+ nbtActiveVaultKey);
        if (VaultUtils.validKey(nbtActiveVaultKey)) {
            this.activeVaultKey = nbtActiveVaultKey;
        } else {
            // LOGGER.info("updateActiveVaultKey -> nbtActiveVaultKey is null: '"+nbtActiveVaultKey+"', setting nbt to DEFAULT_VAULT");
            VaultUtils.PlayerVaultData.setString(getPlayer(), ACTIVE_VAULT, DEFAULT_VAULT);
            this.activeVaultKey = DEFAULT_VAULT;
        }
    }

    public void updatePreviousVaultKey() {
        String nbtPreviousVaultKey = VaultUtils.PlayerVaultData.getString(getPlayer(), PREVIOUS_VAULT);
        if (VaultUtils.validKey(nbtPreviousVaultKey)) {
            this.previousVaultKey = nbtPreviousVaultKey;
        }
    }

    public CompoundTag getPreviousLocation() {
        CompoundTag location = new CompoundTag();
        location.putString("Dimension", this.lastDimension.location().toString());
        location.put("Pos", this.lastPos);
        location.put("Rotation", this.lastRot);
        return location;
    }
    public void updateLocation(Player player) {
        updateLastPos(player);
        updateLastRot(player);
        updateLastDimension();
    }
    public void updateLastPos(Player player) {
        Vec3 pos = player.position();
        this.lastPos = new ListTag();
        this.lastPos.add(DoubleTag.valueOf(pos.x));
        this.lastPos.add(DoubleTag.valueOf(pos.y));
        this.lastPos.add(DoubleTag.valueOf(pos.z));
    }
    public void updateLastRot(Player player) {
        Vec2 rot = player.getRotationVector();
        this.lastRot = new ListTag();
        this.lastRot.add(FloatTag.valueOf(rot.y));
        this.lastRot.add(FloatTag.valueOf(rot.x));
    }
    public void updateCurrentDimension(Player player) {
        ResourceKey<Level> dimension = player.level.dimension();
        this.currentDimension = Objects.requireNonNull(dimension, "Current dimension cannot be null");
    }
    public void updateLastDimension() {
        this.lastDimension = this.currentDimension;
    }
    public Boolean hasChangedDimension() {
        // if (!this.lastDimension.equals(this.currentDimension)) {
        //     LOGGER.info("hasChangedDimension -> true");
        //     LOGGER.info("lastDimension: "+ this.lastDimension.location().toString());
        //     LOGGER.info("currentDimension: "+ this.currentDimension.location().toString());
        // }
        return !this.lastDimension.equals(this.currentDimension);
    }

}
