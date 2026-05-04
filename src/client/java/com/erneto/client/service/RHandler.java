package com.erneto.client.service;

import com.erneto.client.util.ItemHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RHandler {
    public static final Pattern REPORT_PATTERN = Pattern.compile("Usuario reportado:\\s+([a-zA-Z0-9_]{3,16})");

    public static void proccessChatMessage(String message, boolean isEnabled) {
        if (!isEnabled) return;

        String cleanMessage = message.replace("\n", " ").trim();
        Matcher matcher = REPORT_PATTERN.matcher(cleanMessage);

        if (matcher.find()) {
            String targetUser = matcher.group(1);
            executeReport(targetUser);
        }
    }

    private static void executeReport(String user) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemHelper.sendCommand(client, "goto " + user);

        new Thread(() -> {
            try {
                Thread.sleep(500);
                ItemHelper.sendCommand(client, "tp " + user);
            } catch (InterruptedException e) {
            }
        }).start();

        client.inGameHud.setOverlayMessage(
                Text.literal("§c§l(!) §f:Responding to report §e" + user),
                false
        );
    }
}