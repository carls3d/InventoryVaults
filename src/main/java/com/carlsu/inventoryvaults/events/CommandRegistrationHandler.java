package com.carlsu.inventoryvaults.events;

import com.carlsu.inventoryvaults.commands.VaultCommands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CommandRegistrationHandler {
    @SubscribeEvent
	public void registerCommands(RegisterCommandsEvent e) {
        VaultCommands.register(e.getDispatcher());
    }
}