package net.treechopperplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUpdater {
    private final TreeCapPlugin plugin;

    public ConfigUpdater(TreeCapPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkAndUpdateConfigs() {
        checkAndUpdateItemsConfig();
        checkAndUpdateUpgradesConfig();
    }

    private void checkAndUpdateItemsConfig() {
        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        FileConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);

        if (itemsConfig.getInt("config-version", 0) < getCurrentItemsConfigVersion()) {
            plugin.saveResource("items.yml", true);
        }
    }

    private void checkAndUpdateUpgradesConfig() {
        File upgradesFile = new File(plugin.getDataFolder(), "UpgradesAxe.yml");
        FileConfiguration upgradesConfig = YamlConfiguration.loadConfiguration(upgradesFile);

        if (upgradesConfig.getInt("config-version", 0) < getCurrentUpgradesConfigVersion()) {
            plugin.saveResource("UpgradesAxe.yml", true);
        }
    }

    private int getCurrentItemsConfigVersion() {
        return 1;
    }

    private int getCurrentUpgradesConfigVersion() {
        return 1;
    }
}
