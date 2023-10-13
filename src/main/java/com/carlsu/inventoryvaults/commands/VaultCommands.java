package com.carlsu.inventoryvaults.commands;

import javax.annotation.Nullable;

import com.carlsu.inventoryvaults.events.UpdateVaultEvent;
import com.carlsu.inventoryvaults.types.PlayerData;
import com.carlsu.inventoryvaults.types.VaultType;
import com.carlsu.inventoryvaults.util.CommandUtils;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultContainer;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
        LiteralArgumentBuilder<CommandSourceStack> vaultCommands = Commands.literal("vaults")
            .requires(player -> player.hasPermission(2));

        
        vaultCommands
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
                    Commands.argument("VaultKey", StringArgumentType.string()).executes(vaultSee()).then(
                        Commands.argument("Container", NbtPathArgument.nbtPath()).executes(vaultSee())))))
        .then(
            Commands.literal("vaultseeInv").executes(vaultSee("Inventory", null)).then(
                Commands.argument("Player", EntityArgument.player()).executes(vaultSee("Inventory", null)).then(
                     Commands.argument("VaultKey", StringArgumentType.string()).executes(vaultSee("Inventory", null)))))
        .then(
            Commands.literal("vaultseeEnder").executes(vaultSee("EnderItems", null)).then(
                Commands.argument("Player", EntityArgument.player()).executes(vaultSee("EnderItems", null)).then(
                     Commands.argument("VaultKey", StringArgumentType.string()).executes(vaultSee("EnderItems", null)))))

        .then(
            Commands.literal("list").then(
                Commands.argument("Player", EntityArgument.player()).executes(context -> 
                        listVaults(context.getSource(), 
                            EntityArgument.getPlayer(context, "Player")))));
        
        dispatcher.register(vaultCommands);
    }

    public static boolean hasArg(CommandContext<CommandSourceStack> context, String arg) {
        return context.getNodes().stream().anyMatch(node -> node.getNode().getName().equals(arg));
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
        return 1;
    }


    public static Command<CommandSourceStack> vaultSee() {
        return vaultSee(null, null);
    }
    public static Command<CommandSourceStack> vaultSee(@Nullable String container, @Nullable String vault) {
        return context -> {
            CommandSourceStack source = context.getSource();
            String defaultVault = vault == null ? "main" : vault;
            String defaultContainer = container == null ? "Inventory" : container;
            
            ServerPlayer sourcePlayerArg = (hasArg(context, "Player")) ? EntityArgument.getPlayer(context, "Player") : source.getPlayerOrException();
            String vaultKeyArg = (hasArg(context, "VaultKey")) ? StringArgumentType.getString(context, "VaultKey") : defaultVault;
            String containerArg = (hasArg(context, "Container")) ? NbtPathArgument.getPath(context, "Container").toString() : defaultContainer;

            return vaultSeeExec(source, sourcePlayerArg, vaultKeyArg, containerArg);
        };
    }
    public static int vaultSeeExec(CommandSourceStack source, ServerPlayer targetPlayer,String vaultKey,  String path) throws CommandSyntaxException {
        ServerPlayer sourcePlayer = source.getPlayerOrException();
        targetPlayer = targetPlayer == null ? sourcePlayer : targetPlayer;

        CompoundTag vaultData = VaultUtils.Vault.get(targetPlayer, vaultKey);
        if (vaultData.isEmpty()) {
            sendFailure(source, new TextComponent("Vault not found: " + vaultKey).withStyle(ChatFormatting.RED));
            return 0;
        }

        // Default to inventory
        String pathStr = path.isEmpty() ? "Inventory" : path;

        Tag inventoryPath = VaultUtils.parsePath(vaultData, pathStr);
        if (inventoryPath.getId() != 9) {
            sendFailure(source, new TextComponent("'"+pathStr + "'' is not a list").withStyle(ChatFormatting.RED));
            return 0;
        }
        ListTag inventoryListTag = (ListTag) inventoryPath;
        MenuType<ChestMenu> menuType = MenuType.GENERIC_9x5;
        VaultContainer vaultContainer = new VaultContainer(inventoryListTag, 45);
        int chestContainerRows = vaultContainer.getContainerSize() / 9;
        String menuName = "Vault: "+targetPlayer.getName().getString()+ " -> " + vaultKey+"."+pathStr;
        
        sourcePlayer.openMenu(new MenuProvider() {
            @Override
			public Component getDisplayName() {
                return new TextComponent(menuName);
			}
			@Override
			public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player p) {
				return new ChestMenu(menuType, id, playerInventory, vaultContainer, chestContainerRows);
			}
        });
        return 1;
    }


    public static int listVaults(CommandSourceStack source, ServerPlayer sourcePlayer) {
        CompoundTag playerVaults = VaultUtils.PlayerVaultData.get(sourcePlayer);
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
