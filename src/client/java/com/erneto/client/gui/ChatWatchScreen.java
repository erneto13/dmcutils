package com.erneto.client.gui;

import com.erneto.client.config.Alert;
import com.erneto.client.core.ChatViolation;
import com.erneto.client.service.ChatWatchLog;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatWatchScreen extends Screen {

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final int ROWS_VISIBLE = 8;
    private static final int TABLE_TOP = 118;
    private static final int COL_TIME = 8;
    private static final int COL_PLAYER = 60;
    private static final int COL_TYPE = 150;
    private static final int COL_DETAIL = 210;

    private final Screen parent;
    private final Alert config;

    private TextFieldWidget wordField;
    private TextFieldWidget exemptField;
    private List<ChatViolation> log = List.of();
    private int scrollOffset = 0;

    public ChatWatchScreen(Screen parent, Alert config) {
        super(Text.literal("DMCUtils — Chat Watch"));
        this.parent = parent;
        this.config = config;
        this.log = ChatWatchLog.all();
    }

    @Override
    protected void init() {
        int cx = width / 2;

        wordField = new TextFieldWidget(textRenderer, 8, 40, 160, 18, Text.literal("Palabra"));
        addDrawableChild(wordField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Palabra"), b -> addWord())
                .dimensions(172, 40, 70, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("- Última"), b -> removeLastWord())
                .dimensions(246, 40, 70, 18).build());

        int stepY = 64;
        addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> adjustThreshold(-1))
                .dimensions(8, stepY, 18, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> adjustThreshold(1))
                .dimensions(120, stepY, 18, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("−"), b -> adjustWindow(-1))
                .dimensions(160, stepY, 18, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> adjustWindow(1))
                .dimensions(280, stepY, 18, 18).build());

        int exemptY = 88;
        exemptField = new TextFieldWidget(textRenderer, 8, exemptY, 160, 18, Text.literal("Jugador exento"));
        addDrawableChild(exemptField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Exento"), b -> addExempt())
                .dimensions(172, exemptY, 70, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("- Última"), b -> removeLastExempt())
                .dimensions(246, exemptY, 70, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Limpiar registro"), b -> clearLog())
                .dimensions(cx - 130, height - 26, 130, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(cx + 2, height - 26, 128, 18).build());
    }

    private void addWord() {
        String w = wordField.getText().trim().toLowerCase();
        if (w.isEmpty()) return;
        if (!config.getAlertWords().contains(w)) {
            config.getAlertWords().add(w);
            config.save();
        }
        wordField.setText("");
    }

    private void removeLastWord() {
        List<String> words = config.getAlertWords();
        if (!words.isEmpty()) {
            words.remove(words.size() - 1);
            config.save();
        }
    }

    private void addExempt() {
        String p = exemptField.getText().trim();
        if (p.isEmpty()) return;
        if (!config.isExempt(p)) {
            config.getExemptPlayers().add(p);
            config.save();
        }
        exemptField.setText("");
    }

    private void removeLastExempt() {
        List<String> exempt = config.getExemptPlayers();
        if (!exempt.isEmpty()) {
            exempt.remove(exempt.size() - 1);
            config.save();
        }
    }

    private void adjustThreshold(int delta) {
        config.setRepeatThreshold(Math.max(2, Math.min(20, config.getRepeatThreshold() + delta)));
        config.save();
    }

    private void adjustWindow(int delta) {
        config.setRepeatWindowSeconds(Math.max(2, Math.min(120, config.getRepeatWindowSeconds() + delta)));
        config.save();
    }

    private void clearLog() {
        ChatWatchLog.clear();
        log = ChatWatchLog.all();
        scrollOffset = 0;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG);
        Theme.drawHeader(ctx, textRenderer, width, "Chat Watch", log.size() + " eventos", Theme.ACCENT);

        ctx.drawText(textRenderer, "Palabras vigiladas (" + config.getAlertWords().size() + "):",
                8, 28, Theme.TEXT_SEC, false);

        int stepY = 64;
        ctx.drawText(textRenderer, "Repetición: " + config.getRepeatThreshold() + "x",
                30, stepY + 5, Theme.TEXT_PRI, false);
        ctx.drawText(textRenderer, "Ventana: " + config.getRepeatWindowSeconds() + "s",
                182, stepY + 5, Theme.TEXT_PRI, false);

        ctx.drawText(textRenderer, "Exentos (" + config.getExemptPlayers().size() + "): §7"
                        + String.join(", ", config.getExemptPlayers()),
                8, 76, Theme.TEXT_SEC, false);

        drawTableHeader(ctx);

        int rowsTop = TABLE_TOP + 18;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int idx = i + scrollOffset;
            if (idx >= log.size()) break;

            int ry = rowsTop + i * Theme.ROW_H;
            ctx.fill(0, ry, width, ry + Theme.ROW_H, i % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);

            ChatViolation v = log.get(idx);
            int typeColor = v.type().equals("PALABRA") ? Theme.WARNING : Theme.DANGER;

            ctx.drawText(textRenderer, TIME_FMT.format(Instant.ofEpochMilli(v.timestamp())),
                    COL_TIME, ry + 6, Theme.TEXT_SEC, false);
            ctx.drawText(textRenderer, v.player(), COL_PLAYER, ry + 6, Theme.TEXT_PRI, false);
            ctx.drawText(textRenderer, v.type(), COL_TYPE, ry + 6, typeColor, false);
            ctx.drawText(textRenderer, trim(v.detail(), 26), COL_DETAIL, ry + 6, Theme.TEXT_SEC, false);

            ctx.fill(0, ry + Theme.ROW_H - 1, width, ry + Theme.ROW_H, Theme.BORDER);
        }

        if (log.isEmpty()) {
            String msg = "Sin eventos registrados todavía";
            int msgW = textRenderer.getWidth(msg);
            ctx.drawText(textRenderer, msg, (width - msgW) / 2,
                    rowsTop + (ROWS_VISIBLE * Theme.ROW_H) / 2, Theme.TEXT_DIM, false);
        }

        Theme.drawFooterLine(ctx, width, height);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTableHeader(DrawContext ctx) {
        ctx.fill(0, TABLE_TOP, width, TABLE_TOP + 18, Theme.PANEL);
        ctx.fill(0, TABLE_TOP + 17, width, TABLE_TOP + 18, Theme.BORDER);
        ctx.drawText(textRenderer, "Hora", COL_TIME, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Jugador", COL_PLAYER, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Tipo", COL_TYPE, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Detalle", COL_DETAIL, TABLE_TOP + 5, Theme.TEXT_SEC, false);
    }

    private static String trim(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, log.size() - ROWS_VISIBLE);
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