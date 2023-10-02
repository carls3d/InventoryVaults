package com.carlsu.inventoryvaults.util;


import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class VaultContainer implements Container{
    private final int size;
    private final NonNullList<ItemStack> items;
    // private final CompoundTag inventory;
    @Nullable private List<ContainerListener> listeners;
    
    public VaultContainer(ListTag inventory, int size) {
        // this.inventory = inventory;

        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < inventory.size(); i++) {
            CompoundTag item = inventory.getCompound(i);
            this.items.set(i, ItemStack.of(item));
        } 
        this.size = items.size();
    }
    // public VaultContainer(ItemStack... itemsStacks) {
    //     // this.inventory = new ListTag();
    //     this.items = NonNullList.withSize(itemsStacks.length, ItemStack.EMPTY);
    //     for (int i = 0; i < itemsStacks.length; i++) {
    //         this.items.set(i, itemsStacks[i]);
    //     } 
    //     this.size = itemsStacks.length;
    // }


    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < this.size ? this.items.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int size, int index) {
        return ContainerHelper.removeItem(this.items, size, index);
        // List<ItemStack> items = this.inventory.stream().map(item -> ItemStack.of((CompoundTag) item)).toList();
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        return ContainerHelper.takeItem(this.items, p_18951_);
    }

    @Override
    public void setItem(int p_18944_, ItemStack p_18945_) {
        this.items.set(p_18944_, p_18945_);
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for(ContainerListener containerlistener : this.listeners) {
               containerlistener.containerChanged(this);
            }
         }
    }

    @Override
    public boolean stillValid(Player p_18946_) {
        return true;
    }
}
