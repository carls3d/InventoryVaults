package com.carlsu.inventoryvaults.events;

import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.VaultHandler;
import com.carlsu.inventoryvaults.util.UpdateVaultEvent;
import com.carlsu.inventoryvaults.util.VaultsData;
import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;


@EventBusSubscriber
public class PlayerEventHandler {
    // new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/test");
    static final Logger LOGGER = LogUtils.getLogger();
    static final String VAULT_NAME = VaultsData.VAULT_NAME;

    // Vaults
    static final String DEFAULT_VAULT = VaultsData.DEFAULT_VAULT;
    static final String CREATIVE_VAULT = ModDimension.CREATIVE_VAULT;

    // Dimensions
    static final ResourceKey<Level> CREATIVE_KEY = ModDimension.CREATIVE_KEY;
    
    // Players that died in creative dimension -> respawn triggers load main vault
    private static final HashMap<UUID, Boolean> mapDiedInCreativeDimension = new HashMap<>();
    public static final HashMap<UUID, Boolean> mapRespawnInCreative = new HashMap<>();
    public static final HashMap<UUID, Vec3> mapLastLocation = new HashMap<>();

    // Custom on dimension change event
    @SubscribeEvent
    public static void onUpdateVaultEvent(UpdateVaultEvent event) {
        LOGGER.info("UpdateVaultEvent");

        Player player = event.getPlayer();
        CompoundTag lastLocationData = event.getLastLocationNBT();
        
        // Skip if player wants to respawn in creative dimension
        if (mapDiedInCreativeDimension.getOrDefault(player.getUUID(), false)) {
            LOGGER.info("Skip UpdateVaultEvent");
            return;
        }
        

        if (event.getLastCapturedDimension() == CREATIVE_KEY) {
            fromCreativeDimension(player, false, lastLocationData);
        } else if (event.getCurrentDimension() == CREATIVE_KEY) {
            toCreativeDimension(player, false, lastLocationData);
        }
    }


    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        String prevDimension = event.getOriginal().getCommandSenderWorld().dimension().location().getPath();
        String dimension = event.getPlayer().getCommandSenderWorld().dimension().location().getPath();
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
                CompoundTag inventoryVaults = originalForgeData.getCompound(VAULT_NAME);

                mapDiedInCreativeDimension.put(respawnPlayer.getUUID(), true);
                respawnPlayer.serializeNBT().put("Pos", inventoryVaults.getCompound("main").get("Pos"));

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
        if (mapDiedInCreativeDimension.getOrDefault(event.getPlayer().getUUID(), false)) {
            LOGGER.info("PlayerEvent.PlayerRespawnEvent -> mapDiedInCreativeDimension");
            VaultHandler.loadVault(event.getPlayer(), VaultsData.DEFAULT_VAULT, true);
            mapDiedInCreativeDimension.put(event.getPlayer().getUUID(), false);
        }
    }
    
    
    public static void toCreativeDimension(Player player, Boolean changeDimension, CompoundTag lastLocationData) {
        VaultHandler.saveVault(player, VaultsData.DEFAULT_VAULT, lastLocationData);
        VaultHandler.loadVault(player, ModDimension.CREATIVE_VAULT, changeDimension);
    }

    public static void fromCreativeDimension(Player player, Boolean changeDimension, CompoundTag lastLocationData) {
        VaultHandler.saveVault(player, ModDimension.CREATIVE_VAULT, lastLocationData);
        VaultHandler.loadVault(player, VaultsData.DEFAULT_VAULT, changeDimension);
    }



    public static  void broadcastMessage(String message, Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.getPlayerList().broadcastMessage(new TextComponent(message), ChatType.CHAT, player.getUUID());
    }
}
