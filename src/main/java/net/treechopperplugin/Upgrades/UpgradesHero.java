package net.treechopperplugin.Upgrades;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getLogger;

public class UpgradesHero {
    private static final long NATURE_CALL_COOLDOWN = 90L;
    private static final long NATURE_CALL_DURATION = 30L;

    private final Map<Player, Long> cooldowns = new HashMap<>();
    private final Map<String, Integer> upgradeLevels;
    private static final Map<String, Integer> MAX_LEVELS = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> playerUpgrades = new HashMap<>();


    static {
        MAX_LEVELS.put("SharpBlades", 5);
        MAX_LEVELS.put("LumberjackMastery", 5);
        MAX_LEVELS.put("StrikePrecision", 5);
        MAX_LEVELS.put("LumberjackStrength", 5);
        MAX_LEVELS.put("NatureCall", 1);
    }

    private final TreeCapPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public UpgradesHero(TreeCapPlugin plugin) {
        this.plugin = plugin;
        this.upgradeLevels = new HashMap<>();
        MAX_LEVELS.forEach((upgrade, level) -> upgradeLevels.put(upgrade, 0));

        // initialization
        configFile = new File(plugin.getDataFolder(), "upgradesPlayers.yml");
        if (!configFile.exists()) {
            plugin.saveResource("upgradesPlayers.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

    }


    public void loadUpgradesFromFile(Player player) {
        UUID playerId = player.getUniqueId();
        String path = "players." + playerId.toString();

        if (config.contains(path)) {
            Map<String, Object> playerUpgradesMap = config.getConfigurationSection(path).getValues(false);
            playerUpgradesMap.forEach((key, value) -> {
                String formattedKey = formatUpgradeName(key);
                if (formattedKey != null) {
                    upgradeLevels.put(formattedKey, (Integer) value);
                }
            });
        } else {
            MAX_LEVELS.forEach((upgrade, level) -> upgradeLevels.put(upgrade, 0));
        }
    }


    public void saveUpgradesToFile(Player player) {
        UUID playerId = player.getUniqueId();
        String path = "players." + playerId.toString();

        if (config.contains(path)) {
            config.set(path, null);
        }

        config.createSection(path, upgradeLevels);
        try {
            config.save(configFile);
        } catch (IOException e) {
            Logger.getLogger(plugin.getName()).severe("Failed to save upgrades to file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAllUpgradesToFile() {
        playerUpgrades.forEach((playerId, upgrades) -> {
            config.set("players." + playerId.toString(), upgrades);
        });
        try {
            config.save(configFile);
        } catch (IOException e) {
            Logger.getLogger(plugin.getName()).severe("Failed to save all upgrades to file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void createDefaultConfig() {
        config = new YamlConfiguration();
        config.createSection("players");
        try {
            config.save(configFile);
        } catch (IOException e) {
            Logger.getLogger(plugin.getName()).severe("Failed to save default upgrades config: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void setUpgradeLevel(Player player, String upgradeName, int level) {
        String formattedUpgradeName = formatUpgradeName(upgradeName);

        int maxLevel = MAX_LEVELS.getOrDefault(formattedUpgradeName, 0);
        if (level < 1 || level > maxLevel) {
            player.sendMessage("Уровень должен быть от 1 до " + maxLevel + " для " + formattedUpgradeName + ".");
            return;
        }

        if ("NatureCall".equalsIgnoreCase(formattedUpgradeName) && level != 1) {
            player.sendMessage("NatureCall может быть только уровня 1.");
            return;
        }

        upgradeLevels.put(formattedUpgradeName, level);

        Logger.getLogger(plugin.getName()).info("Setting upgrade level: " +
                "Upgrade=" + formattedUpgradeName + ", Level=" + level);

        saveUpgradesToFile(player);
    }


    private String formatUpgradeName(String upgradeName) {
        switch (upgradeName.toLowerCase()) {
            case "sharpblades":
                return "SharpBlades";
            case "lumberjackmastery":
                return "LumberjackMastery";
            case "strikeprecision":
                return "StrikePrecision";
            case "lumberjackstrength":
                return "LumberjackStrength";
            case "naturecall":
                return "NatureCall";
            default:
                return null;
        }
    }

    public double getSharpBladesBonus() {
        return upgradeLevels.getOrDefault("SharpBlades", 0) * 8.0;
    }

    public double getLumberjackMasteryBonus() {
        return upgradeLevels.getOrDefault("LumberjackMastery", 0) * 5.0;
    }

    public double getStrikePrecisionBonus() {
        int level = upgradeLevels.getOrDefault("StrikePrecision", 0);
        switch (level) {
            case 1: return 1.0;
            case 2: return 3.0;
            case 3: return 5.0;
            case 4: return 15.0;
            case 5: return 20.0;
            default: return 0.0;
        }
    }

    public double getLumberjackStrengthBonus() {
        int level = upgradeLevels.getOrDefault("LumberjackStrength", 0);
        switch (level) {
            case 1: return 3.0;
            case 2: return 6.0;
            case 3: return 9.0;
            case 4: return 12.0;
            case 5: return 15.0;
            default: return 0.0;
        }
    }

    public boolean canActivateNatureCall() {
        return upgradeLevels.getOrDefault("NatureCall", 0) > 0;
    }

    public double getNatureCallSpeedBoost() {
        return 25.0;
    }

    public int getNatureCallDuration() {
        return 60;
    }

    public double applyTreeChoppingSpeedBonus(double baseSpeed) {
        return baseSpeed * (1 + getSharpBladesBonus() / 100);
    }

    public boolean applyDoubleDropChance() {
        return Math.random() * 100 < getLumberjackMasteryBonus();
    }

    public boolean isCriticalHit() {
        return Math.random() * 100 < getStrikePrecisionBonus();
    }

    public double applyCriticalDamage(double baseDamage) {
        if (isCriticalHit()) {
            double critMultiplier = 2.0;
            double critBonus = getLumberjackStrengthBonus() / 100.0;
            return baseDamage * critMultiplier * (1 + critBonus);
        }
        return baseDamage;
    }

    public double applyNatureCallSpeedBonus(double baseSpeed) {
        if (canActivateNatureCall()) {
            return baseSpeed * (1 + getNatureCallSpeedBoost() / 100);
        }
        return baseSpeed;
    }

    public void activateNatureCall(Player player) {
        if (!canActivateNatureCall()) return;

        long currentTime = System.currentTimeMillis() / 1000;
        Long lastActivated = cooldowns.get(player);
        if (lastActivated != null && (currentTime - lastActivated) < NATURE_CALL_COOLDOWN) {
            player.sendMessage("Ability is on cooldown!");
            return;
        }

        cooldowns.put(player, currentTime);
        spawnNatureCallEntities(player);
        player.sendMessage("Nature Call activated!");

        new BukkitRunnable() {
            @Override
            public void run() {
                despawnNatureCallEntities(player);
            }
        }.runTaskLater(plugin, NATURE_CALL_DURATION * 20L);
    }

    private void spawnNatureCallEntities(Player player) {
        Location location = player.getLocation();
        for (int i = 0; i < 3; i++) {
            Vex vex = (Vex) player.getWorld().spawnEntity(location, EntityType.VEX);
            vex.setCustomName("Nature Spirit");
            vex.setCustomNameVisible(true);

            vex.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
            vex.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));

            vex.setSilent(true);
            vex.setCustomNameVisible(true);
            vex.setTarget(null);
            vex.setCanPickupItems(false);

            vex.setAI(true);
            vex.setRemoveWhenFarAway(false);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                despawnNatureCallEntities(player);
            }
        }.runTaskLater(plugin, NATURE_CALL_DURATION * 20L);
    }

    private void despawnNatureCallEntities(Player player) {
        player.getWorld().getEntities().stream()
                .filter(entity -> entity instanceof Vex && "Nature Spirit".equals(entity.getCustomName()))
                .forEach(Entity::remove);
    }

    public int getUpgradeLevel(Player player, String upgradeName) {
        String formattedUpgradeName = formatUpgradeName(upgradeName);
        if (formattedUpgradeName != null) {
            return upgradeLevels.getOrDefault(formattedUpgradeName, 0);
        }
        return 0;
    }

    public Map<String, Integer> getUpgradeLevels() {
        return new HashMap<>(upgradeLevels);
    }

    public Map<String, Integer> getMaxLevels() {
        return new HashMap<>(MAX_LEVELS);
    }

    public boolean isPrecisionHit() {
        return Math.random() * 100 < getStrikePrecisionBonus();
    }
}
