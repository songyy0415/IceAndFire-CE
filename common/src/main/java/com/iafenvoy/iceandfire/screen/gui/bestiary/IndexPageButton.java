package com.iafenvoy.iceandfire.screen.gui.bestiary;

import com.iafenvoy.iceandfire.IceAndFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class IndexPageButton extends Button {
    private static final Identifier SPRITE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/bestiary/widgets.png");

    public IndexPageButton(int x, int y, Component buttonText, OnPress butn) {
        super(x, y, 160, 32, buttonText, butn, DEFAULT_NARRATION);
        this.width = 160;
        this.height = 32;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partial) {
        if (this.active) {
            boolean flag = this.isHoveredOrFocused();
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPRITE, this.getX(), this.getY(), 0, flag ? 32 : 0, this.width, this.height, 256, 256, ARGB.white(this.alpha));
            int i = -1;
            int color = i | Mth.ceil(this.alpha * 255.0F) << 24;
            Component text = this.getMessage();
            pGuiGraphics.text(Minecraft.getInstance().font, text, this.getX() + (this.width - Minecraft.getInstance().font.width(text)) / 2, this.getY() + (this.height - 8) / 2, color, false);
        }
    }
}
