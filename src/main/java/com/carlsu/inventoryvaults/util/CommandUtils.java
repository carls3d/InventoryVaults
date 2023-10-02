package com.carlsu.inventoryvaults.util;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CommandUtils {
    public static void sendSuccess(ServerPlayer player, Tag tag) {
        sendSuccess(getDefaultSource(), NbtUtils.toPrettyComponent(tag));
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
    public static void sendSuccessNBT(CommandSourceStack source, String str, Tag tag) {
        sendSuccess(source, new TranslatableComponent(str, NbtUtils.toPrettyComponent(tag)));
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
}
