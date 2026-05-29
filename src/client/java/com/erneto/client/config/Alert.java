package com.erneto.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Alert {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private List<String> alertWords = new ArrayList<>();
    private boolean autoTpEnabled = true;
    private boolean hudEnabled = true;
    private final File configFile;
    private final Path logFile;

    public Alert() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.configFile = configDir.resolve("dmc_alerts.json").toFile();
        this.logFile = configDir.resolve("dmc_sanciones.log");
        load();
    }

    public void load() {
        if (!configFile.exists()) {
            alertWords.add("staff");
            alertWords.add("hacker");
            save();
            return;
        }
        try (Reader reader = new FileReader(configFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            if (json != null) {
                if (json.has("words")) {
                    alertWords = GSON.fromJson(json.get("words"), new TypeToken<List<String>>() {}.getType());
                }
                if (json.has("autoTp")) {
                    autoTpEnabled = json.get("autoTp").getAsBoolean();
                }
                if (json.has("hudEnabled")) {
                    hudEnabled = json.get("hudEnabled").getAsBoolean();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(configFile)) {
            JsonObject json = new JsonObject();
            json.add("words", GSON.toJsonTree(alertWords));
            json.addProperty("autoTp", autoTpEnabled);
            json.addProperty("hudEnabled", hudEnabled);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoTpEnabled() { return autoTpEnabled; }
    public void setAutoTpEnabled(boolean state) { this.autoTpEnabled = state; }

    public boolean isHudEnabled() { return hudEnabled; }
    public void setHudEnabled(boolean state) { this.hudEnabled = state; }

    public List<String> getAlertWords() { return alertWords; }

    public Path getLogFile() { return logFile; }
}
