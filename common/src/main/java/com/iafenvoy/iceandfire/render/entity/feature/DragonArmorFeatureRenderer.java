package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.item.DragonArmorItem;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class DragonArmorFeatureRenderer<T extends DragonBaseEntity> extends RenderLayer<T, TabulaModel<T>> {
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    public DragonArmorFeatureRenderer(MobRenderer<T, TabulaModel<T>> renderIn) {
        super(renderIn);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int light, T dragon, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        EntityModel<T> model = this.getParentModel();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            Identifier texture = getArmorTexture(dragon.getItemBySlot(slot), slot);
            if (texture == null) continue;
            VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(texture));
            model.renderToBuffer(matrixStackIn, vertexConsumer, light, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    @Nullable
    public static Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        DragonArmorPart part = DragonArmorPart.fromSlot(slot);
        if (part != null && !stack.isEmpty() && stack.getItem() instanceof DragonArmorItem armorItem)
            return Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, String.format(Locale.ROOT, "textures/entity/dragon_armor/armor_%s_%s.png", part.getId(), armorItem.type.name()));
        else return null;
    }
}