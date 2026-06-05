package com.erneto.client.service;

import com.erneto.client.config.Alert;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PLogger {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dmcutils-logger");
        t.setDaemon(true);
        return t;
    });

    private final Path logFile;
    private final Alert config;

    public PLogger(Path logFile, Alert config) {
        this.logFile = logFile;
        this.config = config;
    }

    public void log(PHandler.PData data) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        EXECUTOR.submit(() -> {
            writeLocal(data);
            if (config.hasSupabaseCredentials()) {
                postRemote(data, client, config.getSupabaseUrl(), config.getSupabaseKey(), null);
            }
        });
    }

    //Runs a test POST and reports result via the callback on the MC thread
    public void testConnection(Consumer<String> onResult) {
        if (!config.hasSupabaseCredentials()) {
            MinecraftClient.getInstance().execute(() -> onResult.accept("§cFaltan credenciales"));
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        EXECUTOR.submit(() -> {
            PHandler.PData dummy = new PHandler.PData("__test__", "test", "erneto13");
            postRemote(dummy, client, config.getSupabaseUrl(), config.getSupabaseKey(), onResult);
        });
    }

    private void writeLocal(PHandler.PData data) {
        String line = String.format("[%s] user=%s type=%s staff=%s%n",
                LocalDateTime.now().format(FMT), data.user(), data.type(), data.staff());
        try {
            Files.writeString(logFile, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postRemote(PHandler.PData data, MinecraftClient client,
                            String url, String key, Consumer<String> callback) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("apikey", key);
            conn.setRequestProperty("Authorization", "Bearer " + key);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
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
            conn.disconnect();

            client.execute(() -> {
                String msg = (code == 201 || code == 200)
                        ? "§a✔ §fRegistrado: §e" + data.user() + " §7(" + data.type() + ")"
                        : "§c✘ §fSupabase error §7(" + code + ")";

                if (callback != null) {
                    callback.accept(msg);
                } else {
                    client.inGameHud.setOverlayMessage(Text.literal(msg), false);
                }
            });

        } catch (Exception e) {
            client.execute(() -> {
                String msg = "§c✘ §fError: " + e.getMessage();
                if (callback != null) {
                    callback.accept(msg);
                } else {
                    client.inGameHud.setOverlayMessage(Text.literal("§c✘ §fError de conexión"), false);
                }
            });
        }
    }
}
