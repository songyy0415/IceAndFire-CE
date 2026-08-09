package com.iafenvoy.iceandfire.render;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.config.IafClientConfig;
import com.iafenvoy.iceandfire.registry.IafStatusEffects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class SirenShaderRenderHelper {
    private static final Identifier SIREN_SHADER = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "shaders/post/siren.json");

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (IafClientConfig.INSTANCE.sirenShader.getValue() && player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(IafStatusEffects.SIREN_CHARM.get())))
            enableShader(renderer);
        else disableShader(renderer);
    }

    private static boolean enabled(GameRenderer renderer) {
        PostChain processor = renderer.currentEffect();
        return processor != null && SIREN_SHADER.toString().equals(processor.getName());
    }

    private static void enableShader(GameRenderer renderer) {
        if (enabled(renderer)) return;
        renderer.loadEffect(SIREN_SHADER);
    }

    private static void disableShader(GameRenderer renderer) {
        if (!enabled(renderer)) return;
        renderer.shutdownEffect();
    }
}
