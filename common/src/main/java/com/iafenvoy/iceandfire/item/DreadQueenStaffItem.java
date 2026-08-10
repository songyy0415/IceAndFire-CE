package com.iafenvoy.iceandfire.item;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

// TODO :: Has no usage at the moment
public class DreadQueenStaffItem extends Item {
    public DreadQueenStaffItem(ResourceKey<Item> key) {
        super(new Properties().stacksTo(1).setId(key));
    }
}
