package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.DragonType;
import com.iafenvoy.iceandfire.entity.DragonSkullEntity;
import com.iafenvoy.iceandfire.entity.util.dragon.DragonSize;
import com.iafenvoy.iceandfire.registry.IafDragonTypes;
import com.iafenvoy.iceandfire.registry.IafRegistries;
import com.iafenvoy.iceandfire.registry.IafRenderers;
import com.iafenvoy.iceandfire.render.entity.state.DragonSkullRenderState;
import com.iafenvoy.iceandfire.render.model.animator.FireDragonTabulaModelAnimator;
import com.iafenvoy.iceandfire.render.model.animator.IceDragonTabulaModelAnimator;
import com.iafenvoy.iceandfire.render.model.animator.LightningTabulaDragonAnimator;
import com.iafenvoy.uranus.client.model.ITabulaModelAnimator;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.iafenvoy.uranus.client.model.basic.BasicModelPart;
import com.iafenvoy.uranus.client.model.util.TabulaModelHandlerHelper;
import com.iafenvoy.uranus.event.Event;
import com.iafenvoy.uranus.util.function.MemorizeSupplier;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class DragonSkullEntityRenderer extends EntityRenderer<DragonSkullEntity, DragonSkullRenderState> {
    public static final Event<Consumer<BiConsumer<DragonType, Map.Entry<Identifier, MemorizeSupplier<ITabulaModelAnimator<? extends LivingEntityRenderState>>>>>> COLLECT_DRAGON_SKULL_MODELS = new Event<>(callbacks -> consumer -> callbacks.forEach(x -> x.accept(consumer)));
    private final Map<DragonType, Map.Entry<Identifier, MemorizeSupplier<ITabulaModelAnimator<? extends LivingEntityRenderState>>>> models = new HashMap<>();

    static {
        COLLECT_DRAGON_SKULL_MODELS.register(consumer -> {
            consumer.accept(IafDragonTypes.FIRE, Map.entry(IafRenderers.FIRE_DRAGON, new MemorizeSupplier<>(FireDragonTabulaModelAnimator::new)));
            consumer.accept(IafDragonTypes.ICE, Map.entry(IafRenderers.ICE_DRAGON, new MemorizeSupplier<>(IceDragonTabulaModelAnimator::new)));
            consumer.accept(IafDragonTypes.LIGHTNING, Map.entry(IafRenderers.LIGHTNING_DRAGON, new MemorizeSupplier<>(LightningTabulaDragonAnimator::new)));
        });
    }

    public DragonSkullEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        COLLECT_DRAGON_SKULL_MODELS.invoker().accept(this.models::put);
    }

    private static void setRotationAngles(BasicModelPart cube, float rotX) {
        cube.rotateAngleX = rotX;
        cube.rotateAngleY = (float) 0;
        cube.rotateAngleZ = (float) 0;
    }

    @Override
    public DragonSkullRenderState createRenderState() {
        return new DragonSkullRenderState();
    }

    @Override
    public void extractRenderState(DragonSkullEntity entity, DragonSkullRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightCoords = this.getPackedLightCoords(entity, partialTicks);
        state.dragonType = IafRegistries.DRAGON_TYPE.getValue(IceAndFire.id(entity.getDragonType()));
        state.texture = state.dragonType.getSkeletonTexture(entity.getDragonStage());
        state.yRot = entity.getYRot();
        state.isOnWall = entity.isOnWall();
        state.size = this.getRenderSize(entity);
    }

    @Override
    public void submit(DragonSkullRenderState state, PoseStack matrixStackIn, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Map.Entry<Identifier, MemorizeSupplier<ITabulaModelAnimator<? extends LivingEntityRenderState>>> p = this.models.get(state.dragonType);
        if (p == null) return;
        TabulaModel<? extends LivingEntityRenderState> model = TabulaModelHandlerHelper.getModel(p.getKey());
        if (model == null) return;
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(-180.0F));
        matrixStackIn.mulPose(Axis.YN.rotationDegrees(-180.0F - state.yRot));
        matrixStackIn.scale(1.0F, 1.0F, 1.0F);
        float size = state.size / 3;
        matrixStackIn.scale(size, size, size);
        matrixStackIn.translate(0, state.isOnWall ? -0.24F : -0.12F, state.isOnWall ? 0.4F : 0.5F);
        submitNodeCollector.submitCustomGeometry(matrixStackIn, RenderTypes.entityTranslucent(state.texture), (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            model.resetToDefaultPose();
            setRotationAngles(model.getCube("Head"), state.isOnWall ? (float) Math.toRadians(50F) : 0F);
            model.getCube("Head").render(fresh, buffer, state.lightCoords, OverlayTexture.NO_OVERLAY, -1);
        });
        matrixStackIn.popPose();
    }

    public float getRenderSize(DragonSkullEntity skull) {
        DragonSize size = DragonSize.getSize(skull.getDragonStage());
        float step = size.step() / 25;
        if (skull.getDragonAge() > 125) return size.x0() + ((step * 25));
        return size.x0() + ((step * this.getAgeFactor(skull)));
    }

    private int getAgeFactor(DragonSkullEntity skull) {
        return (skull.getDragonStage() > 1 ? skull.getDragonAge() - (25 * (skull.getDragonStage() - 1)) : skull.getDragonAge());
    }
}
