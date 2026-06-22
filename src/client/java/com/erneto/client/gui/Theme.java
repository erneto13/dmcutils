package com.erneto.client.gui;

public final class Theme {

    private Theme() {}

    public static final int BG        = 0xFF0A0C10;
    public static final int PANEL     = 0xFF12161C;
    public static final int PANEL_ALT = 0xFF1A1F28;
    public static final int ROW_EVEN  = 0xFF0A0C10;
    public static final int ROW_ODD   = 0xFF12161C;
    public static final int ROW_HOVER = 0xFF1E2530;

    public static final int BORDER    = 0xFF21262D;
    public static final int BORDER_LT = 0xFF30363D;

    public static final int ACCENT    = 0xFF22D3EE;
    public static final int ACCENT_DIM = 0xFF0E7490;

    public static final int SUCCESS   = 0xFF4ADE80;
    public static final int DANGER    = 0xFFF87171;
    public static final int WARNING   = 0xFFFBBF24;
    public static final int INFO      = 0xFF818CF8;

    public static final int TEXT_PRI  = 0xFFE2E8F0;
    public static final int TEXT_SEC  = 0xFF7D8590;
    public static final int TEXT_DIM  = 0xFF4B5563;

    public static final int HEADER_H  = 26;

    public static final int ROW_H     = 20;

    public static void drawHeader(net.minecraft.client.gui.DrawContext ctx,
                                  net.minecraft.client.font.TextRenderer tr,
                                  int width, String title, String badge, int badgeColor) {
        ctx.fill(0, 0, width, HEADER_H, PANEL);
        ctx.fill(0, HEADER_H - 1, width, HEADER_H, BORDER);

        // Left accent bar
        ctx.fill(0, 0, 3, HEADER_H, ACCENT);

        ctx.drawText(tr, title, 10, 9, TEXT_PRI, false);

        if (badge != null && !badge.isEmpty()) {
            int bw = tr.getWidth(badge) + 8;
            int bx = width - bw - 6;
            ctx.fill(bx - 2, 5, bx + bw - 2, HEADER_H - 5, ACCENT_DIM);
            ctx.drawText(tr, badge, bx + 2, 9, badgeColor, false);
        }
    }

    public static void drawFooterLine(net.minecraft.client.gui.DrawContext ctx, int width, int height) {
        ctx.fill(0, height - 30, width, height - 29, BORDER);
    }
}