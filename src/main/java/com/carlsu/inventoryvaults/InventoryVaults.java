package com.carlsu.inventoryvaults;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.carlsu.inventoryvaults.compatibility.CosArmor;
import com.carlsu.inventoryvaults.handlers.CommandRegistrationHandler;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkConstants;

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
        markAsNotRequiredClientSide();
        if (FMLEnvironment.dist.isClient()) {
            LOGGER.info("Skipping LuckPerms init (not supported on the client!)");
            return;
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(commandRegistrationHandler);

        // CosArmor version = 1.18.2-v3x
        ModList.get().getModContainerById("cosmeticarmorreworked").ifPresentOrElse(modContainer -> {
            String cosVersion = modContainer.getModInfo().getVersion().toString();
            cosArmorMod = CosArmor.supportedVersions.contains(cosVersion);
        }, () -> {
            cosArmorMod = false;
        });
        

        LOGGER.info("InventoryVaults loaded -> version: " + MOD_VERSION);
        LOGGER.info("Valid CosArmor mod detected -> " + cosArmorMod);
    }

    // Copied method from luckperms
    private static void markAsNotRequiredClientSide() {
        try {
            // workaround as we don't compile against java 17
            ModLoadingContext.class.getDeclaredMethod("registerExtensionPoint", Class.class, Supplier.class)
                    .invoke(
                            ModLoadingContext.get(),
                            IExtensionPoint.DisplayTest.class,
                            (Supplier<?>) () -> new IExtensionPoint.DisplayTest(
                                    () -> NetworkConstants.IGNORESERVERONLY,
                                    (a, b) -> true
                            )
                    );
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
