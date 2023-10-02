package com.carlsu.inventoryvaults.util;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;

public class Vault {
    UUID ownerUUID;
    String vaultKey;
    CompoundTag data;
    // ListTag inventory;
    // ListTag enderChest;

    public Vault(Player player, String vaultKey, CompoundTag playerVault) {
        this.ownerUUID = player.getUUID();
        this.vaultKey = vaultKey;
    }


    public static Vault getVault(Player player, String vaultKey) {
        CompoundTag playerVault = VaultUtils.getVault(player, vaultKey);
        return new Vault(player, vaultKey, playerVault);
    }

    // Set vault data
    public void setData(CompoundTag data) {
        this.data = data;
    }

    public ListTag getInventory() {
        return data.getList("Inventory", 10);
    }
    public void setInventory(ListTag inventory) {
        data.put("Inventory", inventory);
    }

    public ListTag getContainer(String containerName) {
        return data.getList(containerName, 10);
    }

}
