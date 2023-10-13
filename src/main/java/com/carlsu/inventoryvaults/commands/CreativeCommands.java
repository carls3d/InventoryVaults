package com.carlsu.inventoryvaults.commands;

import java.util.function.Predicate;

import com.carlsu.inventoryvaults.util.CommandUtils;
import com.carlsu.inventoryvaults.util.IVaultData;
import com.carlsu.inventoryvaults.util.VaultUtils;
import com.carlsu.inventoryvaults.world.dimension.CreativeDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CreativeCommands extends CommandUtils implements CreativeDimension, IVaultData{

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        LiteralArgumentBuilder<CommandSourceStack> creativeCommands = Commands.literal("creative")
            .requires(player -> player.hasPermission(0));
        
        ArgumentBuilder<CommandSourceStack, ?> teleport_creative = 
                Commands.argument("coordinates", BlockPosArgument.blockPos()).executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ResourceKey<Level> dimensionKey = player.level.dimension();
                    if (dimensionKey != CreativeDimension.CREATIVE_KEY) {
                        sendSuccess(context.getSource(), 
                            new TextComponent("Not in creative dimension").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "coordinates");
                    player.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                    return 1;
                }
            );

        Predicate<CommandSourceStack> inCreativeDimension = player -> {
            return player.getLevel().dimension().equals(CREATIVE_KEY);
        };

        creativeCommands.then(Commands.literal("tp").requires(inCreativeDimension).then(teleport_creative));
        // creativeCommands.then(Commands.literal("teleport").requires(inCreativeDimension).then(teleport_creative));



        creativeCommands.then(
            Commands.literal("enter").executes(context -> {
                CommandSourceStack source = context.getSource();
                ServerPlayer player = context.getSource().getPlayerOrException();
                ResourceKey<Level> dimensionKey = player.level.dimension();
                if (dimensionKey == CreativeDimension.CREATIVE_KEY) {
                    sendFailure(context.getSource(), 
                        new TextComponent("You are already in creative silly~").withStyle(ChatFormatting.RED));
                    return 0;
                } else {
                    // Check if player has a vault in creative dimension
                    // CompoundTag creativeVault = VaultUtils.PlayerVaultData.get(player).getCompound(CREATIVE_VAULT_KEY);
                    CompoundTag creativeVault;
                    creativeVault = VaultUtils.Vault.get(player, CREATIVE_VAULT_KEY);
                    
                    String creativeDimension = creativeVault.getString("Dimension");
                    ResourceKey<Level> creativeDimensionKey = VaultUtils.getResourceKey(creativeDimension);
                    
                    // MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                   

                    if (creativeDimensionKey.equals(CREATIVE_KEY)) {
                        VaultCommands.commandLoadVault(source, player, CREATIVE_VAULT_KEY);
                    } else {
                        CompoundTag inventoryVaults = VaultUtils.PlayerVaultData.get(player);
                        inventoryVaults.remove(CREATIVE_VAULT_KEY);
                        player.teleportTo(
                            ServerLifecycleHooks.getCurrentServer().getLevel(CreativeDimension.CREATIVE_KEY),
                            CreativeDimension.CREATIVE_SPAWN.getDouble(0), 
                            CreativeDimension.CREATIVE_SPAWN.getDouble(1), 
                            CreativeDimension.CREATIVE_SPAWN.getDouble(2), 
                            0.0F, 
                            0.0F
                            );
                    } 
                    sendSuccess(
                        context.getSource(), 
                        new TextComponent("Entering to creative dimension...").withStyle(ChatFormatting.GREEN));
                    return 1;}}))
        
        .then(
            Commands.literal("leave").executes(context -> {
                CommandSourceStack source = context.getSource();
                ServerPlayer player = source.getPlayerOrException();
                ResourceKey<Level> dimensionKey = player.level.dimension();
                if (dimensionKey != CreativeDimension.CREATIVE_KEY) {
                    sendFailure(source, 
                        new TextComponent("Not in creative dimension").withStyle(ChatFormatting.RED));
                    return 0;
                } else {
                    CompoundTag inventoryVaults = VaultUtils.PlayerVaultData.get(player);
                    String previousVault = VaultUtils.PlayerVaultData.getStringOrDefault(player, PREVIOUS_VAULT, DEFAULT_VAULT);
                    
                    if (inventoryVaults.contains(previousVault)) {
                        // CompoundTag vault = inventoryVaults.get(previousVault);
                        if (inventoryVaults.getCompound(previousVault).contains("Dimension")) {
                            sendSuccess(source, 
                                new TextComponent("Leaving creative dimension...").withStyle(ChatFormatting.GREEN));
                            VaultCommands.commandLoadVault(source, player, previousVault);
                            return 1;
                        }
                    }
                    return 1;}}))
        
        .then(
                Commands.literal("rotation").executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerPlayer player = source.getPlayerOrException();
                    CompoundTag invVaults = VaultUtils.PlayerVaultData.get(player);
                    
                    if (!invVaults.contains(LOAD_ROTATION)) {
                        invVaults.putBoolean(LOAD_ROTATION, true);
                    }
                    boolean hasLoadRot = invVaults.contains(LOAD_ROTATION);
                    boolean loadRot = hasLoadRot ? invVaults.getBoolean(LOAD_ROTATION) : false;
                    
                    invVaults.putBoolean(LOAD_ROTATION, !loadRot);
                    sendSuccess(source, new TextComponent("Load rotation: " + loadRot + " -> " + !loadRot).withStyle(ChatFormatting.GREEN));
                    return 1;
                }));
        
        dispatcher.register(creativeCommands);

    }
   
}
