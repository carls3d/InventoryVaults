package com.carlsu.inventoryvaults.util;

import java.util.HashMap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerData {
    static final ResourceKey<Level> CREATIVE_LEVEL = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("_dimensions", "creative"));
    
    private static final long captureInterval = 100; // 0.1 seconds in milliseconds (1000 milliseconds = 1 second)
    private final HashMap<Player, Long> lastCaptureTime = new HashMap<>();
    private final HashMap<Player, Vec3> lastCapturedPos = new HashMap<>();
    private final HashMap<Player, Vec2> lastCapturedRot = new HashMap<>();
    private final HashMap<Player, ResourceKey<Level>> lastCapturedDimension = new HashMap<>();

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            ResourceKey<Level> currentDimension = player.level.dimension();

            long currentTime = System.currentTimeMillis();
            long lastTime = lastCaptureTime.getOrDefault(player, 0L);
            ResourceKey<Level> lastDimension = lastCapturedDimension.getOrDefault(player, null);

            if (currentTime - lastTime >= captureInterval) {

                if (lastDimension == null || lastDimension.equals(currentDimension)) {
                    Vec3 currentPosition = player.position();
                    Vec2 currentRotation = new Vec2(player.getXRot(), player.getYRot());
    
                    lastCapturedPos.put(player, currentPosition);
                    lastCapturedRot.put(player, currentRotation);
                    lastCapturedDimension.put(player, currentDimension);
                    lastCaptureTime.put(player, currentTime);
                    
                } else if (lastDimension != null && !lastDimension.equals(currentDimension)) {
                    checkIfCreativeDimension(player, lastDimension, currentDimension);
                    
                    // Reset lock
                    lastCapturedDimension.put(player, currentDimension);
                }
            }
        }
    }

    


    public void checkIfCreativeDimension(Player player, ResourceKey<Level> lastDimension, ResourceKey<Level> currentDimension) {
        if (lastDimension == null || lastDimension.equals(currentDimension)) {
            return;
        }
        if (lastDimension == CREATIVE_LEVEL) {
            UpdateVaultEvent updateVaultEvent = new UpdateVaultEvent(player, lastCapturedPos.get(player), lastCapturedRot.get(player), lastCapturedDimension.get(player), currentDimension);
            MinecraftForge.EVENT_BUS.post(updateVaultEvent);
            // fromCreativeDimension(player);
        }
        if (currentDimension == CREATIVE_LEVEL) {
            fireUpdateVaultEvent(player, lastCapturedPos.get(player), lastCapturedRot.get(player), lastCapturedDimension.get(player), currentDimension);
            // toCreativeDimension(player);
        }
        
        

    }


    public void fireUpdateVaultEvent(Player player, Vec3 lastCapturedPos, Vec2 lastCapturedRot, ResourceKey<Level> lastCapturedDimension, ResourceKey<Level> currentDimension) {
        UpdateVaultEvent updateVaultEvent = new UpdateVaultEvent(player, lastCapturedPos, lastCapturedRot, lastCapturedDimension, currentDimension);
        MinecraftForge.EVENT_BUS.post(updateVaultEvent);
    }


}
