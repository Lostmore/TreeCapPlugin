package net.treechopperplugin.GUI;

import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.Upgrades.UpgradesHero;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CharacterUpgradeMenu implements Listener {
    private final TreeCapPlugin plugin;
    private final UpgradesHero upgradesHero;
    private final Map<Player, Long> playerLastClickMap = new HashMap<>();
    private static final long CLICK_COOLDOWN_MS = 1000L;

    public CharacterUpgradeMenu(TreeCapPlugin plugin) {
        this.plugin = plugin;
        this.upgradesHero = plugin.getUpgradesHero();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory upgradeMenu = Bukkit.createInventory((InventoryHolder) null, 27, ChatColor.BOLD + "Улучшение персонажа");

        upgradeMenu.setItem(10, createUpgradeItem("SharpBlades", "Острые лезвия", Material.IRON_SWORD, player));
        upgradeMenu.setItem(12, createUpgradeItem("LumberjackMastery", "Мастерство лесоруба", Material.DIAMOND_AXE, player));
        upgradeMenu.setItem(14, createUpgradeItem("StrikePrecision", "Точность удара", Material.BOW, player));
        upgradeMenu.setItem(16, createUpgradeItem("LumberjackStrength", "Сила лесоруба", Material.IRON_AXE, player));
        upgradeMenu.setItem(22, createUpgradeItem("NatureCall", "Зов природы", Material.SAPLING, player));

        ItemStack blueGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta paneMeta = blueGlassPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            blueGlassPane.setItemMeta(paneMeta);
        }
        for (int i = 0; i < upgradeMenu.getSize(); i++) {
            if (upgradeMenu.getItem(i) == null) {
                upgradeMenu.setItem(i, blueGlassPane);
            }
        }

        player.openInventory(upgradeMenu);
        this.playerLastClickMap.put(player, System.currentTimeMillis());
    }

    private ItemStack createUpgradeItem(String upgradeName, String displayName, Material material, Player player) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            int currentLevel = upgradesHero.getUpgradeLevel(player, upgradeName);
            meta.setDisplayName(ChatColor.GOLD + displayName + " (Уровень: " + currentLevel + ")");
            meta.setLore(new ArrayList<>());
            meta.addItemFlags(ItemFlag.values());

            item.setItemMeta(meta);
        }
        return item;
    }

    private void updateMenu(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory != null && inventory.getTitle().equals(ChatColor.BOLD + "Улучшение персонажа")) {
            inventory.setItem(10, createUpgradeItem("SharpBlades", "Острые лезвия", Material.IRON_SWORD, player));
            inventory.setItem(12, createUpgradeItem("LumberjackMastery", "Мастерство лесоруба", Material.DIAMOND_AXE, player));
            inventory.setItem(14, createUpgradeItem("StrikePrecision", "Точность удара", Material.BOW, player));
            inventory.setItem(16, createUpgradeItem("LumberjackStrength", "Сила лесоруба", Material.IRON_AXE, player));
            inventory.setItem(22, createUpgradeItem("NatureCall", "Зов природы", Material.SAPLING, player));
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (event.getView().getTitle().equals(ChatColor.BOLD + "Улучшение персонажа")) {
                event.setCancelled(true);
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null) {
                    ItemMeta meta = clickedItem.getItemMeta();
                    if (meta != null) {
                        String displayName = meta.getDisplayName();
                        String upgradeName = null;

                        if (displayName != null) {
                            if (displayName.contains("Острые лезвия")) {
                                upgradeName = "SharpBlades";
                            } else if (displayName.contains("Мастерство лесоруба")) {
                                upgradeName = "LumberjackMastery";
                            } else if (displayName.contains("Точность удара")) {
                                upgradeName = "StrikePrecision";
                            } else if (displayName.contains("Сила лесоруба")) {
                                upgradeName = "LumberjackStrength";
                            } else if (displayName.contains("Зов природы")) {
                                upgradeName = "NatureCall";
                            }
                        }

                        if (upgradeName != null) {
                            long currentTime = System.currentTimeMillis();
                            long lastClickTime = this.playerLastClickMap.getOrDefault(player, 0L);
                            if (currentTime - lastClickTime >= CLICK_COOLDOWN_MS) {
                                this.playerLastClickMap.put(player, currentTime);
                                Integer currentLevel = upgradesHero.getUpgradeLevel(player, upgradeName);
                                Integer maxLevel = upgradesHero.getMaxLevels().get(upgradeName);

                                if (currentLevel != null && maxLevel != null) {
                                    if (currentLevel < maxLevel) {
                                        upgradesHero.setUpgradeLevel(player, upgradeName, currentLevel + 1);
                                        player.sendTitle("", ChatColor.GREEN + "Прокачка " + upgradeName + " улучшена до " + (currentLevel + 1) + " уровня.", 10, 70, 20);
                                        // Обновляем меню без его закрытия
                                        updateMenu(player);
                                    } else {
                                        player.sendTitle("", ChatColor.RED + "Прокачка " + upgradeName + " уже на максимальном уровне.", 10, 70, 20);
                                    }
                                } else {
                                    player.sendMessage(ChatColor.RED + "Ошибка: не удалось получить уровень или максимальный уровень для " + upgradeName);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
