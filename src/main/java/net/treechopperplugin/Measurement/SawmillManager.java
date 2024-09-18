package net.treechopperplugin.Measurement;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import net.treechopperplugin.TreeCapPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SawmillManager {

    private final FileConfiguration config;
    private final Map<Material, Integer> requiredResources;

    public SawmillManager(TreeCapPlugin plugin) {
        File configFile = new File(plugin.getDataFolder(), "sawmill.yml");
        if (!configFile.exists()) {
            plugin.saveResource("sawmill.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        requiredResources = new HashMap<>();
        loadRequiredResources();
    }

    private void loadRequiredResources() {
        if (config.contains("resources")) {
            for (String key : config.getConfigurationSection("resources").getKeys(false)) {
                int amount = config.getInt("resources." + key);
                Material material = getMaterialFromName(key);
                if (material != null) {
                    requiredResources.put(material, amount);
                    Bukkit.getLogger().info("Loaded resource: " + material + " Amount: " + amount);
                } else {
                    Bukkit.getLogger().warning("Invalid material name: " + key);
                }
            }
        } else {
            Bukkit.getLogger().warning("No resources section found in the configuration file.");
        }
    }

    private Material getMaterialFromName(String name) {
        switch (name.toUpperCase()) {
            case "LOG":
                return Material.LOG;
            case "LOG_2":
                return Material.LOG_2;
            case "LOG_1":
                return null;
            default:
                return null;
        }
    }

    public void openDonationInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Пожертвовать ресурсы");

        ItemStack confirmButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§aПодтвердить");
            confirmButton.setItemMeta(confirmMeta);
        }
        inv.setItem(3, confirmButton);

        ItemStack cancelButton = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§cОтменить");
            cancelButton.setItemMeta(cancelMeta);
        }
        inv.setItem(5, cancelButton);

        ItemStack visualBlock = new ItemStack(Material.LOG);
        ItemMeta visualMeta = visualBlock.getItemMeta();
        if (visualMeta != null) {
            visualMeta.setDisplayName("§eВы жертвуете ресурсы");
            visualBlock.setItemMeta(visualMeta);
        }
        inv.setItem(4, visualBlock);

        player.openInventory(inv);
    }

    public void handleResourceDonation(Player player, ItemStack item) {
        Material type = item.getType();
        if (requiredResources.containsKey(type)) {
            int amountToDonate = item.getAmount();
            int requiredAmount = requiredResources.get(type);

            if (amountToDonate >= requiredAmount) {
                requiredResources.put(type, requiredAmount - amountToDonate);
                player.sendMessage("§aВы пожертвовали " + type.name() + " в строительство лесопилки.");
            } else {
                player.sendMessage("§cНедостаточно ресурсов. Требуется " + requiredAmount + " " + type.name());
            }
        } else {
            player.sendMessage("§cУ вас нет ресурсов для пожертвования.");
        }
    }
}
