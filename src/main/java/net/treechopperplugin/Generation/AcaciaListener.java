package net.treechopperplugin.Generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AcaciaListener implements Listener {

    private final JavaPlugin plugin;
    private List<Location> acaciaPlantLocations;

    public AcaciaListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadAcaciaPlantLocations();
    }

    private void loadAcaciaPlantLocations() {
        acaciaPlantLocations = new ArrayList<>();
        File file = new File(plugin.getDataFolder(), "CustomGeneration.yml");

        if (!file.exists()) {
            plugin.saveResource("CustomGeneration.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> locations = config.getList("acacia_plant_locations");

        if (locations != null) {
            for (Object loc : locations) {
                if (loc instanceof Map<?, ?>) {
                    Map<?, ?> map = (Map<?, ?>) loc;
                    String worldName = (String) map.get("world");
                    double x = ((Number) map.get("x")).doubleValue();
                    double y = ((Number) map.get("y")).doubleValue();
                    double z = ((Number) map.get("z")).doubleValue();

                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z);
                    acaciaPlantLocations.add(location);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location brokenBlockLocation = event.getBlock().getLocation();

        for (Location location : acaciaPlantLocations) {
            if (location.equals(brokenBlockLocation)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    brokenBlockLocation.getBlock().setType(Material.SAPLING);
                    brokenBlockLocation.getBlock().setData((byte) 4);
                }, 200L);
                break;
            }
        }
    }
}
