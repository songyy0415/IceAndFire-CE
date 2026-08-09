package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.HippocampusEntity;
import com.iafenvoy.iceandfire.screen.handler.HippocampusScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class HippocampusScreen extends AbstractContainerScreen<HippocampusScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/hippogryph.png");

    public HippocampusScreen(HippocampusScreenHandler handler, Inventory playerInv, Component name) {
        super(handler, playerInv, name);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int k = 0;
        int l = 0;
        graphics.text(this.font, this.menu.getHippocampus().getDisplayName().getString(), l + 8, 6, ARGB.opaque(4210752), false);
        graphics.text(this.font, this.playerInventoryTitle, k + 8, l + this.imageHeight - 96 + 2, ARGB.opaque(4210752), false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
        super.extractBackground(graphics, mouseX, mouseY, tickDelta);
        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        HippocampusEntity hippo = this.menu.getHippocampus();
        if (hippo.isChested()) graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 79, j + 17, 0.0F, this.imageHeight, 5 * 18, 54, 256, 256);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, i + 26, j + 18, i + 77, j + 69, 17, 0.25F, mouseX, mouseY, hippo);
    }
}
