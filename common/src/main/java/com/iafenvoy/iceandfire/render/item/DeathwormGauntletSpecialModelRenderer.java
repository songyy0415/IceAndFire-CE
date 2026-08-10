package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.registry.IafDataComponents;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.render.entity.DeathWormEntityRenderer;
import com.iafenvoy.iceandfire.render.model.DeathWormGauntletModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class DeathwormGauntletSpecialModelRenderer extends AdvancedSpecialModelRenderer<DeathwormGauntletArgument> {
    private static final DeathWormGauntletModel MODEL = new DeathWormGauntletModel();

    public DeathwormGauntletSpecialModelRenderer() {
        super(MODEL);
    }

    @Override
    public DeathwormGauntletArgument extractArgument(ItemStack stack) {
        int variant;
        if (stack.is(IafItems.DEATHWORM_GAUNTLET_RED.get()))
            variant = 0;
        else if (stack.is(IafItems.DEATHWORM_GAUNTLET_WHITE.get()))
            variant = 1;
        else
            variant = 2;
        float lungeTicks = 0.0F;
        if (Minecraft.getInstance().level != null) {
            Entity holder = Minecraft.getInstance().level.getEntity(stack.getOrDefault(IafDataComponents.USER_ID.get(), -1));
            if (holder instanceof LivingEntity livingEntity) {
                MiscData miscData = MiscData.get(livingEntity);
                float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                lungeTicks = miscData.lungeTicks == 0 ? 0 : miscData.lungeTicks + partialTick;
            }
        }
        return new DeathwormGauntletArgument(variant, lungeTicks);
    }

    @Override
    public void submit(DeathwormGauntletArgument argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color) {
        RenderType texture = switch (argument.variant()) {
            case 0 -> RenderTypes.entityCutout(DeathWormEntityRenderer.TEXTURE_RED);
            case 1 -> RenderTypes.entityCutout(DeathWormEntityRenderer.TEXTURE_WHITE);
            default -> RenderTypes.entityCutout(DeathWormEntityRenderer.TEXTURE_YELLOW);
        };
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        MODEL.animate(argument.lungeTicks());
        this.renderModel(poseStack, submitNodeCollector, texture, light, overlay, color);
        poseStack.popPose();
    }

    public static final class Unbaked implements SpecialModelRenderer.Unbaked<DeathwormGauntletArgument> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<DeathwormGauntletArgument> bake(SpecialModelRenderer.BakingContext context) {
            return new DeathwormGauntletSpecialModelRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<DeathwormGauntletArgument>> type() {
            return MAP_CODEC;
        }
    }
}
