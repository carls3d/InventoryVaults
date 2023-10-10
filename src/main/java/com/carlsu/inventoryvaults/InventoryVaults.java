package com.carlsu.inventoryvaults;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.compatibility.CosArmor;
import com.carlsu.inventoryvaults.handlers.CommandRegistrationHandler;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

// TODO
// -

@Mod(InventoryVaults.MODID)
public class InventoryVaults {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "inventoryvaults";
    public static final String MOD_VERSION = ModList.get().getModContainerById("inventoryvaults").get().getModInfo().getVersion().toString();

    public static boolean cosArmorMod;

    CommandRegistrationHandler commandRegistrationHandler = new CommandRegistrationHandler();

    public InventoryVaults() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(commandRegistrationHandler);



        // CosArmor version = 1.18.2-v3x
        ModList.get().getModContainerById("cosmeticarmorreworked").ifPresentOrElse(modContainer -> {
            String cosVersion = modContainer.getModInfo().getVersion().toString();
            cosArmorMod = CosArmor.supportedVersions.contains(cosVersion);
            // cosArmorMod = cosVersion.equals("1.18.2-v3x");
        }, () -> {
            cosArmorMod = false;
        });

        LOGGER.info("InventoryVaults loaded -> version: " + MOD_VERSION);
        LOGGER.info("Valid CosArmor mod detected -> version: " + cosArmorMod);
    }
}
