package com.erneto.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Alert {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private List<String> alertWords = new ArrayList<>();
    private boolean autoTpEnabled = true;
    private final File configFile;

    public Alert() {
        this.configFile = FabricLoader.getInstance().getConfigDir().resolve("dmc_alerts.json").toFile();
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
                    alertWords = GSON.fromJson(json.get("words"), new TypeToken<List<String>>() {
                    }.getType());
                }
                if (json.has("autoTp")) {
                    autoTpEnabled = json.get("autoTp").getAsBoolean();
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
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAutoTpEnabled() {
        return autoTpEnabled;
    }

    public void setAutoTpEnabled(boolean state) {
        this.autoTpEnabled = state;
    }

    public List<String> getAlertWords() {
        return alertWords;
    }
}