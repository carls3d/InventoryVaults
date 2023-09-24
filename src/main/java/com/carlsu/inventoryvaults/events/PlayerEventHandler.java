package com.carlsu.inventoryvaults.events;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.util.UpdateVaultEvent;
import com.carlsu.inventoryvaults.util.VaultsData;
import com.mojang.logging.LogUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.server.ServerLifecycleHooks;


@EventBusSubscriber
public class PlayerEventHandler {
    // new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/test");
    static final ResourceKey<Level> CREATIVE_LEVEL = VaultsData.CREATIVE_LEVEL;
    static final Logger LOGGER = LogUtils.getLogger();
    static final String VAULT_NAME = VaultsData.VAULT_NAME;
    static final String DEFAULT_VAULT = VaultsData.DEFAULT_VAULT;
    static final String CREATIVE_VAULT = VaultsData.CREATIVE_VAULT;
    
    
    @SubscribeEvent
    public static void onUpdateVaultEvent(UpdateVaultEvent event) {
        String rot = event.getLastCapturedRot().x+", "+event.getLastCapturedRot().y;
        LOGGER.info(
            "\n\nUpdateVaultEvent:"+
            "\n\tCurrent pos: "+ event.getCurrentPlayer().position().toString() +
            "\n\tLast pos: "+ event.getLastCapturedPos().toString() +
            "\n\tLast rot: "+ rot +
            "\n\tLast dimension: "+ event.getLastCapturedDimension().location().getPath() +
            "\n\tCurrent dimension: "+ event.getCurrentDimension().location().getPath() +
            "\n"
        );
    }

    @SubscribeEvent
    public static void playerDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof ServerPlayer sp) {
            LOGGER.info(
                "\n\nLivingDeathEvent:"+
                "\n\tPlayer dim: "+ sp.getCommandSenderWorld().dimension().location().getPath()+
                "\n\tPlayer pos: "+ sp.position().toString() +
                "\n"
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        String prevDimension = event.getOriginal().getCommandSenderWorld().dimension().location().getPath();
        String dimension = event.getPlayer().getCommandSenderWorld().dimension().location().getPath();
        LOGGER.info(
            "\n\nPlayerEvent.Clone:"+
            "\n\tPrev: "+prevDimension+
            "\n\tNew: "+dimension +
            "\n"
            );
        

        // Check if the player is actually dying, not just changing dimensions
        if (event.isWasDeath()) {
            Player originalPlayer = event.getOriginal();
            Player respawnPlayer = event.getPlayer();

            
            CompoundTag originalForgeData = originalPlayer.getPersistentData(); // The original player entity that has died
            CompoundTag spawnForgeData = respawnPlayer.getPersistentData();     // The new player entity that will spawn

            // Check if died in creative dimension -> Respawn in creative dimension or restore main vault on respawn
            if (event.getOriginal().getCommandSenderWorld().dimension() == CREATIVE_LEVEL) {
                LOGGER.info(
                    "\n\nDied in creative dimension"+
                    "\n\tOriginal player pos: "+originalPlayer.position().toString()+
                    "\n\tNew player pos: "+respawnPlayer.position().toString()+
                    "\n\toriginal isDeadOrDying: "+originalPlayer.isDeadOrDying()+
                    "\n\trespawn isDeadOrDying: "+respawnPlayer.isDeadOrDying() +
                    "\n"
                );
            } else {
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
        // 1. If died in creative dimension, respawn in creative dimension
        // 2. If died in creative dimension, run load main vault
        String dimension = event.getPlayer().getCommandSenderWorld().dimension().location().getPath();
        LOGGER.info(
            "\n\nPlayerEvent.PlayerRespawnEvent:"+
            "\n\tNew: "+dimension,
            "\n\tisEndConquered: "+event.isEndConquered() +
            "\n"
            );
    }
    
    
    // private static void toCreativeDimension(Player player) {
    //     VaultHandler.saveVault(player, DEFAULT_VAULT);
    //     VaultHandler.loadVault(player, CREATIVE_VAULT, false);
    // }
    // private static void fromCreativeDimension(Player player) {
    //     VaultHandler.saveVault(player, CREATIVE_VAULT);
    //     VaultHandler.loadVault(player, DEFAULT_VAULT, false);
    // }



    public static  void broadcastMessage(String message, Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        server.getPlayerList().broadcastMessage(new TextComponent(message), ChatType.CHAT, player.getUUID());
    }
}
