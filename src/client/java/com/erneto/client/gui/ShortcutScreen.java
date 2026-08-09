package com.erneto.client.gui;

import com.erneto.client.core.CommandShortcut;
import com.erneto.client.service.ShortcutStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ShortcutScreen extends Screen {

    private static final int ROWS_VISIBLE = 9;
    private static final int TABLE_TOP    = 70;
    private static final int COL_ALIAS    = 8;
    private static final int COL_ARROW    = 60;
    private static final int COL_COMMAND  = 80;

    private final Screen parent;

    private TextFieldWidget aliasField;
    private TextFieldWidget commandField;
    private final ButtonWidget[] removeButtons = new ButtonWidget[ROWS_VISIBLE];

    private List<CommandShortcut> shortcuts = List.of();
    private String statusMessage = "";
    private int scrollOffset = 0;

    public ShortcutScreen(Screen parent) {
        super(Text.literal("DMCUtils — Shortcuts"));
        this.parent = parent;
        this.shortcuts = ShortcutStore.all();
    }

    @Override
    protected void init() {
        int cx = width / 2;

        aliasField = new TextFieldWidget(textRenderer, 8, 40, 70, 18, Text.literal("Alias"));
        aliasField.setMaxLength(16);
        addDrawableChild(aliasField);

        commandField = new TextFieldWidget(textRenderer, 86, 40, 156, 18, Text.literal("Comando destino"));
        commandField.setMaxLength(64);
        addDrawableChild(commandField);

        addDrawableChild(ButtonWidget.builder(Text.literal("+ Agregar"), b -> addShortcut())
                .dimensions(250, 40, 66, 18).build());

        int rowsTop = TABLE_TOP + 18;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            final int ri = i;
            int ry = rowsTop + i * Theme.ROW_H + 2;
            removeButtons[i] = ButtonWidget.builder(Text.literal("x"), b -> removeRow(ri))
                    .dimensions(width - 26, ry, 18, 15).build();
            addDrawableChild(removeButtons[i]);
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(cx - 64, height - 26, 128, 18).build());
    }

    private void addShortcut() {
        String alias = aliasField.getText().trim().toLowerCase();
        String command = commandField.getText().trim();

        if (alias.isEmpty() || command.isEmpty()) {
            statusMessage = "§cCompleta alias y comando";
            return;
        }
        if (alias.contains(" ") || command.startsWith("/")) {
            statusMessage = "§cAlias sin espacios, comando sin '/'";
            return;
        }

        boolean added = ShortcutStore.add(alias, command);
        if (!added) {
            statusMessage = "§cEl alias '" + alias + "' ya existe";
            return;
        }

        aliasField.setText("");
        commandField.setText("");
        statusMessage = "§aAgregado: §f/" + alias + " §7→ §f/" + command;
        refresh();
    }

    private void removeRow(int rowIndex) {
        int idx = scrollOffset + rowIndex;
        if (idx < 0 || idx >= shortcuts.size()) return;

        ShortcutStore.remove(shortcuts.get(idx).alias());
        refresh();
    }

    private void refresh() {
        shortcuts = ShortcutStore.all();
        int max = Math.max(0, shortcuts.size() - ROWS_VISIBLE);
        scrollOffset = Math.min(scrollOffset, max);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG);
        Theme.drawHeader(ctx, textRenderer, width, "Shortcuts", shortcuts.size() + " atajos", Theme.ACCENT);

        if (!statusMessage.isEmpty()) {
            ctx.drawText(textRenderer, statusMessage, 8, 26, Theme.TEXT_SEC, false);
        }

        drawTableHeader(ctx);

        int rowsTop = TABLE_TOP + 18;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int idx = i + scrollOffset;
            boolean valid = idx < shortcuts.size();
            removeButtons[i].visible = valid;
            if (!valid) continue;

            int ry = rowsTop + i * Theme.ROW_H;
            ctx.fill(0, ry, width, ry + Theme.ROW_H, i % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);

            CommandShortcut s = shortcuts.get(idx);
            ctx.drawText(textRenderer, "/" + s.alias(), COL_ALIAS, ry + 6, Theme.ACCENT, false);
            ctx.drawText(textRenderer, "→", COL_ARROW, ry + 6, Theme.TEXT_DIM, false);
            ctx.drawText(textRenderer, "/" + s.command(), COL_COMMAND, ry + 6, Theme.TEXT_PRI, false);

            ctx.fill(0, ry + Theme.ROW_H - 1, width, ry + Theme.ROW_H, Theme.BORDER);
        }

        if (shortcuts.isEmpty()) {
            String msg = "Sin atajos configurados";
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
        ctx.drawText(textRenderer, "Alias", COL_ALIAS, TABLE_TOP + 5, Theme.TEXT_SEC, false);
        ctx.drawText(textRenderer, "Comando", COL_COMMAND, TABLE_TOP + 5, Theme.TEXT_SEC, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, shortcuts.size() - ROWS_VISIBLE);
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