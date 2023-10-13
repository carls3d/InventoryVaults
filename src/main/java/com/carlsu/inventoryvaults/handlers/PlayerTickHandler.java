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
    private static final VaultType eventTypeDimensionChange = VaultType.fromString("DimensionChange");

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
                // Create PlayerData for player if it doesn't exist
                if (playerData == null) {
                    playerData = new PlayerData(player, playerDimension);
                    mapPlayerData.put(uuid, playerData);
                }
                playerData.updateCurrentDimension(player);

                //* ORDER MATTERS
                if (playerData.hasChangedDimension()) {
                    playerData.updateActiveVaultKey();
                    PlayerData playerDataCopy = playerData.copy();
                    playerData.updateLastDimension();
                    checkIfCreativeDimension(playerDataCopy);
                } else {
                    playerData.updateLocation(player);
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
            LOGGER.error(playerName+" -> Invalid dimension change:");
            LOGGER.error("   Last dimension: " + playerData.getLastDimension().location().toString());
            LOGGER.error("   Current dimension: " + playerData.getCurrentDimension().location().toString());
        }
        // Only if last dimension is not equal to current dimension
        LOGGER.info("checkIfCreativeDimension");
        LOGGER.info("  Last dimension: "+ playerData.getLastDimension().location().toString());
        LOGGER.info("  Current dimension: "+ playerData.getCurrentDimension().location().toString());


        if (playerData.getCurrentDimension() == CREATIVE_KEY) {
            String activeVaultKey = playerData.getActiveVaultKey();

            playerData.setSaveVaultKey((!VaultUtils.validKey(activeVaultKey) || activeVaultKey.equals(CREATIVE_VAULT_KEY)) ? DEFAULT_VAULT : activeVaultKey);
            playerData.setLoadVaultKey(CREATIVE_VAULT_KEY);
            fireUpdateVaultEventDimension(playerData, eventTypeDimensionChange);
        }
        else if (playerData.getLastDimension() == CREATIVE_KEY) {
            String previousVaultKey = playerData.getPreviousVaultKey();

            playerData.setSaveVaultKey(CREATIVE_VAULT_KEY);
            playerData.setLoadVaultKey((!VaultUtils.validKey(previousVaultKey) || previousVaultKey.equals(CREATIVE_VAULT_KEY)) ? DEFAULT_VAULT : previousVaultKey);
            fireUpdateVaultEventDimension(playerData, eventTypeDimensionChange);
        }
        
    }


    public static void fireUpdateVaultEventDimension(PlayerData playerData, VaultType eventType) {
        MinecraftForge.EVENT_BUS.post(new UpdateVaultEvent(playerData, eventType));
    }
}
