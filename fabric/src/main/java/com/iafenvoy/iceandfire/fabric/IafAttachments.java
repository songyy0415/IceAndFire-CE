package com.iafenvoy.iceandfire.fabric;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.component.ChainData;
import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.data.component.PortalData;
import com.iafenvoy.iceandfire.event.CommonEvents;
import com.iafenvoy.iceandfire.util.attachment.IafEntityAttachment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("UnstableApiUsage")
public final class IafAttachments {
    public static final AttachmentType<ChainData> CHAIN_DATA = AttachmentRegistry.create(ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "chain_data"), builder -> builder.initializer(ChainData::new).persistent(ChainData.CODEC).syncWith(ChainData.PACKET_CODEC, AttachmentSyncPredicate.all()).copyOnDeath());
    public static final AttachmentType<MiscData> MISC_DATA = AttachmentRegistry.create(ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "misc_data"), builder -> builder.initializer(MiscData::new).persistent(MiscData.CODEC).syncWith(MiscData.PACKET_CODEC, AttachmentSyncPredicate.all()).copyOnDeath());
    public static final AttachmentType<PortalData> PORTAL_DATA = AttachmentRegistry.create(ResourceLocation.fromNamespaceAndPath(IceAndFire.MOD_ID, "portal_data"), builder -> builder.initializer(PortalData::new).persistent(PortalData.CODEC).syncWith(PortalData.PACKET_CODEC, AttachmentSyncPredicate.all()).copyOnDeath());

    public static void init() {
        CommonEvents.LIVING_TICK.register(living -> {
            tickAndSync(CHAIN_DATA, living);
            tickAndSync(MISC_DATA, living);
            tickAndSync(PORTAL_DATA, living);
        });
    }

    private static <T extends Entity, A extends IafEntityAttachment<T>> void tickAndSync(AttachmentType<A> type, T entity) {
        A attachment = entity.getAttachedOrCreate(type);
        attachment.tick(entity);
        if (attachment.isDirty()) entity.setAttached(type, attachment);
    }
}
