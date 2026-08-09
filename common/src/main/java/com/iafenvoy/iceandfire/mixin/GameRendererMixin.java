package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.event.ClientEvents;
import com.iafenvoy.iceandfire.render.RenderVariables;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Camera mainCamera;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", shift = At.Shift.AFTER))
    private void onCameraSetup(DeltaTracker tickCounter, CallbackInfo ci) {
        ClientEvents.onCameraSetup(this.mainCamera);
    }

    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Ljava/lang/String;Lcom/mojang/blaze3d/vertex/VertexFormat;)V", shift = At.Shift.BEFORE))
    private void registerProgram(ResourceProvider factory, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> list2) {
        try {
            list2.add(Pair.of(new ShaderInstance(factory, "rendertype_dread_portal", DefaultVertexFormat.POSITION_COLOR), program -> RenderVariables.DREAD_PORTAL_PROGRAM = program));
        } catch (Exception e) {
            IceAndFire.LOGGER.error("Failed to load dread portal program", e);
        }
    }
}
