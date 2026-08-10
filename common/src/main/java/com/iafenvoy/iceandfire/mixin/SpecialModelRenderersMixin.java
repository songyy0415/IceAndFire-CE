package com.iafenvoy.iceandfire.mixin;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.render.item.DeathwormGauntletSpecialModelRenderer;
import com.iafenvoy.iceandfire.render.item.GorgonHeadSpecialModelRenderer;
import com.iafenvoy.iceandfire.render.item.TideTridentSpecialModelRenderer;
import com.iafenvoy.iceandfire.render.item.TrollWeaponSpecialModelRenderer;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpecialModelRenderers.class)
public class SpecialModelRenderersMixin {
    @Shadow
    @Final
    private static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends SpecialModelRenderer.Unbaked<?>>> ID_MAPPER;

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void iceandfire$registerSpecialModels(CallbackInfo ci) {
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "tide_trident"), TideTridentSpecialModelRenderer.Unbaked.MAP_CODEC);
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "troll_weapon"), TrollWeaponSpecialModelRenderer.Unbaked.MAP_CODEC);
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "deathworm_gauntlet"), DeathwormGauntletSpecialModelRenderer.Unbaked.MAP_CODEC);
        ID_MAPPER.put(Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "gorgon_head"), GorgonHeadSpecialModelRenderer.Unbaked.MAP_CODEC);
    }
}
