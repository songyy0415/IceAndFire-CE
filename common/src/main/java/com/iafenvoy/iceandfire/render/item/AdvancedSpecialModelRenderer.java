package com.iafenvoy.iceandfire.render.item;

import com.iafenvoy.uranus.client.model.AdvancedEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

/**
 * Base for items rendered from a hand-built {@link AdvancedEntityModel} (AdvancedModelBox) tree via
 * the 26.2 {@link SpecialModelRenderer} item extension point.
 *
 * <p>26.2 {@code Model.renderToBuffer} is final and only draws the single {@code ModelPart root},
 * so an AdvancedModelBox tree would render nothing through the vanilla model path. This feeds the
 * AdvancedModelBox geometry into the deferred render graph via
 * {@code SubmitNodeCollector.submitCustomGeometry} + {@code model.renderPartsToBuffer()}.
 */
public abstract class AdvancedSpecialModelRenderer<T> implements SpecialModelRenderer<T> {
    protected final AdvancedEntityModel<?> model;

    protected AdvancedSpecialModelRenderer(AdvancedEntityModel<?> model) {
        this.model = model;
    }

    /**
     * Submits the AdvancedModelBox geometry of {@link #model} with the given render type, rebuilding
     * a fresh {@code PoseStack} from the pose captured at submit time.
     */
    protected void renderModel(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int light, int overlay, int color) {
        this.renderModel(this.model, poseStack, submitNodeCollector, renderType, light, overlay, color);
    }

    /**
     * Variant of {@link #renderModel(PoseStack, SubmitNodeCollector, RenderType, int, int, int)} that
     * draws an explicit model (for renderers that swap between model classes, e.g. GorgonHead).
     */
    protected void renderModel(AdvancedEntityModel<?> model, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, int light, int overlay, int color) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            PoseStack fresh = new PoseStack();
            fresh.last().pose().set(pose.pose());
            fresh.last().normal().set(pose.normal());
            model.renderPartsToBuffer(fresh, buffer, light, overlay, color);
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
    }

    @Override
    public abstract T extractArgument(ItemStack stack);

    @Override
    public abstract void submit(T argument, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean outline, int color);
}
