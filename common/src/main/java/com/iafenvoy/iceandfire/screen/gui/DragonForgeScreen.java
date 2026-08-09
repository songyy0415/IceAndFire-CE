package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.screen.handler.DragonForgeScreenHandler;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class DragonForgeScreen extends AbstractContainerScreen<DragonForgeScreenHandler> {
    public DragonForgeScreen(DragonForgeScreenHandler container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        String s = I18n.get("block.iceandfire.dragonforge_" + this.menu.getDragonType().name() + "_core");
        graphics.text(this.font, s, this.imageWidth / 2 - this.font.width(s) / 2, 6, ARGB.opaque(4210752), false);
        graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, ARGB.opaque(4210752), false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        Identifier texture = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, String.format(Locale.ROOT, "textures/gui/dragonforge_%s.png", this.menu.getDragonType().name()));

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.getMaxCookTime() > 0)
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos + 12, this.topPos + 23, 0.0F, 166.0F, 125 * this.menu.getCookTime() / this.menu.getMaxCookTime(), 38, 256, 256);
    }
}
