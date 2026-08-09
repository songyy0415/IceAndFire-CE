package com.iafenvoy.iceandfire.item.block.entity;

import com.iafenvoy.iceandfire.entity.PixieEntity;
import com.iafenvoy.iceandfire.network.payload.UpdatePixieHouseS2CPayload;
import com.iafenvoy.iceandfire.network.payload.UpdatePixieJarS2CPayload;
import com.iafenvoy.iceandfire.registry.IafBlockEntities;
import com.iafenvoy.iceandfire.registry.IafEntities;
import com.iafenvoy.iceandfire.registry.IafParticles;
import com.iafenvoy.iceandfire.registry.IafSounds;
import com.iafenvoy.uranus.ServerHelper;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class JarBlockEntity extends BlockEntity {
    private static final float PARTICLE_WIDTH = 0.3F;
    private static final float PARTICLE_HEIGHT = 0.6F;
    private final Random rand;
    public boolean hasPixie;
    public boolean prevHasProduced;
    public boolean hasProduced;
    public boolean tamedPixie;
    public UUID pixieOwnerUUID;
    public int pixieType;
    public int ticksExisted;
    public NonNullList<ItemStack> pixieItems = NonNullList.withSize(1, ItemStack.EMPTY);
    public float rotationYaw;
    public float prevRotationYaw;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(IafBlockEntities.PIXIE_JAR.get(), pos, state);
        this.rand = new Random();
        this.hasPixie = true;
    }

    public JarBlockEntity(BlockPos pos, BlockState state, boolean empty) {
        super(IafBlockEntities.PIXIE_JAR.get(), pos, state);
        this.rand = new Random();
        this.hasPixie = !empty;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, JarBlockEntity entityJar) {
        entityJar.ticksExisted++;
        if (level.isClientSide() && entityJar.hasPixie) {
            level.addParticle(IafParticles.PIXIE_DUST.get(),
                    pos.getX() + 0.5F + (double) (entityJar.rand.nextFloat() * PARTICLE_WIDTH * 2F) - PARTICLE_WIDTH,
                    pos.getY() + (double) (entityJar.rand.nextFloat() * PARTICLE_HEIGHT),
                    pos.getZ() + 0.5F + (double) (entityJar.rand.nextFloat() * PARTICLE_WIDTH * 2F) - PARTICLE_WIDTH, PixieEntity.PARTICLE_RGB[entityJar.pixieType][0], PixieEntity.PARTICLE_RGB[entityJar.pixieType][1], PixieEntity.PARTICLE_RGB[entityJar.pixieType][2]);
        }
        if (entityJar.ticksExisted % 24000 == 0 && !entityJar.hasProduced && entityJar.hasPixie) {
            entityJar.hasProduced = true;
            if (!level.isClientSide())
                ServerHelper.sendToAll(new UpdatePixieJarS2CPayload(pos, true));
        }
        if (entityJar.hasPixie && entityJar.hasProduced != entityJar.prevHasProduced && entityJar.ticksExisted > 5) {
            if (!level.isClientSide())
                ServerHelper.sendToAll(new UpdatePixieJarS2CPayload(pos, entityJar.hasProduced));
            else
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5, IafSounds.PIXIE_HURT.get(), SoundSource.BLOCKS, 1, 1, false);
        }
        entityJar.prevRotationYaw = entityJar.rotationYaw;
        if (entityJar.rand.nextInt(30) == 0)
            entityJar.rotationYaw = (entityJar.rand.nextFloat() * 360F) - 180F;
        if (entityJar.hasPixie && entityJar.ticksExisted % 40 == 0 && entityJar.rand.nextInt(6) == 0 && level.isClientSide())
            level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5, IafSounds.PIXIE_IDLE.get(), SoundSource.BLOCKS, 1, 1, false);
        entityJar.prevHasProduced = entityJar.hasProduced;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        nbt.putBoolean("HasPixie", this.hasPixie);
        nbt.putInt("PixieType", this.pixieType);
        nbt.putBoolean("HasProduced", this.hasProduced);
        nbt.putBoolean("TamedPixie", this.tamedPixie);
        if (this.pixieOwnerUUID != null)
            nbt.putIntArray("PixieOwnerUUID", UUIDUtil.uuidToIntArray(this.pixieOwnerUUID));
        nbt.putInt("TicksExisted", this.ticksExisted);
        ContainerHelper.saveAllItems(nbt, this.pixieItems, registryLookup);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.hasPixie = nbt.getBooleanOr("HasPixie", false);
        this.pixieType = nbt.getInt("PixieType").orElse(0);
        this.hasProduced = nbt.getBooleanOr("HasProduced", false);
        this.ticksExisted = nbt.getInt("TicksExisted").orElse(0);
        this.tamedPixie = nbt.getBooleanOr("TamedPixie", false);
        if (nbt.read("PixieOwnerUUID", UUIDUtil.CODEC).isPresent())
            this.pixieOwnerUUID = nbt.read("PixieOwnerUUID", UUIDUtil.CODEC).orElse(null);
        this.pixieItems = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.pixieItems, registryLookup);
    }

    public void releasePixie() {
        PixieEntity pixie = new PixieEntity(IafEntities.PIXIE.get(), this.level);
        pixie.absMoveTo(this.worldPosition.getX() + 0.5F, this.worldPosition.getY() + 1F, this.worldPosition.getZ() + 0.5F, new Random().nextInt(360), 0);
        pixie.setItemInHand(InteractionHand.MAIN_HAND, this.pixieItems.getFirst());
        pixie.setColor(this.pixieType);
        pixie.ticksUntilHouseAI = 500;
        pixie.setTame(this.tamedPixie, false);
        pixie.setOwnerUUID(this.pixieOwnerUUID);
        assert this.level != null;
        this.level.addFreshEntity(pixie);
        this.hasPixie = false;
        this.pixieType = 0;
        if (!this.level.isClientSide())
            ServerHelper.sendToAll(new UpdatePixieHouseS2CPayload(this.worldPosition, false, 0));
    }
}
