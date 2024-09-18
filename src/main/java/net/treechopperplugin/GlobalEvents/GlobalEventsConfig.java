package net.treechopperplugin.GlobalEvents;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class GlobalEventsConfig {

    private final JavaPlugin plugin;

    public GlobalEventsConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
    }

    private void setupConfig() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("events.thunderstorm.enabled")) {
            config.set("events.thunderstorm.enabled", true);
            plugin.saveConfig();
        }
    }

    public void registerEvents() {
        boolean thunderstormEnabled = plugin.getConfig().getBoolean("events.thunderstorm.enabled");
        if (thunderstormEnabled) {
            plugin.getServer().getPluginManager().registerEvents(new DoubleDamageWOOD(plugin), plugin);
        }
    }
}
