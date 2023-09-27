package com.carlsu.inventoryvaults.world.dimension;

import com.carlsu.inventoryvaults.InventoryVaults;

import net.minecraft.core.Registry;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ModDimension {
    public static final ResourceKey<Level> CREATIVE_KEY = ResourceKey.create(
        Registry.DIMENSION_REGISTRY, 
        new ResourceLocation(InventoryVaults.MODID, "creative")
        );
    
    public static final ResourceKey<DimensionType> CREATIVE_TYPE = ResourceKey.create(
        Registry.DIMENSION_TYPE_REGISTRY, 
        CREATIVE_KEY.getRegistryName()
        );

    public static final String CREATIVE_VAULT = "creative";
    public static final ListTag CREATIVE_SPAWN;
    static {
        CREATIVE_SPAWN = new ListTag();
        CREATIVE_SPAWN.add(DoubleTag.valueOf(0.0D));
        CREATIVE_SPAWN.add(DoubleTag.valueOf(64.0D));
        CREATIVE_SPAWN.add(DoubleTag.valueOf(0.0D));
    }


    public static void register() {
        System.out.println(
            "Registering " + CREATIVE_KEY.registry().toString() + ":" + CREATIVE_KEY.location().toString()
            );
    }
}   
