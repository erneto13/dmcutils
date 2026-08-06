package com.erneto.client.hud;

import com.erneto.client.core.CPEvent;
import com.erneto.client.core.CPTimelineStore;
import com.erneto.client.gui.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class CPHeatmapRenderer {

    private final CPTimelineStore store;
    private boolean enabled = false;

    public CPHeatmapRenderer(CPTimelineStore store) {
        this.store = store;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void register() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::render);
    }

    private void render(WorldRenderContext context) {
        if (!enabled) return;

        Vec3d camera = context.camera().getPos();
        VertexConsumerProvider.Immediate consumers = (VertexConsumerProvider.Immediate) context.consumers();
        if (consumers == null) return;

        MatrixStack matrices = context.matrixStack();
        if (matrices == null) return;

        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        // Only ContainerEvent carries coordinates given block logging is off —
        // this is intentionally limited to that until we confirm other types via network-debug.
        for (CPEvent.ContainerEvent e : store.allLocatable()) {
            Box box = new Box(e.x(), e.y(), e.z(), e.x() + 1, e.y() + 1, e.z() + 1);
            VertexConsumer buffer = consumers.getBuffer(RenderLayer.getLines());
            WorldRenderer.drawBox(matrices, buffer, box, 0.13f, 0.83f, 0.93f, 0.9f); // Theme.ACCENT tone
        }

        consumers.draw();
        matrices.pop();
    }
}