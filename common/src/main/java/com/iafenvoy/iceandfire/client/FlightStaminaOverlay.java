package com.iafenvoy.iceandfire.client;

import com.iafenvoy.iceandfire.entity.util.IFlightStamina;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

/**
 * Stamina bar of the flight rework, drawn like the vanilla jump bar above the hotbar. It is shared by every mount
 * implementing {@link IFlightStamina} and only shows up while the ridden mount has the stamina system enabled.
 * <p>
 * MC 26.2 draws the HUD through {@link GuiGraphicsExtractor} instead of the 1.21.1 {@code LayeredDraw.Layer}; the Fabric
 * HUD element registration in {@code IceAndFireFabricClient} calls {@link #render} at the same place.
 */
@Environment(EnvType.CLIENT)
public final class FlightStaminaOverlay {
    private static final Identifier JUMP_BAR_BACKGROUND = Identifier.withDefaultNamespace("hud/jump_bar_background");
    private static final Identifier JUMP_BAR_PROGRESS = Identifier.withDefaultNamespace("hud/jump_bar_progress");

    private FlightStaminaOverlay() {
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;
        Entity vehicle = minecraft.player.getVehicle();
        if (!(vehicle instanceof IFlightStamina stamina) || !stamina.isFlightStaminaEnabled()) return;
        float maxStamina = stamina.getMaxFlightStamina();
        if (maxStamina <= 0F) return;
        float percentage = Math.max(0F, Math.min(1F, stamina.getFlightStamina() / maxStamina));

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int barWidth = 182;
        int barHeight = 5;
        int x = screenWidth / 2 - 91;
        int y = screenHeight - 25;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, JUMP_BAR_BACKGROUND, x, y, barWidth, barHeight);
        int fillWidth = Math.round(barWidth * percentage);
        if (fillWidth > 0) {
            graphics.enableScissor(x, y, x + fillWidth, y + barHeight);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, JUMP_BAR_PROGRESS, x, y, barWidth, barHeight);
            graphics.disableScissor();
        }
    }
}
