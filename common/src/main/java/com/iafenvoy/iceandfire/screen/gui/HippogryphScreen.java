package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HippogryphEntity;
import com.iafenvoy.iceandfire.screen.handler.HippogryphScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class HippogryphScreen extends AbstractContainerScreen<HippogryphScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/hippogryph.png");

    public HippogryphScreen(HippogryphScreenHandler handler, Inventory playerInv, Component name) {
        super(handler, playerInv, name);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.menu.getHippogryph().getDisplayName().getString(), 8, 6, ARGB.opaque(4210752), false);
        graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, ARGB.opaque(4210752), false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        HippogryphEntity hippo = this.menu.getHippogryph();
        if (hippo.isChested()) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 79, j + 17, 0.0F, this.imageHeight, 5 * 18, 54, 256, 256);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, i + 26, j + 18, i + 77, j + 69, 17, 0.25F, mouseX, mouseY, hippo);
    }
}
