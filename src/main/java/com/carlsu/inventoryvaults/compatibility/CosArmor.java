package com.carlsu.inventoryvaults.compatibility;

import java.util.Set;

import com.carlsu.inventoryvaults.InventoryVaults;
import com.carlsu.inventoryvaults.util.IVaultData;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CosArmor implements IVaultData {
    public static final String cosKey = "CosArmor";
    public static final Set<String> supportedVersions = Set.of(
        "1.18.2-v3x"
        );
    
    public static void cosLoad(ServerPlayer player, CompoundTag playerVault) {
        if (!InventoryVaults.cosArmorMod) return;
        CompoundTag vaultCosArmor = playerVault.getCompound(cosKey);
        if (vaultCosArmor.isEmpty()) {
            commandClear(player);
            return;
        }
        setNBT(player, vaultCosArmor);
        commandLoadFromNBT(player);
    }
   
    
    public static CompoundTag injectCosArmor(ServerPlayer player, CompoundTag filteredData) {
        if (!InventoryVaults.cosArmorMod) return filteredData;
        CompoundTag forgeData = player.getPersistentData();
        if (forgeData.contains(cosKey)) {
            filteredData.put(cosKey, forgeData.getCompound(cosKey));
        } else {
            filteredData.put(cosKey, new CompoundTag());
        }
        return filteredData;
    }

    public static void commandUpdate(ServerPlayer player) {
        performCommand(player, "updatecosarmor");
    }
    public static void commandLoadFromNBT(ServerPlayer player) {
        performCommand(player, "loadnbtcosarmor");
    }
    public static void commandClear(ServerPlayer player) {
        performCommand(player, "clearcosarmor");
    }

    public static void performCommand(ServerPlayer player, String command) {
        if (!InventoryVaults.cosArmorMod) return;
        CommandSourceStack source = player.createCommandSourceStack().withPermission(2);
        ServerLifecycleHooks.getCurrentServer().getCommands().performCommand(source, command);
    }
 

    public static void setNBT(ServerPlayer player, CompoundTag nbt) {
        player.getPersistentData().put(cosKey, nbt);
    }
    public static CompoundTag getNBT(ServerPlayer player) {
        commandUpdate(player);
        return player.getPersistentData().getCompound(cosKey);
    }
}
