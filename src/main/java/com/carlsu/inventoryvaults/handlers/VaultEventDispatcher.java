package com.carlsu.inventoryvaults.handlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.carlsu.inventoryvaults.events.UpdateVaultEvent;
import com.carlsu.inventoryvaults.events.VaultEventCommand;
import com.carlsu.inventoryvaults.events.VaultEventDimension;
import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = InventoryVaults.MODID)
public final class VaultEventDispatcher implements IVaultData, CreativeDimension{
    public static final Map<UUID, Queue<UpdateVaultEvent>> eventQueue = new HashMap<>();
    public static final Map<UUID, Boolean> uuidQueueStatus = new HashMap<>();
    
    @SubscribeEvent
    public static void onEventReceived(UpdateVaultEvent event) throws InterruptedException{
        LOGGER.info("3 VaultEventDispatcher:");
        LOGGER.info("3   Type: " + event.getEventType().getValue());
        // LOGGER.info("\tActiveVaultKey: "+event.getPlayerData().getActiveVaultKey());
        // LOGGER.info("\tPreviousVaultKey: "+event.getPlayerData().getPreviousVaultKey());
        // LOGGER.info("\tsaveVaultKey: "+event.getSaveVaultKey());
        // LOGGER.info("\tloadVaultKey: "+event.getLoadVaultKey());
        UUID uuid = event.getPlayerData().getUUID();

        if (shouldQueueEvent(uuid)) {
            LOGGER.info("3   Event queued");
            // Queue the event for later execution.
            eventQueue.computeIfAbsent(uuid, k -> new LinkedList<>()).add(event);
        } else {
            LOGGER.info("3   Event dispatched");
            // Dispatch event
            executeEvent(event);
        }
    }
    
    public static boolean shouldQueueEvent(UUID uuid) {
        // If UUID is not present in the map, or its status is 'false' (not being processed), it should not be queued.
        return uuidQueueStatus.getOrDefault(uuid, false);
    }
    

    public static void executeEvent(UpdateVaultEvent event) {
        UUID uuid = event.getPlayerData().getUUID();

        // Mark the UUID as being processed
        uuidQueueStatus.put(uuid, true);
        LOGGER.info("4  VaultEventDispatcher.executeEvent");
        
        VaultType eventType = event.getEventType();
        
        // Manual trigger
        if (eventType == VaultType.MANUAL) {
            VaultEventCommand vaultEventCommand = new VaultEventCommand();
            if (event.getPlayerData().getSaveVaultKey() != null) {
                LOGGER.info("4.1  VaultType.MANUAL.execute");
                vaultEventCommand.execute(event.getPlayerData());
            } else {
                LOGGER.error("4.1  ! VaultType.MANUAL: saveVaultKey is null");
            }
        }
        
        // Dimension change trigger
        if (eventType == VaultType.DIMENSION_CHANGE) {
            VaultEventDimension vaultEventDimension = new VaultEventDimension();
            if (validDimensionChange(event.getPlayerData())) {
                LOGGER.info("4.2  VaultType.DIMENSION_CHANGE.execute");
                vaultEventDimension.execute(event.getPlayerData());
            } else {
                LOGGER.info("4.2  ! VaultType.DIMENSION_CHANGE: Save and Load vaults are the same, aborting");
            }
        }

        // Gamemode change trigger
        if (eventType == VaultType.GAMEMODE_CHANGE) {
            LOGGER.info("4.1  How did you get here?");
        }
    

        // Mark the UUID as done being processed
        LOGGER.info("6  VaultEventDispatcher.executeEvent -> uuidQueueStatus set to false");
        uuidQueueStatus.put(uuid, false);
    
        // Process the next event in the queue for the same UUID, if any
        Queue<UpdateVaultEvent> queue = eventQueue.get(uuid);
        // Queue not null and not empty
        if (queue != null && !queue.isEmpty()) {
            // Dispatch the next event
            executeEvent(queue.poll());
        }
    }

    public static boolean validDimensionChange(PlayerData playerData) {
        // Stops the event from triggering if the player changed dimensions from a manual trigger
        LOGGER.info("4.1  VaultEventDispatcher.validDimensionChange");
        LOGGER.info("4.1    saveVaultKey: " + playerData.getSaveVaultKey());
        LOGGER.info("4.1    loadVaultKey: " + playerData.getLoadVaultKey());
        LOGGER.info("4.1    activeVaultKey: " + playerData.getActiveVaultKey());
        LOGGER.info("4.1    previousVaultKey: " + playerData.getPreviousVaultKey());
        LOGGER.info("4.1    lastDimension: " + playerData.getLastDimension().location().toString());
        LOGGER.info("4.1    currentDimension: " + playerData.getCurrentDimension().location().toString());
        boolean hasChangedSave = !playerData.getSaveVaultKey().equals(playerData.getLoadVaultKey());
        boolean hasChangedDimension = !playerData.getCurrentDimension().equals(playerData.getLastDimension());

        boolean activeKeyEqualsSaveKey = playerData.getSaveVaultKey().equals(playerData.getActiveVaultKey());
        // Has changed save         &&      has changed dimension -> true
        // Has changed save         &&      has not changed dimension -> false
        // Has not changed save     &&      has changed dimension -> false
        // Has not changed save     &&      has not changed dimension -> false

        boolean validDimensionChange = activeKeyEqualsSaveKey && hasChangedSave && hasChangedDimension;

        return validDimensionChange;
    }

}
