package com.carlsu.inventoryvaults.handlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.carlsu.inventoryvaults.events.UpdateVaultEvent;
import com.carlsu.inventoryvaults.events.VaultEvent;
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

    public static final Map<VaultType, VaultEvent> eventMap = new HashMap<>();
    static {
        eventMap.put(VaultType.MANUAL, new VaultEventCommand());
        eventMap.put(VaultType.DIMENSION_CHANGE, new VaultEventDimension());
        // eventMap.put(VaultType.GAMEMODE_CHANGE, null);
    }
    
    @SubscribeEvent
    public static void onEventReceived(UpdateVaultEvent event) throws InterruptedException{
        UUID uuid = event.getPlayerData().getUUID();
        String playerName = event.getPlayerData().getPlayer().getName().getString();

        if (shouldQueueEvent(uuid)) {
            LOGGER.info(" UpdateVaultEvent -> "+playerName+": queued...");
            // Queue the event for later execution.
            eventQueue.computeIfAbsent(uuid, k -> new LinkedList<>()).add(event);
        } else {
            // Dispatch event
            executeEvent(event);
        }
    }
    
    public static boolean shouldQueueEvent(UUID uuid) {
        // If UUID is not present in the map, or its status is 'false' (not being processed), it should not be queued.
        return uuidQueueStatus.getOrDefault(uuid, false);
    }
    

    public static void executeEvent(UpdateVaultEvent event) {
        PlayerData playerData = event.getPlayerData();
        UUID uuid = playerData.getUUID();
        
        // Mark the UUID as being processed
        uuidQueueStatus.put(uuid, true);
        
        // Execute the event
        VaultEvent vaultEvent = eventMap.get(event.getEventType());
        if (vaultEvent != null) {
            vaultEvent.execute(playerData);
        } else {
            LOGGER.error("VaultEventDispatcher: No event found for type: " + event.getEventType().getValue());
        }

        // Mark the UUID as done being processed
        uuidQueueStatus.put(uuid, false);
    
        // Process the next event in the queue for the same UUID, if any
        Queue<UpdateVaultEvent> queue = eventQueue.get(uuid);
        // Queue not null and not empty -> dispatch the next event
        if (queue != null && !queue.isEmpty()) {
            executeEvent(queue.poll());
        }
    }
  

}
