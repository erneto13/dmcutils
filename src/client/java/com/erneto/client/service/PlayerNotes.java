package com.erneto.client.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlayerNotes {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dmc_notes.json");
    private static final Map<String, List<String>> NOTES = load();

    private PlayerNotes() {
    }

    private static Map<String, List<String>> load() {
        if (!Files.exists(FILE)) return new HashMap<>();
        try (FileReader reader = new FileReader(FILE.toFile())) {
            Map<String, List<String>> data = GSON.fromJson(reader, new TypeToken<Map<String, List<String>>>() {
            }.getType());
            return data != null ? data : new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private static void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            GSON.toJson(NOTES, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void add(String user, String text) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + text;
        NOTES.computeIfAbsent(user.toLowerCase(), k -> new ArrayList<>()).add(entry);
        save();
    }

    public static List<String> get(String user) {
        return NOTES.getOrDefault(user.toLowerCase(), List.of());
    }

    public static boolean has(String user) {
        return !get(user).isEmpty();
    }

    public static void clear(String user) {
        NOTES.remove(user.toLowerCase());
        save();
    }
}
