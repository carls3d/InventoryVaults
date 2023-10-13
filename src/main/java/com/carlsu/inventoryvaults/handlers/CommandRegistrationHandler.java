package com.carlsu.inventoryvaults.handlers;

import com.carlsu.inventoryvaults.commands.CreativeCommands;
import com.carlsu.inventoryvaults.commands.VaultCommands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@EventBusSubscriber
public class CommandRegistrationHandler {
    @SubscribeEvent
	public void registerCommands(RegisterCommandsEvent e) {
        VaultCommands.register(e.getDispatcher());
        CreativeCommands.register(e.getDispatcher());
    }
}
