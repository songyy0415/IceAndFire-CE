package com.iafenvoy.iceandfire.registry;

import com.iafenvoy.iceandfire.render.block.DreadPortalBlockEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Iaf-specific render types, migrated to the MC26.2 {@code RenderTypes} factory set.
 *
 * <p>MC26.2 {@code RenderType} is final and its {@code create(name, RenderSetup)} entry point is
 * package-private, so custom pipelines (custom shaders / blend / depth) can no longer be exposed as
 * a {@code RenderType} from a mod. Every method below therefore returns the closest stock
 * {@code RenderTypes} render type; the caller-facing API (name + signature) is unchanged.
 *
 * <ul>
 *   <li>{@code getGhost} — was ENTITY_CUTOUT_NO_CULL shader + additive-ish ghost blend + NO_CULL →
 *       {@code entityCutout} (same cutout pipeline, no cull, standard blend).</li>
 *   <li>{@code getGhostDaytime} — was ENTITY_CUTOUT_NO_CULL + translucent blend → {@code entityTranslucent}.</li>
 *   <li>{@code getDreadlandsPortal} — was a custom shader (RenderVariables.DREAD_PORTAL_PROGRAM, removed in P7-H2)
 *       with two textures (POSITION_COLOR). Custom shader pipelines are not creatable in 26.2; uses
 *       {@code entityCutout} with the portal texture. Legacy {@code rendertype_dread_portal} shader
 *       resources removed in P7-H2 (no consumers).</li>
 *   <li>{@code getStoneMobRenderType} — was ENTITY_CUTOUT with the stone texture → {@code entitySolid}.</li>
 *   <li>{@code getIce} — was BEACON_BEAM shader + translucent + cull → {@code beaconBeam}.</li>
 *   <li>{@code getStoneCrackRenderType} — was ENTITY_CUTOUT + EQUAL_DEPTH_TEST + NO_CULL + translucent.
 *       EQ_DEPTH cannot be expressed in 26.2 stock types → {@code entityTranslucent}.</li>
 * </ul>
 */
public final class IafRenderLayers {
    private static final Identifier STONE_TEXTURE = Identifier.fromNamespaceAndPath(Identifier.DEFAULT_NAMESPACE, "textures/block/stone.png");

    private IafRenderLayers() {
    }

    public static RenderType getGhost(Identifier locationIn) {
        return RenderTypes.entityCutout(locationIn);
    }

    public static RenderType getGhostDaytime(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getDreadlandsPortal() {
        return RenderTypes.entityCutout(DreadPortalBlockEntityRenderer.DREAD_PORTAL);
    }

    public static RenderType getStoneMobRenderType(float x, float y) {
        return RenderTypes.entitySolid(STONE_TEXTURE);
    }

    public static RenderType getIce(Identifier locationIn) {
        return RenderTypes.beaconBeam(locationIn, false);
    }

    public static RenderType getStoneCrackRenderType(Identifier crackTex) {
        return RenderTypes.entityTranslucent(crackTex);
    }
}
