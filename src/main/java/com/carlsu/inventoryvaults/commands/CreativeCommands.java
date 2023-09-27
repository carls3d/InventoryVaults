package com.carlsu.inventoryvaults.commands;

import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class CreativeCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> creativeCommands = Commands.literal("Creative")
            .requires(player -> player.hasPermission(2));

    
       
        creativeCommands.then(
            Commands.literal("inCreative").executes(context -> {
                Player player = context.getSource().getPlayerOrException();
                VaultCommands.sendSuccess(context.getSource(), "In creative dimension: " + player.getLevel().dimension().equals(ModDimension.CREATIVE_KEY) );
                return 1;
            }));
    }
    
}
