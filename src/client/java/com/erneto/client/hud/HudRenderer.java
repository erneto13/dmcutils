package com.erneto.client.hud;

import com.erneto.client.config.Alert;
import com.erneto.client.gui.Theme;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {

    private final Alert config;
    private String lastSanction = null;

    public HudRenderer(Alert config) {
        this.config = config;
    }

    public void setLastSanction(String text) {
        this.lastSanction = text;
    }

    public void register() {
        HudRenderCallback.EVENT.register(this::render);
    }

    private void render(DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (!config.isHudEnabled() || lastSanction == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = "§8[dmc] §f" + lastSanction;
        int textW = mc.textRenderer.getWidth(text);
        int x = 6;
        int y = 6;

        ctx.fill(x - 3, y - 2, x + textW + 3, y + 10, Theme.PANEL);
        ctx.drawText(mc.textRenderer, text, x, y, Theme.TEXT_PRI, false);
    }
}