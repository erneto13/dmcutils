package com.erneto.client.service;

import com.erneto.client.core.CommandShortcut;
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
import java.util.Optional;

public final class ShortcutStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("dmc_shortcuts.json");
    private static final List<CommandShortcut> SHORTCUTS = load();

    private ShortcutStore() {
    }

    private static List<CommandShortcut> load() {
        if (!Files.exists(FILE)) return defaults();
        try (FileReader reader = new FileReader(FILE.toFile())) {
            List<CommandShortcut> data = GSON.fromJson(reader, new TypeToken<List<CommandShortcut>>() {
            }.getType());
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<CommandShortcut> defaults() {
        List<CommandShortcut> list = new ArrayList<>();
        list.add(new CommandShortcut("d", "dupeip"));
        save(list);
        return list;
    }

    private static void save() {
        save(SHORTCUTS);
    }

    private static void save(List<CommandShortcut> list) {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            GSON.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<CommandShortcut> all() {
        return List.copyOf(SHORTCUTS);
    }

    public static Optional<CommandShortcut> findByAlias(String alias) {
        return SHORTCUTS.stream()
                .filter(s -> s.alias().equalsIgnoreCase(alias))
                .findFirst();
    }

    public static boolean add(String alias, String command) {
        String cleanAlias = alias.trim().toLowerCase();
        String cleanCommand = command.trim();
        if (cleanAlias.isEmpty() || cleanCommand.isEmpty()) return false;
        if (findByAlias(cleanAlias).isPresent()) return false;

        SHORTCUTS.add(new CommandShortcut(cleanAlias, cleanCommand));
        save();
        return true;
    }

    public static void remove(String alias) {
        SHORTCUTS.removeIf(s -> s.alias().equalsIgnoreCase(alias));
        save();
    }

    public static void clear() {
        SHORTCUTS.clear();
        save();
    }
}