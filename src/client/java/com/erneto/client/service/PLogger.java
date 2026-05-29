package com.erneto.client.service;

import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PLogger {
    private static final String PUBLIC_SUPABASE_URL = "";
    private static final String PUBLIC_SUPABASE_ANON_KEY = "";

    public void log(PHandler.PData data) {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(PUBLIC_SUPABASE_URL).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", PUBLIC_SUPABASE_ANON_KEY);
                conn.setRequestProperty("Authorization", "Bearer " + PUBLIC_SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Prefer", "return=minimal");
                conn.setDoOutput(true);

                JsonObject body = new JsonObject();
                body.addProperty("user", data.user());
                body.addProperty("mode", "survival");
                body.addProperty("type", data.type());
                body.addProperty("reason", "");
                body.addProperty("evidence", "");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code != 201) {
                    client.inGameHud.setOverlayMessage(
                            Text.literal("error de supabase" + code),
                            false
                    );
                }
                client.inGameHud.setOverlayMessage(
                        Text.literal(code + ": sanción registrada"),
                        false
                );
                conn.disconnect();
            } catch (Exception e) {
                client.inGameHud.setOverlayMessage(
                        Text.literal("error al registrar"), false
                );
            }
        }).start();
    }
}
