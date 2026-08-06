package com.erneto.client.gui;

import com.erneto.client.core.CPEvent;
import com.erneto.client.core.CPTimelineStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class PlayerTimelineScreen extends Screen {

    private final Screen parent;
    private final String targetUser;
    private final List<CPEvent> events;

    private static final int ROWS_VISIBLE = 12;
    private static final int ROW_TOP = 44;

    private int scrollOffset = 0;

    public PlayerTimelineScreen(Screen parent, CPTimelineStore store, String targetUser) {
        super(Text.literal("Timeline — " + targetUser));
        this.parent = parent;
        this.targetUser = targetUser;
        this.events = store.timelineFor(targetUser);
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(width - 64, height - 24, 58, 18).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG);
        Theme.drawHeader(ctx, textRenderer, width, "Timeline — " + targetUser,
                events.size() + " eventos", Theme.ACCENT);

        int rowsTop = ROW_TOP;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int idx = i + scrollOffset;
            if (idx >= events.size()) break;

            int ry = rowsTop + i * Theme.ROW_H;
            ctx.fill(0, ry, width, ry + Theme.ROW_H, i % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);
            ctx.drawText(textRenderer, describe(events.get(idx)), 10, ry + 6, Theme.TEXT_PRI, false);
        }

        if (events.isEmpty()) {
            String msg = "Sin eventos registrados para " + targetUser;
            int msgW = textRenderer.getWidth(msg);
            ctx.drawText(textRenderer, msg, (width - msgW) / 2, height / 2, Theme.TEXT_DIM, false);
        }

        Theme.drawFooterLine(ctx, width, height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String describe(CPEvent event) {
        return switch (event) {
            case CPEvent.ContainerEvent c ->
                    "§7[cont] §f" + c.amount() + " §7@ (" + c.x() + "," + c.y() + "," + c.z() + ") " + c.world();
            case CPEvent.MessageEvent m -> "§7[msg]  §f" + m.message();
            case CPEvent.SimpleEvent s -> "§7[evt]  §f→ " + s.target();
            case CPEvent.BlockEvent b ->
                    "§7[block] §f" + (b.added() ? "+" : "-") + " @ (" + b.x() + "," + b.y() + "," + b.z() + ")";
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        int max = Math.max(0, events.size() - ROWS_VISIBLE);
        scrollOffset = (int) Math.max(0, Math.min(max, scrollOffset - v));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}