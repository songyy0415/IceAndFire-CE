package com.iafenvoy.iceandfire.render.entity.feature;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonArmorPart;
import com.iafenvoy.iceandfire.item.DragonArmorItem;
import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class DragonArmorFeatureRenderer extends RenderLayer<DragonRenderState, TabulaModel<DragonRenderState>> {
    public DragonArmorFeatureRenderer(RenderLayerParent<DragonRenderState, TabulaModel<DragonRenderState>> renderIn) {
        super(renderIn);
    }

    @Override
    public void submit(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int light, DragonRenderState state, float yRot, float xRot) {
        this.renderArmor(matrixStackIn, submitNodeCollector, light, state, state.armorHead);
        this.renderArmor(matrixStackIn, submitNodeCollector, light, state, state.armorChest);
        this.renderArmor(matrixStackIn, submitNodeCollector, light, state, state.armorLegs);
        this.renderArmor(matrixStackIn, submitNodeCollector, light, state, state.armorFeet);
    }

    private void renderArmor(PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, int light, DragonRenderState state, @Nullable Identifier texture) {
        if (texture == null) return;
        submitNodeCollector.order(1)
            .submitModel(this.getParentModel(), state, matrixStackIn, RenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
    }

    @Nullable
    public static Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        DragonArmorPart part = DragonArmorPart.fromSlot(slot);
        if (part != null && !stack.isEmpty() && stack.getItem() instanceof DragonArmorItem armorItem)
            return Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, String.format(Locale.ROOT, "textures/entity/dragon_armor/armor_%s_%s.png", part.getId(), armorItem.type.name()));
        else return null;
    }
}
