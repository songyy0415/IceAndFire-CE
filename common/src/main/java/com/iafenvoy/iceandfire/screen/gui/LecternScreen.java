package com.iafenvoy.iceandfire.screen.gui;

import com.iafenvoy.iceandfire.IceAndFire;
import com.iafenvoy.iceandfire.data.BestiaryPage;
import com.iafenvoy.iceandfire.registry.IafItems;
import com.iafenvoy.iceandfire.registry.IafRegistries;
import com.iafenvoy.iceandfire.screen.handler.LecternScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public class LecternScreen extends AbstractContainerScreen<LecternScreenHandler> {
    private static final Identifier ENCHANTMENT_TABLE_GUI_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/gui/lectern.png");
    private static final Identifier ENCHANTMENT_TABLE_BOOK_TEXTURE = Identifier.fromNamespaceAndPath(IceAndFire.MOD_ID, "textures/entity/lectern_book.png");
    private static BookModel bookModel;
    private final Random random = new Random();
    private final Component nameable;
    public int ticks;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;
    private int flapTimer = 0;

    public LecternScreen(LecternScreenHandler container, Inventory inv, Component name) {
        super(container, inv, name);
        this.nameable = name;
    }

    @Override
    protected void init() {
        super.init();
        assert this.minecraft != null;
        bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.nameable.getString(), 12, 4, ARGB.opaque(4210752), false);
        graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, ARGB.opaque(4210752), false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.menu.onUpdate();
        this.tickBook();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        assert this.minecraft != null;
        assert this.minecraft.gameMode != null;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        for (int k = 0; k < 3; ++k) {
            double l = event.x() - (i + 60);
            double i1 = event.y() - (j + 14 + 19 * k);
            if (l >= 0 && i1 >= 0 && l < 108 && i1 < 19 && this.menu.clickMenuButton(this.minecraft.player, k)) {
                this.flapTimer = 5;
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        int i = this.leftPos;
        int j = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.extractBook(graphics, i, j, partialTicks);
        for (int i1 = 0; i1 < 3; ++i1) {
            int j1 = i + 60;
            int k1 = j1 + 20;
            int l1 = this.menu.getPossiblePages()[i1] == null ? -1 : IafRegistries.BESTIARY_PAGE.getId(this.menu.getPossiblePages()[i1]);
            if (l1 == -1)
                graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, 0.0F, 185.0F, 108, 19, 256, 256);
            else {
                String s = "" + 3;
                String s1 = "";
                float textScale = 1.0F;
                BestiaryPage enchantment = this.menu.getPossiblePages()[i1];
                if (enchantment != null) {
                    s1 = I18n.get("bestiary." + enchantment.name());
                    if (this.font.width(s1) > 80)
                        textScale = 1.0F - (this.font.width(s1) - 80) * 0.01F;
                }
                int j2 = 6839882;
                if (this.menu.getSlot(0).getItem().getItem() == IafItems.BESTIARY.get()) {
                    int k2 = mouseX - (i + 60);
                    int l2 = mouseY - (j + 14 + 19 * i1);
                    int j3 = 0X9F988C;
                    if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, 0.0F, 204.0F, 108, 19, 256, 256);
                        j2 = 16777088;
                        j3 = 16777088;
                    } else
                        graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, 0.0F, 166.0F, 108, 19, 256, 256);

                    graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1 + 1, j + 15 + 19 * i1, 16 * i1, 223, 16, 16, 256, 256);
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(this.width / 2F - 10, this.height / 2F - 83 + (1.0F - textScale) * 55);
                    graphics.pose().scale(textScale, textScale);
                    graphics.text(this.font, s1, 0, 20 + 19 * i1, ARGB.opaque(j2), false);
                    graphics.pose().popMatrix();
                    graphics.text(this.font, s, k1 + 84 - this.font.width(s), j + 13 + 19 * i1 + 7, ARGB.opaque(j3), true);
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, 0.0F, 185.0F, 108, 19, 256, 256);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1 + 1, j + 15 + 19 * i1, 16 * i1, 239, 16, 16, 256, 256);
                }
            }
        }
    }

    private void extractBook(GuiGraphicsExtractor graphics, int left, int top, float partialTicks) {
        float a = partialTicks;
        float open = Mth.lerp(a, this.oOpen, this.open);
        float flip = Mth.lerp(a, this.oFlip, this.flip);
        graphics.book(bookModel, ENCHANTMENT_TABLE_BOOK_TEXTURE, 120.0F, open, flip, left + 20, top + 20, left + 156, top + 110);
    }

    public void tickBook() {
        ItemStack itemstack = this.menu.getSlot(0).getItem();

        if (!ItemStack.matches(itemstack, this.last)) {
            this.last = itemstack;
            do this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
            while (this.flip <= this.flipT + 1.0F && this.flip >= this.flipT - 1.0F);
        }
        ++this.ticks;
        this.oFlip = this.flip;
        this.oOpen = this.open;

        boolean flag = false;
        for (int i = 0; i < 3; ++i)
            if (this.menu.getPossiblePages()[i] != null)
                flag = true;
        this.open += flag ? 0.2F : -0.2F;

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        if (this.flapTimer > 0) {
            assert this.minecraft != null;
            f1 = (this.ticks + this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)) * 0.5F;
            this.flapTimer--;
        }
        f1 = Mth.clamp(f1, -0.2F, 0.2F);
        this.flipA += (f1 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }
}
