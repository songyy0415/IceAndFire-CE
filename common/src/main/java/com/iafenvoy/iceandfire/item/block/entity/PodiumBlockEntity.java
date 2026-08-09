package com.iafenvoy.iceandfire.item.block.entity;

import com.iafenvoy.iceandfire.item.DragonEggItem;
import com.iafenvoy.iceandfire.network.payload.UpdatePodiumS2CPayload;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.screen.handler.PodiumScreenHandler;
import com.iafenvoy.uranus.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PodiumBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    private static final int[] slotsTop = new int[]{0};
    public int ticksExisted;
    public int prevTicksExisted;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

    public PodiumBlockEntity(BlockPos pos, BlockState state) {
        super(IafBlockEntities.PODIUM.get(), pos, state);
    }

    //TODO: This must be easier to do
    public static void tick(Level level, BlockPos pos, BlockState state, PodiumBlockEntity entityPodium) {
        entityPodium.prevTicksExisted = entityPodium.ticksExisted;
        entityPodium.ticksExisted++;
    }

    @Override
    public int getContainerSize() {
        return this.stacks.size();
    }

    @Override
    public ItemStack getItem(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (!this.stacks.get(index).isEmpty()) {
            ItemStack itemstack;
            if (this.stacks.get(index).getCount() <= count) {
                itemstack = this.stacks.get(index);
                this.stacks.set(index, ItemStack.EMPTY);
            } else {
                itemstack = this.stacks.get(index).split(count);
                if (this.stacks.get(index).isEmpty())
                    this.stacks.set(index, ItemStack.EMPTY);
            }
            return itemstack;
        } else return ItemStack.EMPTY;
    }

    public ItemStack getStackInSlotOnClosing(int index) {
        if (!this.stacks.get(index).isEmpty()) {
            ItemStack itemstack = this.stacks.get(index);
            this.stacks.set(index, itemstack);
            return itemstack;
        } else return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.stacks.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize())
            stack.setCount(this.getMaxStackSize());
        assert this.level != null;
        if (!this.level.isClientSide())
            ServerHelper.sendToAll(new UpdatePodiumS2CPayload(this.getBlockPos(), this.stacks.getFirst()));
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.stacks, registryLookup);
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        ContainerHelper.saveAllItems(nbt, this.stacks, registryLookup);
    }

    @Override
    public void startOpen(Player player) {
    }

    @Override
    public void stopOpen(Player player) {
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
        return index != 0 || (stack.getItem() instanceof DragonEggItem);
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stacks.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return slotsTop;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return false;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return this.saveWithFullMetadata(registryLookup);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public Component getDisplayName() {
        return this.getDefaultName();
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.iceandfire.podium");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.stacks = inventory;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); i++)
            if (!this.getItem(i).isEmpty())
                return false;
        return true;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new PodiumScreenHandler(id, this, playerInventory);
    }
}
