package com.iafenvoy.iceandfire.event;

import com.iafenvoy.iceandfire.data.component.ChainData;
import com.iafenvoy.iceandfire.data.component.MiscData;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.entity.util.ICustomMoveController;
import com.iafenvoy.iceandfire.network.payload.DragonControlC2SPayload;
import com.iafenvoy.iceandfire.registry.IafKeybindings;
import com.iafenvoy.iceandfire.registry.IafStatusEffects;
import com.iafenvoy.iceandfire.render.misc.ChainRenderer;
import com.iafenvoy.iceandfire.render.misc.CockatriceBeamRenderer;
import com.iafenvoy.iceandfire.render.misc.FrozenStateRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Environment(EnvType.CLIENT)
public final class ClientEvents {
    public static int currentView = 0;
    public static final CopyOnWriteArrayList<Pair<Vec3, Vec3>> LIGHTNINGS = new CopyOnWriteArrayList<>();

    public static void onCameraSetup(Camera camera) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.getVehicle() instanceof DragonBaseEntity) {
            float scale = ((DragonBaseEntity) player.getVehicle()).getRenderSize() / 3;
            if (Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                    Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
                if (currentView == 1) camera.move(-camera.getMaxZoom(scale * 1.2F), 0F, 0);
                else if (currentView == 2) camera.move(-camera.getMaxZoom(scale * 3F), 0F, 0);
                else if (currentView == 3) camera.move(-camera.getMaxZoom(scale * 5F), 0F, 0);
            }
        }
    }

    public static void onLivingUpdate(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity instanceof ICustomMoveController moveController) {
            if (entity.getVehicle() != null && entity.getVehicle() == mc.player) {
                byte previousState = moveController.getControlState();
                moveController.dismount(mc.options.keyShift.isDown());
                byte controlState = moveController.getControlState();
                if (controlState != previousState)
                    NetworkManager.sendToServer(new DragonControlC2SPayload(entity.getId(), controlState, entity.blockPosition()));
            }
        }
        if (entity instanceof Player player && player == Minecraft.getInstance().player && player.getVehicle() instanceof ICustomMoveController controller) {
            Entity vehicle = player.getVehicle();
            byte previousState = controller.getControlState();
            controller.up(mc.options.keyJump.isDown());
            controller.down(IafKeybindings.DRAGON_DOWN.isDown());
            controller.attack(IafKeybindings.DRAGON_STRIKE.isDown());
            controller.dismount(mc.options.keyShift.isDown());
            controller.strike(IafKeybindings.DRAGON_BREATH.isDown());
            byte controlState = controller.getControlState();
            if (controlState != previousState)
                NetworkManager.sendToServer(new DragonControlC2SPayload(vehicle.getId(), controlState, vehicle.blockPosition()));
        }
    }

    public static void onPostRenderLiving(LivingEntity entity, float partialRenderTick, PoseStack matrixStack, MultiBufferSource buffers, int light) {
        MiscData miscData = MiscData.get(entity);
        ClientLevel world = Minecraft.getInstance().level;
        if (world == null) return;
        miscData.checkScepterTarget(world.entityStorage.getEntityGetter()::get);
        //Cockatrice Beam
        for (Entity target : miscData.getTargetedByScepters().stream().filter(Objects::nonNull).map(x -> world.entityStorage.getEntityGetter().get(x)).filter(Objects::nonNull).toList())
            CockatriceBeamRenderer.render(entity, target, matrixStack, buffers, partialRenderTick);
        //Frozen
        MobEffectInstance effect = entity.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(IafStatusEffects.FROZEN.get()));
        if (effect != null) FrozenStateRenderer.render(entity, matrixStack, buffers, light, effect.getDuration());
        //Chain
        ChainData chainData = ChainData.get(entity);
        ChainRenderer.render(entity, matrixStack, buffers, light, chainData.getChainedTo());
    }
}