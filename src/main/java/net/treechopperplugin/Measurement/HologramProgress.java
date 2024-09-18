package net.treechopperplugin.Measurement;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.World;
import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HologramProgress {

    private static Map<Integer, HologramProgress> holograms = new HashMap<>();
    private static int nextId = 1;

    private final int id;
    private final EntityArmorStand armorStand;
    private final Location location;
    private final int totalResourcesRequired;
    private int currentResources;
    private BukkitTask taskRemove;
    private final int defaultResourcesRequired = 0;

    public HologramProgress(Location location, int totalResourcesRequired) {
        this.id = nextId++;
        World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        armorStand = new EntityArmorStand(nmsWorld);
        armorStand.setPosition(location.getX(), location.getY(), location.getZ());
        armorStand.setCustomNameVisible(true);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setMarker(true);

        nmsWorld.addEntity(armorStand);

        this.location = location;
        this.totalResourcesRequired = totalResourcesRequired;
        this.currentResources = 0;
        holograms.put(id, this);

        updateProgress();
    }

    public static HologramProgress getHologramById(int id) {
        return holograms.get(id);
    }

    public static Map<Integer, HologramProgress> getAllHolograms() {
        return holograms;
    }

    public void saveProgress() {
        File hologramFile = new File(TreeCapPlugin.getInstance().getDataFolder(), "holograms.yml");
        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);

        List<Map<String, Object>> savedHolograms = new ArrayList<>();
        for (Map.Entry<Integer, HologramProgress> entry : holograms.entrySet()) {
            HologramProgress hologram = entry.getValue();
            Map<String, Object> hologramData = new HashMap<>();
            hologramData.put("location", hologram.getLocation().serialize());
            hologramData.put("progress", hologram.getCurrentResources());
            hologramData.put("totalResources", hologram.getTotalResourcesRequired());
            savedHolograms.add(hologramData);
        }

        hologramConfig.set("holograms", savedHolograms);
        try {
            hologramConfig.save(hologramFile);
            Bukkit.getLogger().info("Holograms saved successfully.");
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save holograms: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public int getCurrentResources() {
        return this.currentResources;
    }


    public void loadProgress() {
        File hologramFile = new File(TreeCapPlugin.getInstance().getDataFolder(), "holograms.yml");
        if (!hologramFile.exists()) {
            return;
        }

        FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);

        List<Map<?, ?>> savedHolograms = hologramConfig.getMapList("holograms");
        for (Map<?, ?> hologramData : savedHolograms) {
            Map<String, Object> locationMap = (Map<String, Object>) hologramData.get("location");
            Location location = Location.deserialize(locationMap);
            int progress = ((Number) hologramData.get("progress")).intValue();
            int totalResources = defaultResourcesRequired;

            if (hologramData.containsKey("totalResources")) {
                totalResources = ((Number) hologramData.get("totalResources")).intValue();
            }

            HologramProgress hologram = new HologramProgress(location, totalResources);
            hologram.setCurrentResources(progress);
        }
    }

    public static void reloadAllHolograms() {
        for (HologramProgress hologram : holograms.values()) {
            hologram.remove();
        }
        holograms.clear();

        HologramProgress hologram = new HologramProgress(null, 0);
        hologram.loadProgress();
    }

    public void updateProgress() {
        double progressPercent = ((double) currentResources / totalResourcesRequired) * 100;
        String progressBar = createProgressBar(progressPercent);
        armorStand.setCustomName("§aСтройка лесопилки: " + (int) progressPercent + "% " + progressBar);

        if (progressPercent == 100) {
            armorStand.setCustomName("§aПостройка лесопилки успешно завершена!");
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            taskRemove = new BukkitRunnable() {
                @Override
                public void run() {
                    remove();
                }
            }.runTaskLater(Bukkit.getPluginManager().getPlugin("TreeCapPlugin"), 300L);
        }
    }

    public void remove() {
        if (armorStand != null) {
            net.minecraft.server.v1_12_R1.World nmsWorld = armorStand.world;
            nmsWorld.removeEntity(armorStand);
        }
        holograms.remove(id);
    }

    public void setCurrentResources(int currentResources) {
        if (currentResources > totalResourcesRequired) {
            this.currentResources = totalResourcesRequired;
        } else if (currentResources < 0) {
            this.currentResources = 0;
        } else {
            this.currentResources = currentResources;
        }
        updateProgress();
    }

    private String createProgressBar(double percent) {
        int totalBlocks = 20;
        int filledBlocks = (int) (totalBlocks * percent / 100);
        StringBuilder bar = new StringBuilder("§f[");

        for (int i = 0; i < totalBlocks; i++) {
            if (i < filledBlocks) {
                bar.append("§a█");
            } else {
                bar.append("§7█");
            }
        }
        bar.append("§f]");
        return bar.toString();
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return this.location;
    }

    public static boolean hasHologramAtLocation(Location location) {
        World nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        for (Entity entity : nmsWorld.entityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                if (armorStand.locX == location.getX() && armorStand.locY == location.getY() && armorStand.locZ == location.getZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTotalResourcesRequired() {
        return totalResourcesRequired;
    }
}