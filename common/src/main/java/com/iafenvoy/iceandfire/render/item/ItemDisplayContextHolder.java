package com.iafenvoy.iceandfire.render.item;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Carries the current {@link ItemDisplayContext} from the 26.2 item-render pipeline
 * ({@code ItemStackRenderState.submit}) into a {@code SpecialModelRenderer}. The 26.2
 * {@code SpecialModelRenderer.submit} signature does not receive the display context, but some
 * items (e.g. the tide trident) need to pick a different representation per context — the tide
 * trident's pre-migration renderer drew a flat icon for GUI/FIXED/NONE/GROUND and the 3D model
 * for hand-held contexts.
 */
public final class ItemDisplayContextHolder {
    public static final ThreadLocal<ItemDisplayContext> CURRENT = new ThreadLocal<>();

    private ItemDisplayContextHolder() {
    }
}
