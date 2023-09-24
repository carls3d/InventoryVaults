package com.carlsu.inventoryvaults.commands;

import com.carlsu.inventoryvaults.VaultHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ColorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.server.ServerLifecycleHooks;

public class VaultCommands {
    // /InvVault
    //     storage -> Returns current path
    //     storage <path> -> Sets current path    

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> vaultCommands = Commands.literal("Vaults")
            .requires(player -> player.hasPermission(2));
        
        // vaultCommands.then(
        // Commands.literal("reset").executes(context -> {
        //     ResourceLocation vault = new ResourceLocation("minecraft", "vault");
        //     CommandStorage storage = ServerLifecycleHooks.getCurrentServer().getCommandStorage();
        //     CompoundTag nbtVault = new CompoundTag();
        //     storage.set(vault, nbtVault);
        //     return 1;}));

        // vaultCommands.then(
        // Commands.literal("test1").executes(context -> {
        //     return 1;}));

        vaultCommands.then(
        Commands.literal("test2").executes(context -> {
            Player player = context.getSource().getPlayerOrException();
            CompoundTag ForgeData = player.getPersistentData();
            CompoundTag ForgeDataCopy = ForgeData.copy();
            CompoundTag playerVault = VaultHandler.serializeVault(player);

            // CompoundTag vaultData = new CompoundTag();
            // vaultData.put("test", playerVault);
            CompoundTag vaultData = (CompoundTag) new CompoundTag().put("test", playerVault);
            
            ForgeDataCopy.put("InventoryVaults", vaultData);
            
            sendSuccess(context.getSource(), "ForgeData:");
            sendSuccess(context.getSource(), ForgeData);
            sendSuccess(context.getSource(), "playerVault:");
            sendSuccess(context.getSource(), playerVault);
            sendSuccess(context.getSource(), "vaultData:");
            sendSuccess(context.getSource(), vaultData);
            sendSuccess(context.getSource(), "ForgeDataCopy:");
            sendSuccess(context.getSource(), ForgeDataCopy);
            return 1;}));
        
        vaultCommands.then(
        Commands.literal("dev")
        .then(
            Commands.literal("save").executes(context -> 
                    commandSaveVault(context.getSource())).then(
                Commands.argument("player", EntityArgument.player()).executes(context -> 
                        commandSaveVault(context.getSource(), 
                            EntityArgument.getPlayer(context, "player"))).then(
                    Commands.argument("Vault name", StringArgumentType.string()).executes(context -> 
                            commandSaveVault(context.getSource(), 
                                EntityArgument.getPlayer(context, "player"), 
                                StringArgumentType.getString(context, "Vault name"))))))
        .then(
            Commands.literal("load").executes(context -> 
                    commandLoadVault(context.getSource())).then(
                Commands.argument("player", EntityArgument.player()).executes(context -> 
                        commandLoadVault(context.getSource(),
                            EntityArgument.getPlayer(context, "player"))).then(
                    Commands.argument("Vault key", StringArgumentType.string()).executes(context -> 
                            commandLoadVault(context.getSource(), 
                                EntityArgument.getPlayer(context, "player"), 
                                StringArgumentType.getString(context, "Vault key"))))))
        .then(
            Commands.literal("colortest").then(
                Commands.argument("color", ColorArgument.color()).executes(context -> {
                    sendSuccess(context.getSource(), "Color: "+ ColorArgument.getColor(context, "color").name());
                    return 1;})))
        .then(
            Commands.literal("ForceRespawn").executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    ForgeEventFactory.firePlayerRespawnEvent(player, false);
                    sendSuccess(context.getSource(), "Force respawn fired");
                    return 1;})
            )
        );

        dispatcher.register(vaultCommands);
    }

    static int commandSaveVault(CommandSourceStack source) throws CommandSyntaxException{
        return commandSaveVault(source, source.getPlayerOrException());
    }
    static int commandSaveVault(CommandSourceStack source, Player player) {
        return commandSaveVault(source, player, "main");
    }
    static int commandSaveVault(CommandSourceStack source, Player player, String vaultKey) {
        VaultHandler.saveVault(player, vaultKey);
        sendSuccess(source, "Vault saved");
        return 1;
    }

    static int commandLoadVault(CommandSourceStack source) throws CommandSyntaxException{
        return commandLoadVault(source, source.getPlayerOrException());
    }
    static int commandLoadVault(CommandSourceStack source, Player player) {
        return commandLoadVault(source, player, "main");
    }
    static int commandLoadVault(CommandSourceStack source, Player player, String vaultKey) {
        VaultHandler.loadVault(player, vaultKey);
        sendSuccess(source, "Vault loaded");
        return 1;
    }
    
    



    public static void sendSuccess(Tag tag) {
        sendSuccess(getDefaultSource(), NbtUtils.toPrettyComponent(tag));
    }
    public static void sendSuccess(String string) {
        sendSuccess(getDefaultSource(), new TextComponent(string));
    }
    public static void sendSuccess(CommandSourceStack source, Tag tag) {
        sendSuccess(source, NbtUtils.toPrettyComponent(tag));
    }
    public static void sendSuccess(CommandSourceStack source, String string) {
        sendSuccess(source, new TextComponent(string));
    }
    public static void sendSuccess(CommandSourceStack source, Component component) {
        source.sendSuccess(component, false);
    }

    public static void sendFailure(String string) {
        sendFailure(getDefaultSource(), string);
    }
    public static void sendFailure(CommandSourceStack source, String string) {
        source.sendFailure(new TextComponent(string));
    }

    private static CommandSourceStack getDefaultSource() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return server.createCommandSourceStack();
    }
    // InventoryVaults.CREATIVE_SPAWN = new Vec3(0, 64, 0);
}