package com.carlsu.inventoryvaults.util;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public interface IVaultData {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = InventoryVaults.MODID;

    public static final String VAULT_NAME = "InventoryVaults";
    public static final String ACTIVE_VAULT = "ActiveVaultKey";
    public static final String PREVIOUS_VAULT = "PreviousVaultKey";
    public static final String DEFAULT_VAULT = "main";
    public static final String VERSION = "1.0";
   

    public static final Set<String> VAULT_FILTER = ImmutableSet.of (
            "Health",
            "foodLevel",
            "foodSaturationLevel",
            "foodExhaustionLevel",
            "SelectedItemSlot",
            "XpP",
            "XpLevel",
            "playerGameType",
            "ActiveEffects",
            "Inventory",
            "EnderItems",
            "ForgeCaps",
            // "ForgeData."+VAULT_NAME+"."+ACTIVE_VAULT,
            "Attributes",
            "Dimension",
            "Pos",
            "Rotation"
            // "Motion"
    );


    public static CompoundTag parseArgument(String pathArgument) {
        //  "ForgeData."+VAULT_NAME+"."+DEFAULT_VAULT -> "{ForgeData:{InventoryVaults:{main}}}
        if (pathArgument.contains(".")) {
            String[] strList = pathArgument.split("\\.");
            ArrayUtils.reverse(strList);

            CompoundTag compoundTag = new CompoundTag();
            for (String path : strList) {
                CompoundTag newCT = new CompoundTag();
                CompoundTag tempCT = compoundTag.copy();
                newCT.put(path, tempCT);
                compoundTag = newCT;
            }
            return compoundTag;
        }
        else return new CompoundTag();
    }
//
//    public Tag getNBTData(String command, CompoundTag root) {
//        if (command.contains(".")) {
//            // Handle "ForgeCaps.curios:inventory" style command
//            String[] parts = command.split("\\.");
//            CompoundTag temp = root;
//            for (String part : parts) {
//                temp = temp.getCompound(part);
//            }
//            return temp;
//        } else if (command.contains("[") && command.contains("]")) {
//            // Handle "Pos[0]" style command
//            String listName = command.substring(0, command.indexOf('['));
//            int index = Integer.parseInt(command.substring(command.indexOf('[') + 1, command.indexOf(']')));
//            ListTag listNBT = root.getList(listName, 10);  // 10 is the NBT type for CompoundNBT
//            return listNBT.get(index);
//        } else {
//            // Handle other cases
//            return null;
//        }
//    }



}
