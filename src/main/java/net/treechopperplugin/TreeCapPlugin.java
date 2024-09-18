package net.treechopperplugin;

import net.milkbowl.vault.economy.Economy;
import net.treechopperplugin.Listener.*;
import net.treechopperplugin.Commands.*;
import net.treechopperplugin.GUI.AxeUpgradeMenu;
import net.treechopperplugin.GUI.CharacterUpgradeMenu;
import net.treechopperplugin.Generation.*;
import net.treechopperplugin.GlobalEvents.GlobalEventsConfig;
import net.treechopperplugin.Items.GiveItemCommand;
import net.treechopperplugin.Items.Items;
import net.treechopperplugin.Items.ReloadItemsCommand;
import net.treechopperplugin.Measurement.HologramProgress;
import net.treechopperplugin.Measurement.Village;
import net.treechopperplugin.Measurement.SawmillManager;
import net.treechopperplugin.Upgrades.UpgradesAxe;
import net.treechopperplugin.Upgrades.UpgradesHero;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TreeCapPlugin extends JavaPlugin {
    private static TreeCapPlugin instance;
    private Items items;
    private UpgradesAxe upgradesAxe;
    private UpgradesHero upgradesHero;
    private FileConfiguration itemsConfig;
    private FileConfiguration upgradesAxeConfig;
    private FileConfiguration upgradesHeroConfig;
    private File itemsConfigFile;
    private File upgradesAxeConfigFile;
    private File upgradesHeroConfigFile;
    private GlobalEventsConfig globalEventsConfig;
    private TreeListener treeListener;
    private static Economy economy = null;
    private AxeUpgradeMenu axeUpgradeMenu;
    private SawmillManager sawmill;
    private FileConfiguration upgradesConfig;
    private File upgradesConfigFile;
    private FileConfiguration sawmillConfig;
    private File sawmillConfigFile;
    private FileConfiguration blockPriceConfig;
    private File blockPriceConfigFile;
    private FileConfiguration upgradesPlayersConfig;
    private File upgradesPlayersConfigFile;
    private HologramProgress hologramProgress;
    private final List<HologramProgress> holograms = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault economy not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Holograms
        File hologramFile = new File(getDataFolder(), "holograms.yml");
        if (hologramFile.exists()) {
            FileConfiguration hologramConfig = YamlConfiguration.loadConfiguration(hologramFile);
            if (hologramConfig.contains("holograms")) {
                List<Map<String, Object>> savedHolograms = (List<Map<String, Object>>) hologramConfig.getList("holograms");
                if (savedHolograms != null) {
                    for (Map<String, Object> hologramData : savedHolograms) {
                        Map<String, Object> locationMap = (Map<String, Object>) hologramData.get("location");
                        Location location = Location.deserialize(locationMap);
                        int progress = ((Number) hologramData.get("progress")).intValue();
                        HologramProgress hologram = new HologramProgress(location, 100);
                        hologram.setCurrentResources(progress);
                    }
                }
            }
        }

        globalEventsConfig = new GlobalEventsConfig(this);
        globalEventsConfig.registerEvents();

        saveDefaultConfig();

        this.items = new Items(this);
        this.axeUpgradeMenu = new AxeUpgradeMenu(this);
        this.upgradesHero = new UpgradesHero(this);
        this.upgradesAxe = new UpgradesAxe(this);
        this.sawmill = new SawmillManager(this);

        loadItemsConfig();
        loadUpgradesAxeConfig();
        loadUpgradesHeroConfig();
        loadUpgradesPlayersConfig();
        loadBlockPriceConfig();

        ConfigGeneration configGeneration = new ConfigGeneration();
        List<GenerationArea> areas = configGeneration.loadAreasFromConfig();

        for (GenerationArea area : areas) {
            if (area.getGenerationType().equalsIgnoreCase("ACACIA")) {
                CustomGenerationAcacia generator = new CustomGenerationAcacia(
                        Bukkit.getWorld("world"),
                        area,
                        50
                );
            }
        }

        Location location = new Location(Bukkit.getWorld("world"), -820, 104, 257);
        HologramProgress hologram = new HologramProgress(location, 500);
        holograms.add(hologram);

        CharacterUpgradeMenu characterUpgradeMenu = new CharacterUpgradeMenu(this);
        getServer().getPluginManager().registerEvents(characterUpgradeMenu, this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.items, this.upgradesHero), this);
        getServer().getPluginManager().registerEvents(new TreeListener(this, upgradesHero), this);
        getServer().getPluginManager().registerEvents(new FastLeafDecay(), this);
        getServer().getPluginManager().registerEvents(new AcaciaListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCClickListener(this), this);
        getServer().getPluginManager().registerEvents(new NBTListener(this), this);
        getServer().getPluginManager().registerEvents(new SellingBlockListener(this), this);
        SawmillManager sawmillManager = new SawmillManager(this);

        items = new Items(this);
        getCommand("giveitem").setExecutor(new GiveItemCommand(this.items));
        getServer().getPluginManager().registerEvents(new GiveItemCommand(items), this);
        getCommand("reloaditems").setExecutor(new ReloadItemsCommand(this.items));
        getCommand("upgrade").setExecutor(new UpgradeCommand(this));
        getCommand("setuphero").setExecutor(new UpgradeHeroAdminCommand(this, upgradesHero));
        getCommand("village").setExecutor(new Village(this));
        getCommand("return").setExecutor(new Village(this));
        getCommand("upgradehero").setExecutor(new UpgradeHeroCommand(this, characterUpgradeMenu));
        getCommand("savenpcpos").setExecutor(new NPCSaveCommand(this));
        getCommand("hogo").setExecutor(new HologramCommand());
        getCommand("sawmillDrop").setExecutor(new SawmillDonateCommand(sawmillManager));

        CustomGeneratorTree customGeneratorTree = new CustomGeneratorTree(Bukkit.getWorld("world"));
        getServer().getOnlinePlayers().forEach(player -> upgradesHero.loadUpgradesFromFile(player));

        this.items.updateAllPlayersInventories();
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabling plugin and removing armor stands...");
        for (org.bukkit.World bukkitWorld : Bukkit.getWorlds()) {
            net.minecraft.server.v1_12_R1.World nmsWorld = ((org.bukkit.craftbukkit.v1_12_R1.CraftWorld) bukkitWorld).getHandle();

            List<net.minecraft.server.v1_12_R1.Entity> entities = new ArrayList<>(nmsWorld.entityList);

            for (net.minecraft.server.v1_12_R1.Entity entity : entities) {
                if (entity instanceof net.minecraft.server.v1_12_R1.EntityArmorStand) {
                    nmsWorld.removeEntity(entity);
                    Bukkit.getLogger().info("Removed an armor stand.");
                }
            }
        }
    }


    public static TreeCapPlugin getInstance() {
        return instance;
    }

    public TreeListener getTreeListener() {
        return this.treeListener;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
        return economy != null;
    }

    public void saveSawmillConfig() {
        if (sawmillConfig != null && sawmillConfigFile != null) {
            try {
                sawmillConfig.save(sawmillConfigFile);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось сохранить sawmill.yml", e);
            }
        }
    }

    public static Economy getEconomy() {
        return economy;
    }

    public UpgradesAxe getUpgradesAxe() {
        return this.upgradesAxe;
    }

    public UpgradesHero getUpgradesHero() {
        return this.upgradesHero;
    }

    public void loadItemsConfig() {
        if (this.itemsConfigFile == null) {
            this.itemsConfigFile = new File(this.getDataFolder(), "items.yml");
        }

        this.getLogger().info("Loading items configuration from: " + this.itemsConfigFile.getAbsolutePath());

        try {
            this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsConfigFile);
            this.getLogger().info("Items configuration loaded successfully.");

            if (this.itemsConfig.getConfigurationSection("items") == null) {
                this.getLogger().warning("The 'items' section is missing in items.yml.");
            } else {
                this.getLogger().info("The 'items' section is present.");
            }

            this.getLogger().info("Loaded items sections: " + this.itemsConfig.getConfigurationSection("items").getKeys(false).toString());

        } catch (Exception e) {
            this.getLogger().severe("Failed to load items configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public FileConfiguration getUpgradesConfig() {
        if (upgradesConfig == null) {
            loadUpgradesConfig();
        }
        return upgradesConfig;
    }

    public void loadUpgradesConfig() {
        if (upgradesConfigFile == null) {
            upgradesConfigFile = new File(getDataFolder(), "UpgradesAxe.yml");
        }
        upgradesConfig = YamlConfiguration.loadConfiguration(upgradesConfigFile);
    }


    public void loadUpgradesAxeConfig() {
        if (this.upgradesAxeConfigFile == null) {
            this.upgradesAxeConfigFile = new File(this.getDataFolder(), "UpgradesAxe.yml");
        }

        this.getLogger().info("Loading UpgradesAxe configuration from: " + this.upgradesAxeConfigFile.getAbsolutePath());

        try {
            this.upgradesAxeConfig = YamlConfiguration.loadConfiguration(this.upgradesAxeConfigFile);
            this.getLogger().info("UpgradesAxe configuration loaded successfully.");
            logUpgradesAxeConfig();
        } catch (Exception e) {
            this.getLogger().severe("Failed to load UpgradesAxe configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadUpgradesHeroConfig() {
        if (this.upgradesHeroConfigFile == null) {
            this.upgradesHeroConfigFile = new File(this.getDataFolder(), "UpgradesHero.yml");
        }
        try {
            this.upgradesHeroConfig = YamlConfiguration.loadConfiguration(this.upgradesHeroConfigFile);
            logUpgradesHeroConfig();

        } catch (Exception e) {
            this.getLogger().severe("Failed to load UpgradesHero configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logItemsConfig() {
        this.getLogger().info("Items configuration:");
        if (this.itemsConfig != null) {
            for (String key : this.itemsConfig.getConfigurationSection("items").getKeys(false)) {
                this.getLogger().info("Key: " + key + " - " + this.itemsConfig.getConfigurationSection("items." + key).getValues(false).toString());
            }
        }
    }

    public void loadUpgradesPlayersConfig() {
        if (this.upgradesPlayersConfigFile == null) {
            this.upgradesPlayersConfigFile = new File(this.getDataFolder(), "upgradesPlayers.yml");
        }

        if (!this.upgradesPlayersConfigFile.exists()) {
            this.getLogger().warning("upgradesPlayers.yml не найден! Создаем новый файл.");
            this.saveResource("upgradesPlayers.yml", false);
        }

        this.getLogger().info("Loading upgradesPlayers configuration from: " + this.upgradesPlayersConfigFile.getAbsolutePath());

        try {
            this.upgradesPlayersConfig = YamlConfiguration.loadConfiguration(this.upgradesPlayersConfigFile);
            this.getLogger().info("UpgradesPlayers configuration loaded successfully.");
        } catch (Exception e) {
            this.getLogger().severe("Failed to load upgradesPlayers configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadBlockPriceConfig() {
        if (this.blockPriceConfigFile == null) {
            this.blockPriceConfigFile = new File(this.getDataFolder(), "BlockSell.yml");
        }

        if (!this.blockPriceConfigFile.exists()) {
            this.getLogger().warning("BlockSell.yml не найден! Создаем новый файл.");
            this.saveResource("BlockSell.yml", false);
        }

        this.getLogger().info("Loading block price configuration from: " + this.blockPriceConfigFile.getAbsolutePath());

        try {
            this.blockPriceConfig = YamlConfiguration.loadConfiguration(this.blockPriceConfigFile);
            this.getLogger().info("Block price configuration loaded successfully.");
        } catch (Exception e) {
            this.getLogger().severe("Failed to load block price configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public FileConfiguration getBlockPriceConfig() {
        return this.blockPriceConfig;
    }


    private void logUpgradesAxeConfig() {
        this.getLogger().info("UpgradesAxe configuration:");
        if (this.upgradesAxeConfig != null) {
            for (String key : this.upgradesAxeConfig.getKeys(false)) {
                this.getLogger().info("Key: " + key + " - " + this.upgradesAxeConfig.getConfigurationSection(key).getValues(false).toString());
            }
        }
    }

    private void logUpgradesHeroConfig() {
        this.getLogger().info("UpgradesHero configuration:");
        if (this.upgradesHeroConfig != null) {
            for (String key : this.upgradesHeroConfig.getKeys(false)) {
            }
        }
    }

    public FileConfiguration getItemsConfig() {
        if (this.itemsConfig == null) {
            this.loadItemsConfig();
        }
        return this.itemsConfig;
    }

    public void saveItemsConfig() {
        if (itemsConfig != null && itemsConfigFile != null) {
            try {
                itemsConfig.save(itemsConfigFile);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось сохранить items.yml", e);
            }
        }
    }

    public FileConfiguration getUpgradesAxeConfig() {
        if (upgradesAxeConfig == null) {
            loadUpgradesAxeConfig();
        }
        return upgradesAxeConfig;
    }

    public FileConfiguration getUpgradesHeroConfig() {
        if (upgradesHeroConfig == null) {
            loadUpgradesHeroConfig();
        }
        return upgradesHeroConfig;
    }
}
