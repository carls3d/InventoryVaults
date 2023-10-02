package com.carlsu.inventoryvaults.handlers;

import java.util.HashMap;
import java.util.UUID;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.carlsu.inventoryvaults.events.UpdateVaultEvent;
import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventBusSubscriber(modid = InventoryVaults.MODID)
public final class PlayerTickHandler implements CreativeDimension, IVaultData{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long captureInterval = 250; // 0.25 seconds in milliseconds (1000 milliseconds = 1 second)

    private static final HashMap<UUID, Long> mapLastTime = new HashMap<>();
    private static final HashMap<UUID, PlayerData> mapPlayerData = new HashMap<>();
    // private static final HashMap<UUID, ResourceKey<Level>> mapLastDimension = new HashMap<>();
    // private static final HashMap<UUID, ResourceKey<Level>> mapCurrentDimension = new HashMap<>();
    // private static final HashMap<UUID, ListTag> mapLastPos = new HashMap<>();
    // private static final HashMap<UUID, ListTag> mapLastRot = new HashMap<>();
    private static final VaultType eventTypeDimensionChange = VaultType.fromString("DimensionChange");

    // private static final HashMap<UUID, Vec3> mapLastPos = new HashMap<>();
    // private static final HashMap<UUID, Vec2> mapLastRot = new HashMap<>();
    // private static final HashMap<UUID, ResourceKey<Level>> mapLastDimension = new HashMap<>();

    

    public static boolean debugTick = false;
    
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            UUID uuid = player.getUUID();
            long currentTime = System.currentTimeMillis();
            long lastTime = mapLastTime.getOrDefault(uuid, 0L);
            
            if (currentTime - lastTime >= captureInterval) {
                ResourceKey<Level> playerDimension = player.level.dimension();
                if (playerDimension == null) return;
                
                PlayerData playerData = mapPlayerData.get(uuid);
                if (playerData == null) {
                    LOGGER.info("onPlayerTick.playerData == null, creating new PlayerData");
                    playerData = new PlayerData(player, playerDimension);
                    mapPlayerData.put(uuid, playerData);
                }
                playerData.updateCurrentDimension(player);

                // ORDER MATTERS
                if (playerData.hasChangedDimension()) {
                    LOGGER.info("onPlayerTick.hasChangedDimension()");
                    playerData.updateActiveVaultKey();
                    PlayerData playerDataCopy = playerData.copy();
                    playerData.updateLastDimension();
                    checkIfCreativeDimension(playerDataCopy);
                } else {
                    playerData.updateLastPos(player);
                    playerData.updateLastRot(player);
                    playerData.updateLastDimension();
                    mapPlayerData.put(uuid, playerData);
                }
                
                mapLastTime.put(uuid, currentTime);
            }
        }
    }


    public static void checkIfCreativeDimension(PlayerData playerData) {
        Player player = playerData.getPlayer();

        if (playerData.getLastDimension() == null) {
            String playerName = player != null ? player.getName().getString() : "!missing player!";
            LOGGER.error("1 "+playerName+" -> Invalid dimension change:");
            LOGGER.error("1    Last dimension: "+ playerData.getLastDimension());
            LOGGER.error("1    Current dimension: "+ playerData.getCurrentDimension());
        }
        // Only if last dimension is not equal to current dimension
        LOGGER.info("2 checkIfCreativeDimension");
        LOGGER.info("2   Last dimension: "+ playerData.getLastDimension().location().getPath());
        LOGGER.info("2   Current dimension: "+ playerData.getCurrentDimension().location().getPath());
        // LOGGER.info("2     ActiveVaultKey: "+ playerData.getActiveVaultKey());
        // LOGGER.info("2     PreviousVaultKey: "+ playerData.getPreviousVaultKey());
        // LOGGER.info("\t    SaveVaultKey: "+ playerData.getSaveVaultKey());
        // LOGGER.info("\t    LoadVaultKey: "+ playerData.getLoadVaultKey());
        // LOGGER.info("\tPrevLocation: "+ playerData.getLastPos());
        // LOGGER.info("\tPrevRotation: "+ playerData.getLastRot());



        if (playerData.getCurrentDimension() == CREATIVE_KEY) {
            String activeVaultKey = playerData.getActiveVaultKey();

            playerData.setSaveVaultKey((!VaultUtils.validKey(activeVaultKey) || activeVaultKey.equals(CREATIVE_VAULT)) ? DEFAULT_VAULT : activeVaultKey);
            playerData.setLoadVaultKey(CREATIVE_VAULT);
            LOGGER.info("2   currentDimension == CREATIVE_KEY");
            fireUpdateVaultEventDimension(playerData, eventTypeDimensionChange);
        }
        else if (playerData.getLastDimension() == CREATIVE_KEY) {
            String previousVaultKey = playerData.getPreviousVaultKey();

            playerData.setSaveVaultKey(CREATIVE_VAULT);
            playerData.setLoadVaultKey((!VaultUtils.validKey(previousVaultKey) || previousVaultKey.equals(CREATIVE_VAULT)) ? DEFAULT_VAULT : previousVaultKey);
            LOGGER.info("2   lastDimension == CREATIVE_KEY");
            fireUpdateVaultEventDimension(playerData, eventTypeDimensionChange);
        }
        
    }


    public static void fireUpdateVaultEventDimension(PlayerData playerData, VaultType eventType) {
        MinecraftForge.EVENT_BUS.post(new UpdateVaultEvent(playerData, eventType));
    }
}