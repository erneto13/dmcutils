package com.erneto.client.hud;

import com.erneto.client.core.CPEvent;
import com.erneto.client.core.CPTimelineStore;
import com.erneto.client.gui.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public final class CPHeatmapRenderer {

    private final CPTimelineStore store;
    private boolean enabled = false;

    private static final int MAX_ROWS = 8;
    private static final int PANEL_X = 6;
    private static final int PANEL_TOP = 30;
    private static final int ROW_H = 11;

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
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (!enabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Vec3d playerPos = mc.player.getEyePos();

        List<CPEvent.ContainerEvent> nearby = store.allLocatable().stream()
                .sorted(Comparator.comparingDouble(e -> distanceSq(playerPos, e)))
                .limit(MAX_ROWS)
                .toList();

        int panelH = 14 + nearby.size() * ROW_H;
        int panelW = 170;

        ctx.fill(PANEL_X, PANEL_TOP, PANEL_X + panelW, PANEL_TOP + panelH, Theme.PANEL);
        ctx.fill(PANEL_X, PANEL_TOP, PANEL_X + panelW, PANEL_TOP + 12, Theme.PANEL_ALT);
        ctx.drawText(mc.textRenderer, "§bCP Heatmap §7(" + nearby.size() + ")",
                PANEL_X + 4, PANEL_TOP + 2, Theme.TEXT_PRI, false);

        if (nearby.isEmpty()) {
            ctx.drawText(mc.textRenderer, "§8Sin eventos con coordenadas",
                    PANEL_X + 4, PANEL_TOP + 14, Theme.TEXT_DIM, false);
            return;
        }

        int y = PANEL_TOP + 14;
        for (CPEvent.ContainerEvent e : nearby) {
            double dist = Math.sqrt(distanceSq(playerPos, e));
            String dir = direction(playerPos, e);
            String line = "§f" + e.user() + " §7" + dir + " §e" + Math.round(dist) + "m";
            ctx.drawText(mc.textRenderer, line, PANEL_X + 4, y, Theme.TEXT_SEC, false);
            y += ROW_H;
        }
    }

    private double distanceSq(Vec3d playerPos, CPEvent.ContainerEvent e) {
        double dx = e.x() - playerPos.x;
        double dy = e.y() - playerPos.y;
        double dz = e.z() - playerPos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private String direction(Vec3d playerPos, CPEvent.ContainerEvent e) {
        double dx = e.x() - playerPos.x;
        double dz = e.z() - playerPos.z;
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360;

        String[] compass = {"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
        int idx = (int) Math.round(angle / 45.0) % 8;
        return compass[idx];
    }
}