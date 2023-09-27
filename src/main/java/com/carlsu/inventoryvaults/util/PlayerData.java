package com.carlsu.inventoryvaults.util;

import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class PlayerData {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final long captureInterval = 250; // 0.1 seconds in milliseconds (1000 milliseconds = 1 second)
    private static final HashMap<UUID, Long> mapLastTime = new HashMap<>();
    private static final HashMap<UUID, Vec3> mapLastPos = new HashMap<>();
    private static final HashMap<UUID, Vec2> mapLastRot = new HashMap<>();
    private static final HashMap<UUID, ResourceKey<Level>> mapLastDimension = new HashMap<>();

    public static boolean debugTick = false;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (debugTick) {LOGGER.info("PlayerData.onPlayerTick");}
            Player player = event.player;
            UUID playerUUID = player.getUUID();
            ResourceKey<Level> currentDimension = player.level.dimension();

            long currentTime = System.currentTimeMillis();
            long lastTime = mapLastTime.getOrDefault(playerUUID, 0L);
            ResourceKey<Level> lastDimension = mapLastDimension.getOrDefault(playerUUID, null);

            if (currentTime - lastTime >= captureInterval) {
                if (debugTick) {LOGGER.info("PlayerData.onPlayerTick: captureInterval");}
                if (lastDimension == null || lastDimension.equals(currentDimension)) {
                    if (debugTick) {LOGGER.info("PlayerData.onPlayerTick: updatePlayerData");}
                    Vec3 currentPosition = player.position();
                    Vec2 currentRotation = new Vec2(player.getXRot(), player.getYRot());
    
                    mapLastPos.put(playerUUID, currentPosition);
                    mapLastRot.put(playerUUID, currentRotation);
                    mapLastDimension.put(playerUUID, currentDimension);
                    mapLastTime.put(playerUUID, currentTime);
                    
                } else if (lastDimension != null && !lastDimension.equals(currentDimension)) {
                    if (debugTick) {LOGGER.info("PlayerData.onPlayerTick: checkIfCreativeDimension");}
                    checkIfCreativeDimension(playerUUID, lastDimension, currentDimension);
                    
                    // Reset lock
                    mapLastDimension.put(playerUUID, currentDimension);
                }
            }
        }
    }


    public void checkIfCreativeDimension(UUID uuid, ResourceKey<Level> lastDimension, ResourceKey<Level> currentDimension) {
        Player player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        LOGGER.info("checkIfCreativeDimension");
        if (lastDimension == null || lastDimension.equals(currentDimension)) {
            String playerName = player != null ? player.getName().getString() : "!missing player!";
            LOGGER.error(
                "\n\n"+playerName+"-> Invalid dimension change" +
                "\n\tLast dimension: "+ lastDimension+
                "\n\tCurrent dimension: "+ currentDimension+
                "\n");
            return;
        }
        if (lastDimension == ModDimension.CREATIVE_KEY) {
            fireUpdateVaultEvent(uuid, mapLastPos.get(uuid), mapLastRot.get(uuid), lastDimension, currentDimension);
        } 
        else if (currentDimension == ModDimension.CREATIVE_KEY) {
            fireUpdateVaultEvent(uuid, mapLastPos.get(uuid), mapLastRot.get(uuid), lastDimension, currentDimension);
        }
    }


    public void fireUpdateVaultEvent(UUID uuid, Vec3 mapLastPos, Vec2 mapLastRot, ResourceKey<Level> mapLastDimension, ResourceKey<Level> currentDimension) {
        UpdateVaultEvent updateVaultEvent = new UpdateVaultEvent(uuid, mapLastPos, mapLastRot, mapLastDimension, currentDimension);
        MinecraftForge.EVENT_BUS.post(updateVaultEvent);
    }


}
