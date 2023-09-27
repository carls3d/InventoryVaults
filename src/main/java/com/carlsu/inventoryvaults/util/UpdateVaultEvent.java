package com.carlsu.inventoryvaults.util;

import java.util.UUID;

import org.lwjgl.system.CallbackI.S;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.server.ServerLifecycleHooks;

public class UpdateVaultEvent extends Event{
    private final UUID playerUUID;
    private final Vec3 lastCapturedPos;
    private final Vec2 lastCapturedRot;
    private final ResourceKey<Level> lastCapturedDimension;
    private final ResourceKey<Level> currentDimension;

    public UpdateVaultEvent(UUID uuid, Vec3 lastCapturedPos, Vec2 lastCapturedRot, ResourceKey<Level> lastCapturedDimension, ResourceKey<Level> currentDimension) {
        this.playerUUID = uuid;
        this.lastCapturedPos = lastCapturedPos;
        this.lastCapturedRot = lastCapturedRot;
        this.lastCapturedDimension = lastCapturedDimension;
        this.currentDimension = currentDimension;
    }

    public Player getPlayer() {
        Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);
        return player;
    }
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public Vec3 getLastCapturedPos() {
        return lastCapturedPos;
    }
    public Vec2 getLastCapturedRot() {
        return lastCapturedRot;
    }
    public ResourceKey<Level> getLastCapturedDimension() {
        return lastCapturedDimension;
    }
    public ResourceKey<Level> getCurrentDimension() {
        return currentDimension;
    }
    public CompoundTag getLastLocationNBT() {
        CompoundTag lastLocation = new CompoundTag();
        ListTag posList = new ListTag();
        ListTag rotList = new ListTag();
        posList.add(DoubleTag.valueOf(lastCapturedPos.x));
        posList.add(DoubleTag.valueOf(lastCapturedPos.y));
        posList.add(DoubleTag.valueOf(lastCapturedPos.z));
        rotList.add(FloatTag.valueOf(lastCapturedRot.x));
        rotList.add(FloatTag.valueOf(lastCapturedRot.y));
        lastLocation.put("Pos", posList);
        lastLocation.put("Rotation", rotList);
        lastLocation.putString("Dimension", currentDimension.location().toString());
        return lastLocation;
    }
}
