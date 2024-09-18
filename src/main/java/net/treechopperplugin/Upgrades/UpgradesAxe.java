package net.treechopperplugin.Upgrades;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class UpgradesAxe {
    private final TreeCapPlugin plugin;

    public UpgradesAxe(TreeCapPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean upgradeAxe(Player player, ItemStack axe) {
        FileConfiguration itemsConfig = this.plugin.getItemsConfig();
        ItemMeta meta = axe.getItemMeta();
        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Не удалось получить данные о топоре.");
            plugin.getLogger().warning("Player " + player.getName() + " has an axe with null metadata.");
            return false;
        } else {
            String displayName = meta.getDisplayName();
            String currentKey = this.findCurrentAxeKey(displayName, itemsConfig);
            if (currentKey == null) {
                player.sendMessage(ChatColor.RED + "Не удалось определить текущий тип топора.");
                return false;
            } else {
                String nextLevelKey = itemsConfig.getString("items." + currentKey + ".nextLevel");
                if (nextLevelKey != null && !nextLevelKey.equals("null")) {
                    if (!this.checkUpgradeRequirements(player, currentKey, nextLevelKey, itemsConfig)) {
                        return false;
                    } else {
                        ItemStack upgradedAxe = this.createAxeFromConfig(nextLevelKey);
                        if (upgradedAxe == null) {
                            player.sendMessage(ChatColor.RED + "Не удалось создать улучшенный топор.");
                            plugin.getLogger().warning("Failed to create upgraded axe for key: " + nextLevelKey);
                            return false;
                        } else {
                            int slot = player.getInventory().getHeldItemSlot();
                            player.getInventory().setItem(slot, upgradedAxe);
                            return true;
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Максимальный уровень достигнут.");
                    return false;
                }
            }
        }
    }

    public boolean isMaxLevel(ItemStack axe) {
        FileConfiguration itemsConfig = this.plugin.getItemsConfig();
        ItemMeta meta = axe.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("Item meta is null for axe.");
            return false;
        } else {
            String displayName = meta.getDisplayName();
            String currentKey = this.findCurrentAxeKey(displayName, itemsConfig);
            if (currentKey == null) {
                return false;
            } else {
                String nextLevelKey = itemsConfig.getString("items." + currentKey + ".nextLevel");
                return nextLevelKey == null || nextLevelKey.equals("null");
            }
        }
    }

    public ItemStack getNextLevelAxe(ItemStack currentAxe) {
        FileConfiguration itemsConfig = this.plugin.getItemsConfig();
        ItemMeta meta = currentAxe.getItemMeta();
        if (meta == null) {
            return null;
        } else {
            String displayName = meta.getDisplayName();
            String currentKey = this.findCurrentAxeKey(displayName, itemsConfig);
            if (currentKey == null) {
                return null;
            } else {
                String nextLevelKey = itemsConfig.getString("items." + currentKey + ".nextLevel");
                return nextLevelKey != null && !nextLevelKey.equals("null") ? this.createAxeFromConfig(nextLevelKey) : null;
            }
        }
    }

    private String findCurrentAxeKey(String displayName, FileConfiguration itemsConfig) {
        String colorizedDisplayName = ChatColor.translateAlternateColorCodes('&', displayName);
        if (itemsConfig.getConfigurationSection("items") != null) {
            Iterator<String> iterator = itemsConfig.getConfigurationSection("items").getKeys(false).iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String itemName = itemsConfig.getString("items." + key + ".name");
                String colorizedItemName = ChatColor.translateAlternateColorCodes('&', itemName);
                if (colorizedItemName.equals(colorizedDisplayName)) {
                    return key;
                }
            }
        } else {
            plugin.getLogger().warning("Раздел 'items' отсутствует в конфигурации.");
        }
        plugin.getLogger().warning("Не удалось найти топор с именем: '" + displayName + "'");
        return null;
    }

    private boolean checkUpgradeRequirements(Player player, String currentKey, String nextLevelKey, FileConfiguration itemsConfig) {
        FileConfiguration upgradesConfig = this.plugin.getUpgradesConfig();
        if (upgradesConfig == null) {
            player.sendMessage(ChatColor.RED + "Ошибка конфигурации обновлений.");
            this.plugin.getLogger().severe("Failed to load upgrades configuration.");
            return false;
        }

        ConfigurationSection upgradeSection = upgradesConfig.getConfigurationSection("upgrades." + currentKey);
        if (upgradeSection == null) {
            player.sendMessage(ChatColor.RED + "Не найдены требования для текущего уровня.");
            this.plugin.getLogger().warning("Upgrade requirements section missing for key: " + currentKey);
            return false;
        }

        int cost = upgradeSection.getInt("cost");
        List<String> requiredBlocks = upgradeSection.getStringList("required_blocks");

        return true;
    }



    private ItemStack createAxeFromConfig(String key) {
        FileConfiguration itemsConfig = this.plugin.getItemsConfig();
        String materialName = itemsConfig.getString("items." + key + ".material");
        if (materialName == null) {
            plugin.getLogger().warning("Материал для ключа " + key + " не найден.");
            return null;
        }

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Материал для ключа " + key + " не распознан: " + materialName);
            return null;
        }

        ItemStack axe = new ItemStack(material);
        ItemMeta meta = axe.getItemMeta();
        if (meta == null) {
            plugin.getLogger().warning("Не удалось получить ItemMeta для материала: " + materialName);
            return null;
        }

        String name = itemsConfig.getString("items." + key + ".name");
        if (name != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        } else {
            plugin.getLogger().warning("Название для ключа " + key + " отсутствует.");
        }

        List<String> lore = itemsConfig.getStringList("items." + key + ".lore");
        if (lore != null) {
            List<String> translatedLore = new ArrayList<>();
            for (String loreLine : lore) {
                translatedLore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
            }
            meta.setLore(translatedLore);
        }

        if (itemsConfig.contains("items." + key + ".enchantments")) {
            ConfigurationSection enchantmentsSection = itemsConfig.getConfigurationSection("items." + key + ".enchantments");
            if (enchantmentsSection != null) {
                for (String enchantmentKey : enchantmentsSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByName(enchantmentKey);
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(enchantmentKey);
                        meta.addEnchant(enchantment, level, true);
                    }
                }
            }
        }

        meta.spigot().setUnbreakable(itemsConfig.getBoolean("items." + key + ".unbreakable", false));
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

        axe.setItemMeta(meta);
        return axe;
    }
}
