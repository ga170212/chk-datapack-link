package com.ga170212.chk_datapack_link.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("chk-datapack-link/ModConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("chk_datapack_link.json").toFile();

    private String channelId = "";
    private boolean autoConnect = false;

    private static ModConfig INSTANCE;

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public String getChannelId() {
        return channelId == null ? "" : channelId.trim();
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId == null ? "" : channelId.trim();
        save();
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
        save();
    }

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    INSTANCE = config;
                    return config;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load mod config", e);
            }
        }
        ModConfig newConfig = new ModConfig();
        newConfig.save();
        INSTANCE = newConfig;
        return newConfig;
    }

    public void save() {
        try {
            File dir = CONFIG_FILE.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save mod config", e);
        }
    }
}
