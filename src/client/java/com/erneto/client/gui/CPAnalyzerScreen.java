package com.erneto.client.gui;

import com.erneto.client.core.CPCapture;
import com.erneto.client.core.CPEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class CPAnalyzerScreen extends Screen {

    private static final int BG        = 0xFF0D1117;
    private static final int PANEL     = 0xFF161B22;
    private static final int BORDER    = 0xFF30363D;
    private static final int ACCENT    = 0xFF238636;
    private static final int RED       = 0xFFDA3633;
    private static final int TEXT_PRI  = 0xFFE6EDF3;
    private static final int TEXT_SEC  = 0xFF7D8590;
    private static final int TEXT_WARN = 0xFFD29922;
    private static final int ROW_EVEN  = 0xFF0D1117;
    private static final int ROW_ODD   = 0xFF161B22;

    private final CPCapture capture;
    private final Screen parent;

    private record Suspect(String user, int totalPicks, List<CPEntry> entries) {}
    private List<Suspect> suspects = List.of();

    private ButtonWidget recordBtn;
    private ButtonWidget stopBtn;
    private final ButtonWidget[] tpButtons;
    private final ButtonWidget[] histButtons;

    private int scrollOffset = 0;
    private static final int ROW_H = 20;
    private static final int ROWS_VISIBLE = 12;

    private static final int TABLE_TOP = 50;
    private static final int COL1 = 10;
    private static final int COL2 = 130;
    private static final int COL3 = 210;

    public CPAnalyzerScreen(Screen parent, CPCapture capture) {
        super(Text.literal("CP X-Ray Analyzer"));
        this.parent = parent;
        this.capture = capture;
        this.tpButtons = new ButtonWidget[ROWS_VISIBLE];
        this.histButtons = new ButtonWidget[ROWS_VISIBLE];

        if (!capture.getEntries().isEmpty()) {
            buildSuspects();
        }
    }

    @Override
    protected void init() {
        int bw = 90;
        int bh = 18;
        int by = height - 26;

        recordBtn = ButtonWidget.builder(
                Text.literal("● Grabar"),
                b -> startRecording()
        ).dimensions(10, by, bw, bh).build();

        stopBtn = ButtonWidget.builder(
                Text.literal("■ Detener"),
                b -> stopRecording()
        ).dimensions(106, by, bw, bh).build();

        ButtonWidget closeBtn = ButtonWidget.builder(
                Text.literal("Cerrar"),
                b -> close()
        ).dimensions(width - 66, by, 60, bh).build();

        addDrawableChild(recordBtn);
        addDrawableChild(stopBtn);
        addDrawableChild(closeBtn);

        int rowsTop = TABLE_TOP + 18;
        int actionCol = width - 80;

        for (int i = 0; i < ROWS_VISIBLE; i++) {
            final int rowIndex = i;
            int ry = rowsTop + i * ROW_H + 2;

            tpButtons[i] = ButtonWidget.builder(Text.literal("TP"), b -> handleRowAction(rowIndex, true))
                    .dimensions(actionCol, ry, 30, 16).build();

            histButtons[i] = ButtonWidget.builder(Text.literal("Hist"), b -> handleRowAction(rowIndex, false))
                    .dimensions(actionCol + 32, ry, 38, 16).build();

            addDrawableChild(tpButtons[i]);
            addDrawableChild(histButtons[i]);
        }

        updateButtonStates();
    }

    private void handleRowAction(int rowIndex, boolean isTp) {
        int idx = scrollOffset + rowIndex;
        if (idx >= 0 && idx < suspects.size() && client != null && client.player != null) {
            Suspect s = suspects.get(idx);

            if (isTp) {
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().sendChatCommand("tp " + s.user());
                }
                client.inGameHud.setOverlayMessage(
                        Text.literal("§9→ §fTeleportando a §e" + s.user()), false);
                close();
            } else {
                client.keyboard.setClipboard("history " + s.user());
                client.inGameHud.setOverlayMessage(
                        Text.literal("§7Copiado: §fhistory " + s.user()), false);
            }
        }
    }

    private void startRecording() {
        capture.startRecording(10);
        updateButtonStates();
        suspects = List.of();
        scrollOffset = 0;
    }

    private void stopRecording() {
        capture.stopRecording();
        buildSuspects();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean rec = capture.isRecording();
        recordBtn.active = !rec;
        stopBtn.active = rec;
    }

    private void buildSuspects() {
        Map<String, List<CPEntry>> grouped = capture.getEntries().stream()
                .filter(CPEntry::isNegativeCoord)
                .collect(Collectors.groupingBy(CPEntry::user));

        suspects = grouped.entrySet().stream()
                .map(e -> new Suspect(e.getKey(), e.getValue().size(), e.getValue()))
                .sorted(Comparator.comparingInt(Suspect::totalPicks).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BG);

        ctx.fill(0, 0, width, 28, PANEL);
        ctx.fill(0, 27, width, 28, BORDER);
        ctx.drawText(textRenderer, "CP X-Ray Analyzer", 10, 9, TEXT_PRI, false);
        drawStatusPill(ctx);

        String captured = "Entradas capturadas: " + capture.getEntries().size()
                + "  |  Negativas: " + suspects.stream().mapToInt(Suspect::totalPicks).sum();
        ctx.drawText(textRenderer, captured, 10, 32, TEXT_SEC, false);

        ctx.fill(0, TABLE_TOP, width, TABLE_TOP + 18, PANEL);
        ctx.fill(0, TABLE_TOP + 17, width, TABLE_TOP + 18, BORDER);

        int actionCol = width - 80;
        ctx.drawText(textRenderer, "Usuario",      COL1,      TABLE_TOP + 5, TEXT_SEC, false);
        ctx.drawText(textRenderer, "Picks (neg)",  COL2,      TABLE_TOP + 5, TEXT_SEC, false);
        ctx.drawText(textRenderer, "Última coord", COL3,      TABLE_TOP + 5, TEXT_SEC, false);
        ctx.drawText(textRenderer, "Acción",       actionCol, TABLE_TOP + 5, TEXT_SEC, false);

        int rowsTop = TABLE_TOP + 18;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int idx = i + scrollOffset;
            boolean valid = idx < suspects.size();

            tpButtons[i].visible = valid;
            histButtons[i].visible = valid;

            if (!valid) continue;

            int ry = rowsTop + i * ROW_H;
            int bg = (i % 2 == 0) ? ROW_EVEN : ROW_ODD;
            ctx.fill(0, ry, width, ry + ROW_H, bg);

            Suspect s = suspects.get(idx);

            ctx.drawText(textRenderer, "#" + (idx + 1), COL1, ry + 6,
                    idx == 0 ? TEXT_WARN : TEXT_SEC, false);

            int nameColor = idx == 0 ? RED : idx == 1 ? TEXT_WARN : TEXT_PRI;
            ctx.drawText(textRenderer, s.user(), COL1 + 18, ry + 6, nameColor, false);

            ctx.drawText(textRenderer, String.valueOf(s.totalPicks()), COL2, ry + 6, ACCENT, false);

            if (!s.entries().isEmpty()) {
                CPEntry last = s.entries().get(s.entries().size() - 1);
                ctx.drawText(textRenderer,
                        last.x() + "," + last.y() + "," + last.z(),
                        COL3, ry + 6, TEXT_SEC, false);
            }

            ctx.fill(0, ry + ROW_H - 1, width, ry + ROW_H, BORDER);
        }

        if (suspects.isEmpty() && !capture.isRecording()) {
            String msg = capture.getEntries().isEmpty()
                    ? "Presiona Grabar, luego navega /co l a:+item include:spawner"
                    : "Sin coordenadas negativas registradas";
            ctx.drawText(textRenderer, msg, (width - textRenderer.getWidth(msg)) / 2,
                    height / 2, TEXT_SEC, false);
        }

        if (suspects.size() > ROWS_VISIBLE) {
            int sbX  = width - 4;
            int sbH  = ROWS_VISIBLE * ROW_H;
            int sbY  = rowsTop;
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, BORDER);
            int thumbH = Math.max(20, sbH * ROWS_VISIBLE / suspects.size());
            int thumbY = sbY + (sbH - thumbH) * scrollOffset
                    / Math.max(1, suspects.size() - ROWS_VISIBLE);
            ctx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, TEXT_SEC);
        }

        ctx.fill(0, height - 30, width, height - 29, BORDER);

        if (capture.isRecording()) {
            int prog  = capture.getCurrentPage();
            int total = capture.getTargetPages();
            int barW  = width - 20;
            int barY  = height - 44;
            ctx.fill(10, barY, 10 + barW, barY + 6, BORDER);
            int fill = total > 0 ? barW * prog / total : 0;
            ctx.fill(10, barY, 10 + fill, barY + 6, ACCENT);
            ctx.drawText(textRenderer, "Grabando... página " + prog + "/" + total,
                    10, barY - 10, TEXT_WARN, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawStatusPill(DrawContext ctx) {
        String label;
        int color;
        if (capture.isRecording()) {
            label = "● REC";
            color = RED;
        } else if (!suspects.isEmpty()) {
            label = suspects.size() + " sospechosos";
            color = ACCENT;
        } else {
            label = "Listo";
            color = TEXT_SEC;
        }
        ctx.drawText(textRenderer, label, width - textRenderer.getWidth(label) - 14, 9, color, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, suspects.size() - ROWS_VISIBLE);
        scrollOffset = (int) Math.max(0, Math.min(max, scrollOffset - verticalAmount));
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