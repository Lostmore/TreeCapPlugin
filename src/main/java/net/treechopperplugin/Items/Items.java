package net.treechopperplugin.Items;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Items {
    private JavaPlugin plugin;
    private FileConfiguration itemsConfig;
    private File itemsFile;
    private Map<String, ItemStack> itemsMap = new HashMap();
    private Map<String, Integer> levelMap = new HashMap<>();
    private FileTime lastModifiedTime;


    public Items(JavaPlugin plugin) {
        this.plugin = plugin;
        this.createItemsConfig();
        this.loadItems();
        this.startConfigWatcher();
    }

    public Set<String> getItemIds() {
        return this.itemsMap.keySet();
    }

    public int getGlobalLevel(String itemId) {
        return this.levelMap.getOrDefault(itemId, 0);
    }

    private void createItemsConfig() {
        this.itemsFile = new File(this.plugin.getDataFolder(), "items.yml");
        if (!this.itemsFile.exists()) {
            this.itemsFile.getParentFile().mkdirs();
            this.plugin.saveResource("items.yml", false);
        }

        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsFile);
        this.updateLastModifiedTime();
    }

    public void reloadItemsConfig() {
        this.itemsConfig = YamlConfiguration.loadConfiguration(this.itemsFile);
        this.loadItems();
        this.updateAllPlayersInventories();
    }

    private void loadItems() {
        this.itemsMap.clear();
        Iterator var1 = this.itemsConfig.getConfigurationSection("items").getKeys(false).iterator();

        while(true) {
            while(var1.hasNext()) {
                String key = (String)var1.next();
                String path = "items." + key;
                String materialStr = this.itemsConfig.getString(path + ".material");
                Material material = Material.getMaterial(materialStr);
                if (material == null) {
                    this.plugin.getLogger().warning("Invalid material for item " + key + ": " + materialStr);
                } else {
                    String name = this.itemsConfig.getString(path + ".name");
                    List<String> lore = this.itemsConfig.getStringList(path + ".lore");
                    boolean hideFlags = this.itemsConfig.getBoolean(path + ".hide_flags", false);
                    boolean unbreakable = this.itemsConfig.getBoolean(path + ".unbreakable", false);
                    boolean hideEnchantments = this.itemsConfig.getBoolean(path + ".hide_enchantments", false);
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (name != null) {
                        meta.setDisplayName(name.replace("&", "§"));
                    }

                    if (lore != null) {
                        for(int i = 0; i < lore.size(); ++i) {
                            lore.set(i, ((String)lore.get(i)).replace("&", "§"));
                        }

                        meta.setLore(lore);
                    }

                    if (hideFlags) {
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS);
                    }

                    if (unbreakable) {
                        meta.setUnbreakable(true);
                    }

                    if (hideEnchantments) {
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }

                    Map<Enchantment, Integer> enchantments = new HashMap();
                    if (this.itemsConfig.isConfigurationSection(path + ".enchantments")) {
                        Iterator var14 = this.itemsConfig.getConfigurationSection(path + ".enchantments").getKeys(false).iterator();

                        while(var14.hasNext()) {
                            String ench = (String)var14.next();
                            Enchantment enchantment = Enchantment.getByName(ench.toUpperCase());
                            if (enchantment != null) {
                                int level = this.itemsConfig.getInt(path + ".enchantments." + ench);
                                enchantments.put(enchantment, level);
                            }
                        }
                    }

                    item.setItemMeta(meta);
                    item.addUnsafeEnchantments(enchantments);
                    this.itemsMap.put(key, item);
                }
            }

            return;
        }
    }

    public ItemStack getItem(String id) {
        return (ItemStack)this.itemsMap.get(id);
    }

    public void updatePlayerInventory(Player player) {
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        ItemStack[] updatedContents = new ItemStack[contents.length];

        for (int i = 0; i < contents.length; i++) {
            ItemStack currentItem = contents[i];
            if (currentItem != null && currentItem.hasItemMeta() && currentItem.getItemMeta().hasDisplayName()) {
                String displayName = currentItem.getItemMeta().getDisplayName();
                ItemStack configuredItem = findConfiguredItemByName(displayName);
                if (configuredItem != null) {
                    updatedContents[i] = configuredItem;
                } else {
                    updatedContents[i] = currentItem;
                }
            } else {
                updatedContents[i] = currentItem;
            }
        }

        inventory.setContents(updatedContents);
    }


    private ItemStack findConfiguredItemByName(String displayName) {
        Iterator var2 = this.itemsMap.entrySet().iterator();

        ItemStack configuredItem;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            Map.Entry<String, ItemStack> entry = (Map.Entry)var2.next();
            configuredItem = (ItemStack)entry.getValue();
        } while(!configuredItem.getItemMeta().getDisplayName().equals(displayName));

        return configuredItem;
    }

    public void updateAllPlayersInventories() {
        Iterator var1 = Bukkit.getOnlinePlayers().iterator();

        while(var1.hasNext()) {
            Player player = (Player)var1.next();
            this.updatePlayerInventory(player);
        }

    }

    private void startConfigWatcher() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, (Runnable)(this::checkConfigUpdates), 20L, 200L);
    }

    private void checkConfigUpdates() {
        try {
            FileTime currentModifiedTime = Files.getLastModifiedTime(Paths.get(this.itemsFile.toURI()));
            if (currentModifiedTime.compareTo(this.lastModifiedTime) > 0) {
                this.lastModifiedTime = currentModifiedTime;
                Bukkit.getScheduler().runTask(this.plugin, (Runnable)(this::reloadItemsConfig));
            }
        } catch (IOException var2) {
            this.plugin.getLogger().severe("Error checking items config file modification time: " + var2.getMessage());
        }

    }
    public void getUpdateItems() {
        FileConfiguration itemsConfig = plugin.getConfig();
        plugin.getLogger().info("Конфигурация загружена: " + itemsConfig.getKeys(false));
    }

    private void updateLastModifiedTime() {
        try {
            this.lastModifiedTime = Files.getLastModifiedTime(Paths.get(this.itemsFile.toURI()));
        } catch (IOException var2) {
            this.plugin.getLogger().severe("Error getting items config file modification time: " + var2.getMessage());
        }

    }
}