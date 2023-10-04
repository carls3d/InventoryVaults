package com.carlsu.inventoryvaults.commands;

import java.util.HashMap;

import com.carlsu.inventoryvaults.events.UpdateVaultEvent;
import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.CommandUtils;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultContainer;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.ibm.icu.impl.Pair;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;

public class VaultCommands extends CommandUtils implements IVaultData{

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> vaultCommands = Commands.literal("Vaults")
            .requires(player -> player.hasPermission(2));


        // vaultCommands.then(
        // Commands.literal("debugTickEvents").executes(context -> {
        //         sendSuccess("Debug: " + PlayerTickHandler.debugTick);
        //         return 1;})
        //         .then(
        //     Commands.argument("debug", BoolArgumentType.bool()).executes(context -> {
        //             PlayerTickHandler.debugTick = BoolArgumentType.getBool(context, "debug");
        //             sendSuccess("Debug: " + PlayerTickHandler.debugTick);
        //             return 1;})
        //             ));
        
        
        vaultCommands
        // .then(
        // Commands.literal("dev")
        .then(
            Commands.literal("save").executes(context ->
                    commandSaveVault(context.getSource())).then(
                Commands.argument("VaultKey", StringArgumentType.string()).executes(context ->
                        commandSaveVault(context.getSource(), 
                            context.getSource().getPlayerOrException(), 
                            StringArgumentType.getString(context, "VaultKey"))).then(
                    Commands.argument("player", EntityArgument.player()).executes(context -> 
                            commandSaveVault(context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "VaultKey"))))))

        .then(
            Commands.literal("load").executes(context -> 
                    commandLoadVault(context.getSource())).then(
                Commands.argument("VaultKey", StringArgumentType.string()).executes(context ->
                        commandLoadVault(context.getSource(), 
                            context.getSource().getPlayerOrException(), 
                            StringArgumentType.getString(context, "VaultKey"))).then(
                    Commands.argument("player", EntityArgument.player()).executes(context -> 
                            commandLoadVault(context.getSource(),
                                EntityArgument.getPlayer(context, "player"),
                                StringArgumentType.getString(context, "VaultKey"))))))

        .then(
            Commands.literal("vaultsee").then(
                Commands.argument("Player", EntityArgument.player()).then(
                    Commands.argument("VaultKey", StringArgumentType.string()).executes(context -> 
                            vaultSee(context.getSource().getPlayerOrException(), 
                                StringArgumentType.getString(context, "VaultKey"),
                                EntityArgument.getPlayer(context, "Player"))))))

        .then(
            Commands.literal("list").then(
                Commands.argument("Player", EntityArgument.player()).executes(context -> 
                        listVaults(
                            context.getSource(), 
                            EntityArgument.getPlayer(context, "Player")))))

        .then(
            Commands.literal("types").then(
                Commands.argument("vaultKey", StringArgumentType.string()).executes(context -> {
                Player player = context.getSource().getPlayerOrException();
                String stringArg = StringArgumentType.getString(context, "vaultKey");
                sendSuccess(context.getSource(), "VaultTypes:");
                HashMap<String, Pair<Byte, TagType<?>>> tagTypes = VaultUtils.getTagTypes(player, stringArg);
                
                for (String key : tagTypes.keySet()) {
                    // Pair<Byte, TagType<?>> pair = tagTypes.get(key);
                    sendSuccess(context.getSource(), key + ": " + tagTypes.get(key).second.getPrettyName() + 
                    " -> " + tagTypes.get(key).second.getName() + " -> " + tagTypes.get(key).first);
                }
                return 1;})));
        dispatcher.register(vaultCommands);
    }



    static int commandSaveVault(CommandSourceStack source) throws CommandSyntaxException{
        return commandSaveVault(source, source.getPlayerOrException());
    }
    static int commandSaveVault(CommandSourceStack source, Player player) {
        return commandSaveVault(source, player, "main");
    }
    static int commandSaveVault(CommandSourceStack source, Player player, String vaultKey) {
        ResourceKey<Level> dimensionKey = player.level.dimension();
        if (dimensionKey == null) return 0;

        PlayerData playerData = new PlayerData(player, dimensionKey);
        playerData.setSaveVaultKey(vaultKey);
        MinecraftForge.EVENT_BUS.post(new UpdateVaultEvent(playerData, VaultType.MANUAL));
        sendSuccess(source, "Vault saved manually");
        return 1;
    }


    static int commandLoadVault(CommandSourceStack source) throws CommandSyntaxException{
        return commandLoadVault(source, source.getPlayerOrException());
    }
    static int commandLoadVault(CommandSourceStack source, Player player) {
        return commandLoadVault(source, player, "main");
    }
    static int commandLoadVault(CommandSourceStack source, Player player, String vaultKey) {
        ResourceKey<Level> dimensionKey = player.level.dimension();
        if (dimensionKey == null) return 0;
        
        PlayerData playerData = new PlayerData(player, dimensionKey);
        playerData.setLoadVaultKey(vaultKey);
        MinecraftForge.EVENT_BUS.post(new UpdateVaultEvent(playerData, VaultType.MANUAL));
        sendSuccess(source, new TextComponent("Vault loaded").withStyle(ChatFormatting.GREEN));
        return 1;
    }
    




    public static int vaultSee(ServerPlayer sourcePlayer, String vaultKey) {
        return vaultSee(sourcePlayer, vaultKey, null);
    }
    public static int vaultSee(ServerPlayer sourcePlayer, String vaultKey, ServerPlayer targetPlayer) {
        targetPlayer = targetPlayer == null ? sourcePlayer : targetPlayer;
        CompoundTag vaultData = VaultUtils.getVault(targetPlayer, vaultKey);
        ListTag inventoryListTag = vaultData.getList("Inventory", 10);

        // String menuName = targetPlayer.getName().getString()+ " vault: " + vaultKey;
        String menuName = targetPlayer.getName().getContents()+ " vault: " + vaultKey;

        sourcePlayer.openMenu(new MenuProvider() {
            @Override
			public Component getDisplayName() {
                return new TextComponent(menuName);
			}
			@Override
			public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player p) {
				return new ChestMenu(MenuType.GENERIC_9x5, id, playerInventory, new VaultContainer(inventoryListTag, 45), 5);
			}
        });
        return 1;
    }


    public static int listVaults(CommandSourceStack source, ServerPlayer sourcePlayer) {
        CompoundTag playerVaults = VaultUtils.getInventoryVaults(sourcePlayer);
        if (playerVaults == null) return 0;
        sendSuccess(source, new TextComponent("Vaults for " + sourcePlayer.getName().getString() + ":").withStyle(ChatFormatting.GOLD));
        for (String key : playerVaults.getAllKeys()) {
            Tag tag = playerVaults.get(key);
            if (tag == null) continue;
            if (tag.getType() == CompoundTag.TYPE) {
                // Green text
                sendSuccess(source, new TextComponent(key).withStyle(ChatFormatting.GREEN));
            }
        }
        return 1;
    }

}
