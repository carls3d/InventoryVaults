package com.carlsu.inventoryvaults;

import com.carlsu.inventoryvaults.handlers.CommandRegistrationHandler;
// import com.carlsu.inventoryvaults.world.dimension.ModDimension;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// TODO
// - On enter creative dimension
//~~      - Set gamemode to creative
//      - Set flight to true?
//~~      - When no creative vault exists, clear inventory after successful save
//~~ - On sucessful save -> loading new/empty vault -> clear inventory

@Mod(InventoryVaults.MODID)
public class InventoryVaults {
    public static final String MODID = "inventoryvaults";
    public static final String VERSION = "1.0";
    private static final Logger LOGGER = LogUtils.getLogger();


    CommandRegistrationHandler commandRegistrationHandler = new CommandRegistrationHandler();

    public InventoryVaults() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(commandRegistrationHandler);
        // ModDimension.register();
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    private void loadComplete(final FMLLoadCompleteEvent e) {
    	LOGGER.info("InventoryVaults loaded");
    }
}
