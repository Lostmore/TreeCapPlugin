package net.treechopperplugin.GUI;

import java.util.HashMap;
import java.util.Map;

import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.Upgrades.UpgradesAxe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AxeUpgradeMenu implements Listener {
    private final TreeCapPlugin plugin;
    private final UpgradesAxe upgradesAxe;
    private final Map<Player, Long> playerLastClickMap = new HashMap<>();
    private final Map<Player, Boolean> playerInUpgradeProcess = new HashMap<>();
    private final Map<Player, Boolean> playerHasUpgraded = new HashMap<>();
    private static final long CLICK_COOLDOWN_MS = 1000L;

    public AxeUpgradeMenu(TreeCapPlugin plugin) {
        this.plugin = plugin;
        this.upgradesAxe = new UpgradesAxe(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory upgradeMenu = Bukkit.createInventory((InventoryHolder)null, 9, ChatColor.BOLD + "Улучшение топора");
        ItemStack upgradeButton = new ItemStack(Material.ANVIL);
        ItemMeta meta = upgradeButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Улучшить топор");
            upgradeButton.setItemMeta(meta);
        }

        upgradeMenu.setItem(4, upgradeButton);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && itemInHand.getType().toString().endsWith("_AXE")) {
            upgradeMenu.setItem(0, itemInHand.clone());
        }

        ItemStack blueGlassPane;
        if (itemInHand != null && itemInHand.getType().toString().endsWith("_AXE")) {
            blueGlassPane = this.upgradesAxe.getNextLevelAxe(itemInHand);
            if (blueGlassPane != null) {
                upgradeMenu.setItem(8, blueGlassPane);
            }
        }

        blueGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
        ItemMeta paneMeta = blueGlassPane.getItemMeta();
        if (paneMeta != null) {
            paneMeta.setDisplayName(" ");
            blueGlassPane.setItemMeta(paneMeta);
        }

        upgradeMenu.setItem(1, blueGlassPane);
        upgradeMenu.setItem(2, blueGlassPane);
        upgradeMenu.setItem(3, blueGlassPane);
        upgradeMenu.setItem(5, blueGlassPane);
        upgradeMenu.setItem(6, blueGlassPane);
        upgradeMenu.setItem(7, blueGlassPane);
        player.openInventory(upgradeMenu);
        this.playerLastClickMap.put(player, System.currentTimeMillis());
        this.playerInUpgradeProcess.put(player, false);
        this.playerHasUpgraded.put(player, false);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (event.getView().getTitle().equals(ChatColor.BOLD + "Улучшение топора")) {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getRawSlot() == 4 && event.getCurrentItem().getType() == Material.ANVIL) {
                    long currentTime = System.currentTimeMillis();
                    long lastClickTime = (Long) this.playerLastClickMap.getOrDefault(player, 0L);
                    if (currentTime - lastClickTime >= 1000L) {
                        this.playerLastClickMap.put(player, currentTime);
                        if ((Boolean) this.playerInUpgradeProcess.getOrDefault(player, false)) {
                            player.sendTitle("", ChatColor.RED + "Вы уже находитесь в процессе улучшения.", 10, 70, 20);
                        } else {
                            this.playerInUpgradeProcess.put(player, true);
                            ItemStack itemInHand = player.getInventory().getItemInMainHand();
                            if (itemInHand != null && itemInHand.getType().toString().endsWith("_AXE")) {
                                Bukkit.getScheduler().runTask(this.plugin, (Runnable) (() -> {
                                    boolean success = this.upgradesAxe.upgradeAxe(player, itemInHand);
                                    if (success) {
                                        if (!(Boolean) this.playerHasUpgraded.getOrDefault(player, false)) {
                                            player.sendTitle("", ChatColor.GREEN + "Топор успешно улучшен.", 10, 70, 20);
                                            this.playerHasUpgraded.put(player, true);
                                        }
                                    } else {
                                        player.sendTitle("", ChatColor.RED + "Произошла ошибка при улучшении топора.", 10, 70, 20);
                                        this.plugin.getLogger().warning("Failed to upgrade axe for player " + player.getName());
                                    }

                                    Bukkit.getScheduler().runTaskLater(this.plugin, (Runnable) (() -> {
                                        player.closeInventory();
                                        this.playerInUpgradeProcess.put(player, false);
                                    }), 1L);
                                }));
                            } else {
                                player.sendTitle("", ChatColor.RED + "Возьмите топор в руку для улучшения.", 10, 70, 20);
                                Bukkit.getScheduler().runTaskLater(this.plugin, (Runnable) (() -> {
                                    player.closeInventory();
                                    this.playerInUpgradeProcess.put(player, false);
                                }), 1L);
                            }
                        }
                    }
                }
            }
        }
    }
}
