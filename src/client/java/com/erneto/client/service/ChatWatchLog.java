package com.erneto.client.service;

import com.erneto.client.core.ChatViolation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ChatWatchLog {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dmc_chatwatch.json");
    private static final int MAX_ENTRIES = 300;
    private static final List<ChatViolation> ENTRIES = load();

    private ChatWatchLog() {
    }

    private static List<ChatViolation> load() {
        if (!Files.exists(FILE)) return new ArrayList<>();
        try (FileReader reader = new FileReader(FILE.toFile())) {
            List<ChatViolation> data = GSON.fromJson(reader, new TypeToken<List<ChatViolation>>() {
            }.getType());
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            GSON.toJson(ENTRIES, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void record(String player, String type, String detail) {
        ENTRIES.add(0, new ChatViolation(System.currentTimeMillis(), player, type, detail));
        while (ENTRIES.size() > MAX_ENTRIES) ENTRIES.remove(ENTRIES.size() - 1);
        save();
    }

    public static List<ChatViolation> all() {
        return List.copyOf(ENTRIES);
    }

    public static void clear() {
        ENTRIES.clear();
        save();
    }
}