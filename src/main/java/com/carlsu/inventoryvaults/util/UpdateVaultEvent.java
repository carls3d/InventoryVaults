package com.carlsu.inventoryvaults.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class UpdateVaultEvent extends Event{
    private final Player player;
    private final Vec3 lastCapturedPos;
    private final Vec2 lastCapturedRot;
    private final ResourceKey<Level> lastCapturedDimension;
    private final ResourceKey<Level> currentDimension;

    public UpdateVaultEvent(Player player, Vec3 lastCapturedPos, Vec2 lastCapturedRot, ResourceKey<Level> lastCapturedDimension, ResourceKey<Level> currentDimension) {
        this.player = player;
        this.lastCapturedPos = lastCapturedPos;
        this.lastCapturedRot = lastCapturedRot;
        this.lastCapturedDimension = lastCapturedDimension;
        this.currentDimension = currentDimension;
    }

    public Player getCurrentPlayer() {
        return player;
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
}
