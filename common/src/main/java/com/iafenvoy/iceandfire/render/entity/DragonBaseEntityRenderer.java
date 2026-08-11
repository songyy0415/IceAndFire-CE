package com.iafenvoy.iceandfire.render.entity;

import com.iafenvoy.iceandfire.data.DragonColor;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.render.entity.feature.DragonArmorFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.DragonBannerFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.DragonEyesFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.DragonMaleOverlayFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.feature.DragonRiderFeatureRenderer;
import com.iafenvoy.iceandfire.render.entity.state.DragonRenderState;
import com.iafenvoy.uranus.client.model.TabulaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class DragonBaseEntityRenderer<T extends DragonBaseEntity> extends AdvancedEntityRendererBase<T, DragonRenderState, TabulaModel<DragonRenderState>> {
    public DragonBaseEntityRenderer(EntityRendererProvider.Context context, TabulaModel<DragonRenderState> model) {
        super(context, model, 0.0025F);
        this.addLayer(new DragonMaleOverlayFeatureRenderer(this));
        this.addLayer(new DragonEyesFeatureRenderer(this));
        this.addLayer(new DragonRiderFeatureRenderer(this));
        this.addLayer(new DragonBannerFeatureRenderer(this));
        this.addLayer(new DragonArmorFeatureRenderer(this));
    }

    @Override
    public DragonRenderState createRenderState() {
        return new DragonRenderState();
    }

    @Override
    public void extractRenderState(T entity, DragonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.partialTicks = partialTicks;
        state.renderSize = entity.getRenderSize();
        state.dragonScale = entity.getRenderSize() / 3;
        state.dragonPitch = Mth.lerp(partialTicks, entity.prevDragonPitch, entity.getDragonPitch());
        state.prevDragonPitch = entity.prevDragonPitch;
        state.shouldRenderEyes = entity.shouldRenderEyes();
        state.isMale = entity.isMale();
        state.isSkeletal = entity.isSkeletal();
        state.variant = entity.getVariant();
        state.dragonStage = entity.getDragonStage();
        state.isHovering = entity.isHovering();
        state.isFlying = entity.isFlying();
        state.isInWater = entity.isInWater();
        state.isSleeping = entity.isSleeping();
        state.isModelDead = entity.isModelDead();
        state.isNoAi = entity.isNoAi();
        state.isActuallyBreathingFire = entity.isActuallyBreathingFire();
        state.isBreathingFire = entity.isBreathingFire();
        state.isVehicle = entity.isVehicle();
        state.isPassenger = entity.isPassenger();
        state.sitProgress = entity.sitProgress;
        state.sleepProgress = entity.sleepProgress;
        state.hoverProgress = entity.hoverProgress;
        state.flyProgress = entity.flyProgress;
        state.fireBreathProgress = entity.fireBreathProgress;
        state.diveProgress = entity.diveProgress;
        state.prevDiveProgress = entity.prevDiveProgress;
        state.prevFireBreathProgress = entity.prevFireBreathProgress;
        state.modelDeadProgress = entity.modelDeadProgress;
        state.prevModelDeadProgress = entity.prevModelDeadProgress;
        state.ridingProgress = entity.ridingProgress;
        state.tackleProgress = entity.tackleProgress;
        state.swimProgress = entity.swimProgress;
        state.walkCycle = entity.walkCycle;
        state.flightCycle = entity.flightCycle;
        state.swimCycle = entity.swimCycle;
        System.arraycopy(entity.prevAnimationProgresses, 0, state.prevAnimationProgresses, 0, entity.prevAnimationProgresses.length);
        state.bbWidth = entity.getBbWidth();
        state.turnBuffer = entity.turn_buffer;
        state.tailBuffer = entity.tail_buffer;
        state.rollBuffer = entity.roll_buffer;
        state.pitchBuffer = entity.pitch_buffer;
        state.pitchBufferBody = entity.pitch_buffer_body;
        state.legSolver = entity.legSolver;
        state.animation = entity.getAnimation();
        state.animationTick = entity.getAnimationTick();
        state.animations = entity.getAnimations();
        state.shakingPrey = entity.getAnimation() == DragonBaseEntity.ANIMATION_SHAKEPREY;
        DragonColor color = DragonColor.getById(entity.getVariant());
        state.texture = color.getTextureProvider().getTextureByEntity(entity);
        state.eyesTexture = color.getTextureProvider().getEyesTexture(entity.getDragonStage());
        state.maleOverlayTexture = color.getTextureProvider().getMaleOverlay();
        state.armorHead = DragonArmorFeatureRenderer.getArmorTexture(entity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD);
        state.armorChest = DragonArmorFeatureRenderer.getArmorTexture(entity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
        state.armorLegs = DragonArmorFeatureRenderer.getArmorTexture(entity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
        state.armorFeet = DragonArmorFeatureRenderer.getArmorTexture(entity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET);
        ItemStack offhand = entity.getItemInHand(InteractionHand.OFF_HAND);
        if (offhand.getItem() instanceof BannerItem) {
            this.itemModelResolver.updateForLiving(state.bannerItem, offhand, ItemDisplayContext.NONE, entity);
        } else {
            state.bannerItem.clear();
        }
        state.preyRenderStates.clear();
        state.preyModelTypes.clear();
        state.preyIsPrey.clear();
        LivingEntity controllingRider = entity.getControllingPassenger();
        for (Entity passenger : entity.getPassengers()) {
            // Restores the original 1.21.1 distinction: the controlling rider is drawn on the
            // dragon's back (prey = false branch of DragonRiderFeatureRenderer) and its standalone
            // world render is suppressed by PlayerEntityRendererMixin; only non-rider passengers
            // (mouth prey, matching positionRider's updatePreyInMouth branch) are drawn in the jaws.
            boolean isPrey = controllingRider == null || !controllingRider.getUUID().equals(passenger.getUUID());
            state.preyRenderStates.add(Minecraft.getInstance().getEntityRenderDispatcher().extractEntity(passenger, partialTicks));
            byte modelType = 2;
            EntityRenderer<?, ?> render = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(passenger);
            if (render instanceof MobRenderer mobRenderer) {
                EntityModel<?> modelBase = mobRenderer.getModel();
                if (modelBase instanceof HorseModel) modelType = 1;
                else if (modelBase instanceof QuadrupedModel) modelType = 2;
                else if (modelBase instanceof HumanoidModel) modelType = 0;
                else if (passenger.getBbHeight() > passenger.getBbWidth()) modelType = 0;
            } else if (passenger.getBbHeight() > passenger.getBbWidth()) modelType = 0;
            state.preyModelTypes.add(modelType);
            state.preyIsPrey.add(isPrey);
        }
    }

    @Override
    protected void scale(DragonRenderState state, PoseStack matrixStackIn) {
        this.shadowRadius = state.renderSize / 3;
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(state.dragonPitch));
        matrixStackIn.scale(this.shadowRadius, this.shadowRadius, this.shadowRadius);
    }

    @Override
    protected AABB getBoundingBoxForCulling(T entity) {
        // The dragon model (head + long tail + wings, rendered at renderSize/3 scale) extends far
        // beyond the 0.78x1.2 base entity AABB used by the default culling box. Without this, the
        // whole dragon is culled (and vanishes) as soon as the camera gets near the tail or wing
        // tips and the small torso box leaves the frustum. 1.5x renderSize covers the head (~0.52r),
        // tail tip (~0.65r) and wings (~0.47r) with margin in every yaw.
        float r = entity.getRenderSize();
        return super.getBoundingBoxForCulling(entity).inflate(r * 1.5);
    }

    @Override
    public Identifier getTextureLocation(DragonRenderState state) {
        return state.texture;
    }
}
