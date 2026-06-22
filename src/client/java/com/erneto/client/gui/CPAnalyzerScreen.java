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

    private final CPCapture capture;
    private final Screen parent;

    private record Suspect(String user, int totalPicks, List<CPEntry> entries) {}
    private List<Suspect> suspects = List.of();

    private ButtonWidget recordBtn;
    private ButtonWidget stopBtn;
    private ButtonWidget autoBtn;
    private ButtonWidget pagesMinusBtn;
    private ButtonWidget pagesPlusBtn;
    private final ButtonWidget[] tpButtons;
    private final ButtonWidget[] histButtons;

    private boolean autoRecord = false;

    private int scrollOffset = 0;

    private static final int ROWS_VISIBLE = 11;
    private static final int TABLE_TOP    = 54;
    private static final int COL_RANK     = 8;
    private static final int COL_NAME     = 30;
    private static final int COL_PICKS    = 140;
    private static final int COL_COORD    = 195;

    public CPAnalyzerScreen(Screen parent, CPCapture capture) {
        super(Text.literal("CP X-Ray Analyzer"));
        this.parent = parent;
        this.capture = capture;
        this.tpButtons  = new ButtonWidget[ROWS_VISIBLE];
        this.histButtons = new ButtonWidget[ROWS_VISIBLE];

        if (!capture.getEntries().isEmpty()) {
            buildSuspects();
        }
    }

    public boolean isAutoRecord() { return autoRecord; }

    public void triggerAutoRecord() {
        if (autoRecord && !capture.isRecording()) {
            capture.startRecording();
            updateButtonStates();
            suspects = List.of();
            scrollOffset = 0;
        }
    }

    @Override
    protected void init() {
        int bh   = 18;
        int by   = height - 26;
        int midY = by;

        recordBtn = ButtonWidget.builder(Text.literal("▶ Grabar"), b -> startRecording())
                .dimensions(8, midY, 74, bh).build();

        stopBtn = ButtonWidget.builder(Text.literal("■ Detener"), b -> stopRecording())
                .dimensions(86, midY, 74, bh).build();

        autoBtn = ButtonWidget.builder(buildAutoLabel(), b -> toggleAuto())
                .dimensions(164, midY, 80, bh).build();

        int pagesX = 252;
        pagesMinusBtn = ButtonWidget.builder(Text.literal("−"), b -> adjustPages(-1))
                .dimensions(pagesX, midY, 18, bh).build();

        pagesPlusBtn = ButtonWidget.builder(Text.literal("+"), b -> adjustPages(1))
                .dimensions(pagesX + 56, midY, 18, bh).build();

        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(width - 64, midY, 58, bh).build();

        addDrawableChild(recordBtn);
        addDrawableChild(stopBtn);
        addDrawableChild(autoBtn);
        addDrawableChild(pagesMinusBtn);
        addDrawableChild(pagesPlusBtn);
        addDrawableChild(closeBtn);

        int rowsTop  = TABLE_TOP + 18;
        int actionCol = width - 76;

        for (int i = 0; i < ROWS_VISIBLE; i++) {
            final int ri = i;
            int ry = rowsTop + i * Theme.ROW_H + 2;

            tpButtons[i] = ButtonWidget.builder(Text.literal("TP"),
                            b -> handleRowAction(ri, true))
                    .dimensions(actionCol, ry, 28, 15).build();

            histButtons[i] = ButtonWidget.builder(Text.literal("Hist"),
                            b -> handleRowAction(ri, false))
                    .dimensions(actionCol + 30, ry, 36, 15).build();

            addDrawableChild(tpButtons[i]);
            addDrawableChild(histButtons[i]);
        }

        updateButtonStates();
    }

    private void startRecording() {
        capture.startRecording();
        updateButtonStates();
        suspects = List.of();
        scrollOffset = 0;
    }

    private void stopRecording() {
        capture.stopRecording();
        buildSuspects();
        updateButtonStates();
    }

    private void toggleAuto() {
        autoRecord = !autoRecord;
        autoBtn.setMessage(buildAutoLabel());
    }

    private void adjustPages(int delta) {
        int current = capture.getTargetPages();
        int next = Math.max(1, Math.min(100, current + delta));
        capture.setTargetPages(next);
    }

    private Text buildAutoLabel() {
        return autoRecord
                ? Text.literal("Auto §a●")
                : Text.literal("Auto §c○");
    }

    private void handleRowAction(int rowIndex, boolean isTp) {
        int idx = scrollOffset + rowIndex;
        if (idx < 0 || idx >= suspects.size() || client == null || client.player == null) return;

        Suspect s = suspects.get(idx);

        if (isTp) {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatCommand("tp " + s.user());
            }
            client.inGameHud.setOverlayMessage(
                    Text.literal("§b→ §fTP a §e" + s.user()), false);
            close();
        } else {
            client.keyboard.setClipboard("history " + s.user());
            client.inGameHud.setOverlayMessage(
                    Text.literal("§7Copiado: §fhistory " + s.user()), false);
        }
    }

    private void updateButtonStates() {
        boolean rec = capture.isRecording();
        recordBtn.active = !rec;
        stopBtn.active   = rec;
        autoBtn.active   = !rec;
        pagesMinusBtn.active = !rec;
        pagesPlusBtn.active  = !rec;
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
        ctx.fill(0, 0, width, height, Theme.BG);

        String badge = capture.isRecording()
                ? "● REC"
                : suspects.isEmpty() ? "Sin datos" : suspects.size() + " sospechosos";
        int badgeColor = capture.isRecording() ? Theme.DANGER
                : suspects.isEmpty() ? Theme.TEXT_SEC : Theme.SUCCESS;
        Theme.drawHeader(ctx, textRenderer, width, "CP X-Ray Analyzer", badge, badgeColor);

        ctx.fill(0, Theme.HEADER_H, width, Theme.HEADER_H + 16, Theme.PANEL_ALT);
        ctx.fill(0, Theme.HEADER_H + 15, width, Theme.HEADER_H + 16, Theme.BORDER);

        int negCount = suspects.stream().mapToInt(Suspect::totalPicks).sum();
        String stats = "Entradas: §f" + capture.getEntries().size()
                + " §8│ §7Neg Y: §f" + negCount
                + " §8│ §7Páginas: §f" + capture.getCurrentPage() + "/" + capture.getTargetPages();
        ctx.drawText(textRenderer, stats, 10, Theme.HEADER_H + 4, Theme.TEXT_SEC, false);

        String pagesLabel = "p. " + capture.getTargetPages();
        int plW = textRenderer.getWidth(pagesLabel);
        ctx.drawText(textRenderer, pagesLabel,
                252 + 19 + (35 - plW) / 2, height - 26 + 5,
                Theme.TEXT_PRI, false);

        drawTableHeader(ctx);

        int rowsTop = TABLE_TOP + 18;

        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int idx = i + scrollOffset;
            boolean valid = idx < suspects.size();

            tpButtons[i].visible  = valid;
            histButtons[i].visible = valid;

            if (!valid) continue;

            int ry = rowsTop + i * Theme.ROW_H;
            ctx.fill(0, ry, width, ry + Theme.ROW_H,
                    i % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);

            Suspect s = suspects.get(idx);

            int rankColor = idx == 0 ? Theme.DANGER : idx == 1 ? Theme.WARNING : Theme.TEXT_DIM;
            ctx.drawText(textRenderer, "#" + (idx + 1), COL_RANK, ry + 6, rankColor, false);

            int nameColor = idx == 0 ? Theme.DANGER : idx == 1 ? Theme.WARNING : Theme.TEXT_PRI;
            ctx.drawText(textRenderer, s.user(), COL_NAME, ry + 6, nameColor, false);

            ctx.drawText(textRenderer, String.valueOf(s.totalPicks()), COL_PICKS, ry + 6, Theme.ACCENT, false);

            if (!s.entries().isEmpty()) {
                CPEntry last = s.entries().get(s.entries().size() - 1);
                String coord = last.x() + "," + last.y() + "," + last.z();
                ctx.drawText(textRenderer, coord, COL_COORD, ry + 6, Theme.TEXT_SEC, false);
            }

            ctx.fill(0, ry + Theme.ROW_H - 1, width, ry + Theme.ROW_H, Theme.BORDER);
        }

        if (suspects.isEmpty() && !capture.isRecording()) {
            String msg = capture.getEntries().isEmpty()
                    ? "Grabar → ejecuta /co l a:+item include:spawner en el chat"
                    : "Sin coordenadas negativas encontradas";
            int msgW = textRenderer.getWidth(msg);
            ctx.drawText(textRenderer, msg, (width - msgW) / 2,
                    rowsTop + (ROWS_VISIBLE * Theme.ROW_H) / 2, Theme.TEXT_DIM, false);
        }

        if (suspects.size() > ROWS_VISIBLE) {
            int sbH   = ROWS_VISIBLE * Theme.ROW_H;
            int sbX   = width - 4;
            int sbY   = rowsTop;
            ctx.fill(sbX, sbY, sbX + 3, sbY + sbH, Theme.BORDER);
            int thumbH = Math.max(16, sbH * ROWS_VISIBLE / suspects.size());
            int thumbY = sbY + (sbH - thumbH) * scrollOffset
                    / Math.max(1, suspects.size() - ROWS_VISIBLE);
            ctx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, Theme.TEXT_SEC);
        }

        if (capture.isRecording()) {
            int barW = width - 20;
            int barY = height - 44;
            ctx.fill(10, barY, 10 + barW, barY + 5, Theme.BORDER);
            int prog = capture.getTargetPages() > 0
                    ? barW * capture.getCurrentPage() / capture.getTargetPages() : 0;
            ctx.fill(10, barY, 10 + prog, barY + 5, Theme.ACCENT);
            ctx.drawText(textRenderer,
                    "Grabando  " + capture.getCurrentPage() + " / " + capture.getTargetPages(),
                    10, barY - 10, Theme.WARNING, false);
        }

        Theme.drawFooterLine(ctx, width, height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTableHeader(DrawContext ctx) {
        int actionCol = width - 76;
        ctx.fill(0, TABLE_TOP, width, TABLE_TOP + 18, Theme.PANEL);
        ctx.fill(0, TABLE_TOP + 17, width, TABLE_TOP + 18, Theme.BORDER);
        ctx.drawText(textRenderer, "#",          COL_RANK,  TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Usuario",    COL_NAME,  TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Picks",      COL_PICKS, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Última pos", COL_COORD, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Acción",     actionCol, TABLE_TOP + 5, Theme.TEXT_SEC, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, suspects.size() - ROWS_VISIBLE);
        scrollOffset = (int) Math.max(0, Math.min(max, scrollOffset - verticalAmount));
        return true;
    }

    @Override
    public boolean shouldPause() { return false; }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }
}