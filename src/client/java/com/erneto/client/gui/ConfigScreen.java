package com.erneto.client.gui;

import com.erneto.client.config.Alert;
import com.erneto.client.service.PLogger;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final Alert config;
    private PLogger logger;

    private ButtonWidget hudToggleBtn;
    private ButtonWidget autoTpToggleBtn;
    private ButtonWidget testConnBtn;
    private TextFieldWidget supabaseUrlField;
    private TextFieldWidget supabaseKeyField;

    private String statusMessage = "";

    public ConfigScreen(Screen parent, Alert config) {
        super(Text.literal("DMCUtils — Config"));
        this.parent = parent;
        this.config = config;
    }

    public void setLogger(PLogger logger) {
        this.logger = logger;
    }

    @Override
    protected void init() {
        int cx = width / 2;

        hudToggleBtn = ButtonWidget.builder(hudLabel(), b -> {
            config.setHudEnabled(!config.isHudEnabled());
            config.save();
            hudToggleBtn.setMessage(hudLabel());
        }).dimensions(cx - 100, 40, 200, 20).build();

        autoTpToggleBtn = ButtonWidget.builder(autoTpLabel(), b -> {
            config.setAutoTpEnabled(!config.isAutoTpEnabled());
            config.save();
            autoTpToggleBtn.setMessage(autoTpLabel());
        }).dimensions(cx - 100, 66, 200, 20).build();

        supabaseUrlField = new TextFieldWidget(textRenderer, cx - 100, 100, 200, 18, Text.literal("Supabase URL"));
        supabaseUrlField.setText(config.getSupabaseUrl());
        supabaseUrlField.setChangedListener(v -> {
            config.setSupabaseUrl(v);
            config.save();
        });

        supabaseKeyField = new TextFieldWidget(textRenderer, cx - 100, 124, 200, 18, Text.literal("Supabase Key"));
        supabaseKeyField.setText(config.getSupabaseKey());
        supabaseKeyField.setChangedListener(v -> {
            config.setSupabaseKey(v);
            config.save();
        });

        testConnBtn = ButtonWidget.builder(Text.literal("Probar conexión"), b -> testConnection())
                .dimensions(cx - 100, 150, 200, 20).build();

        ButtonWidget closeBtn = ButtonWidget.builder(Text.literal("Cerrar"), b -> close())
                .dimensions(cx - 100, height - 30, 200, 20).build();

        addDrawableChild(hudToggleBtn);
        addDrawableChild(autoTpToggleBtn);
        addDrawableChild(supabaseUrlField);
        addDrawableChild(supabaseKeyField);
        addDrawableChild(testConnBtn);
        addDrawableChild(closeBtn);
    }

    private void testConnection() {
        if (logger == null) {
            statusMessage = "§cLogger no inicializado";
            return;
        }
        statusMessage = "§7Probando...";
        logger.testConnection(result -> statusMessage = result);
    }

    private Text hudLabel() {
        return Text.literal("HUD: " + (config.isHudEnabled() ? "§aON" : "§cOFF"));
    }

    private Text autoTpLabel() {
        return Text.literal("Auto-TP: " + (config.isAutoTpEnabled() ? "§aON" : "§cOFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG);
        Theme.drawHeader(ctx, textRenderer, width, "Configuración", "", Theme.ACCENT);

        if (!statusMessage.isEmpty()) {
            int msgW = textRenderer.getWidth(statusMessage);
            ctx.drawText(textRenderer, statusMessage, (width - msgW) / 2, 178, Theme.TEXT_SEC, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
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