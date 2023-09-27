package com.carlsu.inventoryvaults;

import com.carlsu.inventoryvaults.events.CommandRegistrationHandler;
import com.carlsu.inventoryvaults.events.PlayerEventHandler;
import com.carlsu.inventoryvaults.util.PlayerData;
import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(InventoryVaults.MODID)
public class InventoryVaults {
    public static final String MODID = "inventoryvaults";
    private static final Logger LOGGER = LogUtils.getLogger();
    

    private CommandRegistrationHandler commandRegistrationHandler = new CommandRegistrationHandler();
    private PlayerEventHandler playerEventHandler = new PlayerEventHandler();
    private PlayerData playerData = new PlayerData();

    public InventoryVaults() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(commandRegistrationHandler);
        MinecraftForge.EVENT_BUS.register(playerEventHandler);
        MinecraftForge.EVENT_BUS.register(playerData);
        ModDimension.register();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    private void loadComplete(final FMLLoadCompleteEvent e) {
    	LOGGER.info("InventoryVaults loaded");
    }
}
