package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.screen.handler.PodiumScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class PodiumScreen extends AbstractContainerScreen<PodiumScreenHandler> {
    public static final Identifier PODIUM_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/podium.png");

    public PodiumScreen(PodiumScreenHandler container, Inventory inv, Component name) {
        super(container, inv, name, 176, 133);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
        if (this.menu != null) {
            String s = I18n.get("block.iceandfire.podium");
            graphics.text(this.font, s, this.imageWidth / 2 - this.font.width(s) / 2, 6, ARGB.opaque(4210752), false);
        }
        graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, ARGB.opaque(4210752), false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, PODIUM_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
    }
}
