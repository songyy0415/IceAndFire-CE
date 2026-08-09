package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.entity.DragonBaseEntity;
import com.iafenvoy.iceandfire.screen.handler.DragonScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DragonScreen extends AbstractContainerScreen<DragonScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/dragon.png");

    public DragonScreen(DragonScreenHandler dragonInv, Inventory playerInv, Component name) {
        super(dragonInv, playerInv, name, 176, 214);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        DragonBaseEntity dragon = this.menu.getDragon();
        float dragonScale = 1F / Math.max(0.0001F, dragon.getAgeScale());
        Quaternionf quaternionf = (new Quaternionf()).rotateY((float) Mth.lerp((float) mouseX / this.width, 0, Math.PI)).rotateZ((float) Mth.lerp((float) mouseY / this.width, Math.PI, Math.PI + 0.2));
        int size = (int) (dragonScale * 23F);
        int cx = this.leftPos + 88;
        int cy = this.topPos + (int) (0.5F * (dragon.flyProgress)) + 55;
        int boxHalf = Math.max(32, (int) Math.ceil(size * 2.0F));
        graphics.entity(createRenderState(dragon), size, new Vector3f(0), quaternionf, null, cx - boxHalf, cy - boxHalf, cx + boxHalf, cy + boxHalf);
        int l = this.topPos;
        String s3 = dragon.getCustomName() == null ? I18n.get("dragon.unnamed") : I18n.get("dragon.name") + " " + dragon.getCustomName().getString();
        graphics.text(this.font, s3, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s3) / 2), l + 75, ARGB.opaque(0XFFFFFF), false);
        String s2 = I18n.get("dragon.health") + " " + Math.floor(Math.min(dragon.getHealth(), dragon.getMaxHealth())) + " / " + dragon.getMaxHealth();
        graphics.text(this.font, s2, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s2) / 2), l + 84, ARGB.opaque(0XFFFFFF), false);
        String s = (dragon.isMale() ? "dragon.gender.male" : "dragon.gender.female");
        String s5 = I18n.get("dragon.gender") + I18n.get(s);
        graphics.text(this.font, s5, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s5) / 2), l + 93, ARGB.opaque(0XFFFFFF), false);
        String s6 = I18n.get("dragon.hunger") + dragon.getHunger() + "/100";
        graphics.text(this.font, s6, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s6) / 2), l + 102, ARGB.opaque(0XFFFFFF), false);
        String s4 = I18n.get("dragon.stage") + " " + dragon.getDragonStage() + " " + I18n.get("dragon.days.front") + dragon.getAgeInDays() + " " + I18n.get("dragon.days.back");
        graphics.text(this.font, s4, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s4) / 2), l + 111, ARGB.opaque(0XFFFFFF), false);
        String s7 = dragon.getOwner() != null ? I18n.get("dragon.owner") + dragon.getOwner().getName().getString() : I18n.get("dragon.untamed");
        graphics.text(this.font, s7, (int) (this.leftPos + (float) this.imageWidth / 2 - (float) this.font.width(s7) / 2), l + 120, ARGB.opaque(0XFFFFFF), false);
    }

    private static EntityRenderState createRenderState(DragonBaseEntity entity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super DragonBaseEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }
}
