package net.treechopperplugin.Generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigGeneration {

    private FileConfiguration config;
    private File configFile;

    public ConfigGeneration() {
        createConfig();
        loadAreasFromConfig();
    }

    private void createConfig() {
        File dataFolder = Bukkit.getPluginManager().getPlugin("TreeCapPlugin").getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        configFile = new File(dataFolder, "CustomGeneration.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
                List<Map<String, Object>> areasList = new ArrayList<>();
                Map<String, Object> areaMap = new HashMap<>();
                areaMap.put("name", "AcaciaArea");
                areaMap.put("corner1", Arrays.asList(-907, 104, 200));
                areaMap.put("corner2", Arrays.asList(-966, 104, 279));
                areaMap.put("generationType", "ACACIA");
                areasList.add(areaMap);
                config.set("areas", areasList);
                saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public List<GenerationArea> loadAreasFromConfig() {
        List<GenerationArea> areas = new ArrayList<>();

        if (config.contains("areas") && config.getList("areas") != null) {
            List<Map<?, ?>> areasList = config.getMapList("areas");

            for (Map<?, ?> areaMap : areasList) {
                String name = (String) areaMap.get("name");
                List<Integer> corner1Coords = (List<Integer>) areaMap.get("corner1");
                List<Integer> corner2Coords = (List<Integer>) areaMap.get("corner2");
                String generationType = (String) areaMap.get("generationType");

                if (name == null || corner1Coords == null || corner2Coords == null || generationType == null) {
                    Bukkit.getLogger().warning("Config section for area is missing data.");
                    continue;
                }

                if (corner1Coords.size() < 3 || corner2Coords.size() < 3) {
                    Bukkit.getLogger().warning("Invalid coordinates for an area.");
                    continue;
                }

                World world = Bukkit.getWorld("world");
                if (world == null) {
                    Bukkit.getLogger().warning("World 'world' not found.");
                    continue;
                }

                Location corner1 = new Location(world, corner1Coords.get(0), corner1Coords.get(1), corner1Coords.get(2));
                Location corner2 = new Location(world, corner2Coords.get(0), corner2Coords.get(1), corner2Coords.get(2));

                areas.add(new GenerationArea(corner1, corner2, generationType));
            }
        } else {
            Bukkit.getLogger().warning("No areas found in the config or incorrect format.");
        }
        return areas;
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
