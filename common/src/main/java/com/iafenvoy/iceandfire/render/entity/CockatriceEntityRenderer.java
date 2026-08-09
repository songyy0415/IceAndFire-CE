package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.CockatriceEntity;
import com.iafenvoy.iceandfire.entity.util.IafEntityUtil;
import com.iafenvoy.iceandfire.render.entity.state.CockatriceRenderState;
import com.iafenvoy.iceandfire.render.misc.CockatriceBeamRenderer;
import com.iafenvoy.iceandfire.render.model.CockatriceChickModel;
import com.iafenvoy.iceandfire.render.model.CockatriceModel;
import com.iafenvoy.iceandfire.render.model.DragonBaseModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CockatriceEntityRenderer extends AdvancedEntityRendererBase<CockatriceEntity, CockatriceRenderState, DragonBaseModel<CockatriceRenderState>> {
    public static final Identifier TEXTURE_ROOSTER = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cockatrice/cockatrice_0.png");
    public static final Identifier TEXTURE_HEN = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cockatrice/cockatrice_1.png");
    public static final Identifier TEXTURE_ROOSTER_CHICK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cockatrice/cockatrice_0_chick.png");
    public static final Identifier TEXTURE_HEN_CHICK = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/cockatrice/cockatrice_1_chick.png");
    private final CockatriceModel adultModel = new CockatriceModel();
    private final CockatriceChickModel chickModel = new CockatriceChickModel();

    public CockatriceEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new CockatriceModel(), 0.6F);
    }

    @Override
    protected DragonBaseModel<CockatriceRenderState> getModel(CockatriceRenderState state) {
        return state.isBaby ? this.chickModel : this.adultModel;
    }

    @Override
    public CockatriceRenderState createRenderState() {
        return new CockatriceRenderState();
    }

    @Override
    public void extractRenderState(CockatriceEntity entity, CockatriceRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isHen = entity.isHen();
        state.stareProgress = entity.stareProgress;
        state.sitProgress = entity.sitProgress;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        LivingEntity livingentity = entity.getTargetedEntity();
        boolean blindness = entity.hasEffect(MobEffects.BLINDNESS) || livingentity != null && livingentity.hasEffect(MobEffects.BLINDNESS);
        state.beamActive = !blindness && livingentity != null
            && IafEntityUtil.isEntityLookingAt(entity, livingentity, CockatriceEntity.VIEW_RADIUS)
            && IafEntityUtil.isEntityLookingAt(livingentity, entity, CockatriceEntity.VIEW_RADIUS);
        if (state.beamActive) {
            state.startPos = entity.getEyePosition(partialTicks);
            state.targetPos = livingentity.getEyePosition(partialTicks);
            state.beamGameTime = (float) (entity.level().getGameTime() + partialTicks);
            state.attackAnimationScale = entity.getAttackAnimationScale(partialTicks);
        }
    }

    @Override
    public boolean shouldRender(CockatriceEntity entity, Frustum camera, double camX, double camY, double camZ) {
        if (super.shouldRender(entity, camera, camX, camY, camZ))
            return true;
        if (entity.hasTargetedEntity()) {
            LivingEntity livingentity = entity.getTargetedEntity();
            if (livingentity != null) {
                Vec3 Vector3d = new Vec3(livingentity.getX(), livingentity.getY() + livingentity.getBbHeight() * 0.5D, livingentity.getZ());
                Vec3 Vector3d1 = entity.getEyePosition();
                return camera.isVisible(new AABB(Vector3d1.x, Vector3d1.y, Vector3d1.z, Vector3d.x, Vector3d.y, Vector3d.z));
            }
        }
        return false;
    }

    @Override
    protected void submitExtra(CockatriceRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
        if (state.beamActive) {
            submitNodeCollector.submitCustomGeometry(poseStack, CockatriceBeamRenderer.TEXTURE_BEAM, (pose, buffer) -> {
                PoseStack fresh = new PoseStack();
                fresh.last().pose().set(pose.pose());
                fresh.last().normal().set(pose.normal());
                CockatriceBeamRenderer.render(state, fresh, buffer);
            });
        }
    }

    @Override
    protected void scale(CockatriceRenderState state, PoseStack matrixStackIn) {
        if (state.isBaby)
            matrixStackIn.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public Identifier getTextureLocation(CockatriceRenderState state) {
        return state.isBaby ? state.isHen ? TEXTURE_HEN_CHICK : TEXTURE_ROOSTER_CHICK : state.isHen ? TEXTURE_HEN : TEXTURE_ROOSTER;
    }
}
