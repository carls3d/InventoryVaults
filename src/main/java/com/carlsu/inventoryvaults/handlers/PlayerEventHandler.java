package com.carlsu.inventoryvaults.handlers;

import java.util.HashMap;
import java.util.UUID;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.IVaultData;

import net.minecraft.nbt.Tag;

import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;


@EventBusSubscriber(modid = InventoryVaults.MODID)
public final class PlayerEventHandler implements IVaultData, CreativeDimension{

    // Players that died in creative dimension -> respawn triggers load main vault
    private static final HashMap<UUID, Boolean> mapDiedInCreativeDimension = new HashMap<>();
    public static final HashMap<UUID, Boolean> mapRespawnInCreative = new HashMap<>();
    public static final VaultType eventTypeManual = VaultType.fromString("Manual"); 
    public static final VaultType eventTypeDimensionChange = VaultType.fromString("DimensionChange"); 
    public static final VaultType eventTypeGamemodeChange = VaultType.fromString("GamemodeChange");

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        LOGGER.info("directory: " + event.getPlayerDirectory());
        LOGGER.info("PlayerEvent.LoadFromFile");
    }
    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        LOGGER.info("directory: " + event.getPlayerDirectory());
        LOGGER.info("PlayerEvent.SaveToFile");
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Update player permission cache for commands -> enables/disables "/creative teleport"
        ServerPlayer player = (ServerPlayer) event.getPlayer();
        if (event.getFrom() == CREATIVE_KEY || event.getTo() == CREATIVE_KEY) {
            LOGGER.info("Updating player permission cache");
            player.server.getPlayerList().sendPlayerPermissionLevel(player);
        };
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        LOGGER.info("PlayerEvent.Clone");
        

        // Check if the player is actually dying, not just changing dimensions
        if (event.isWasDeath()) {
            Player originalPlayer = event.getOriginal();
            Player respawnPlayer = event.getPlayer();
            
            CompoundTag originalForgeData = originalPlayer.getPersistentData(); // The original player entity that has died
            CompoundTag spawnForgeData = respawnPlayer.getPersistentData();     // The new player entity that will spawn
            mapRespawnInCreative.putIfAbsent(originalPlayer.getUUID(), false);

            // Check if died in creative dimension -> Respawn in creative dimension or restore main vault on respawn
            if (event.getOriginal().getCommandSenderWorld().dimension() == CREATIVE_KEY) {
                CompoundTag originalInventoryVaults = originalForgeData.getCompound(VAULT_NAME);
                Tag originalMainVaultPos = originalInventoryVaults.getCompound(IVaultData.DEFAULT_VAULT).get("Pos");

                if (originalMainVaultPos != null) {
                    mapDiedInCreativeDimension.put(respawnPlayer.getUUID(), true);
                    respawnPlayer.serializeNBT().put("Pos", originalMainVaultPos);
                }

                //--- RESPAWN IN CREATIVE DIMENSION
                // if (mapRespawnInCreative.get(originalPlayer.getUUID())) {
                //     respawnPlayer.serializeNBT().put("Pos", ModDimension.CREATIVE_SPAWN);
                // }
            }

            //--- KEEP VAULTS ON DEATH
            if (originalForgeData.contains(VAULT_NAME)) {
                CompoundTag customData = originalForgeData.getCompound(VAULT_NAME); // Get the custom NBT data from the original player
                spawnForgeData.put(VAULT_NAME, customData); // Set the custom NBT data to the new player
            }
        } 
    }

    @SubscribeEvent 
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        //TODO Yet to be implemented
        // Would need to cancel bed spawn
        
        if (mapDiedInCreativeDimension.getOrDefault(event.getPlayer().getUUID(), false)) {
            LOGGER.info("PlayerEvent.PlayerRespawnEvent -> mapDiedInCreativeDimension");
//            VaultHandlerDimension.loadVault(event.getPlayer(), VaultData.DEFAULT_VAULT, true);
            mapDiedInCreativeDimension.put(event.getPlayer().getUUID(), false);
        }
    }
    

    public static  void broadcastMessage(String message, Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.getPlayerList().broadcastMessage(new TextComponent(message), ChatType.CHAT, player.getUUID());
    }
}
