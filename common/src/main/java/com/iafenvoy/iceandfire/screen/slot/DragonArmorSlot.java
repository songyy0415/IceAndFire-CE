package com.iafenvoy.iceandfire.screen.slot;

import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.DragonArmorItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DragonArmorSlot extends Slot {
    private final DragonArmorPart expectedArmor;

    public DragonArmorSlot(Container inventory, int index, int x, int y, DragonArmorPart expectedArmor) {
        super(inventory, index, x, y);
        this.expectedArmor = expectedArmor;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return super.mayPlace(stack) && !stack.isEmpty() && stack.getItem() instanceof DragonArmorItem armor && armor.dragonSlot == this.expectedArmor;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
