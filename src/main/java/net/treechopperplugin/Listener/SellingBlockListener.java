package net.treechopperplugin.Listener;

import net.milkbowl.vault.economy.Economy;
import net.treechopperplugin.Measurement.HologramProgress;
import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellingBlockListener implements Listener {

    private final Economy economy;
    private final FileConfiguration blockPriceConfig;

    public SellingBlockListener(TreeCapPlugin plugin) {
        this.economy = plugin.getEconomy();
        this.blockPriceConfig = TreeCapPlugin.getInstance().getBlockPriceConfig();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        HologramProgress hologram = HologramProgress.getHologramById(2);
        if (hologram == null || hologram.getLocation().distance(player.getLocation()) > 2) return;

        Inventory inventory = player.getInventory();
        double totalSaleAmount = 0;

        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR) continue;

            Material blockType = item.getType();
            int amount = item.getAmount();

            double pricePerBlock = blockPriceConfig.getDouble("blocks." + blockType.name(), -1);
            if (pricePerBlock > 0) {
                totalSaleAmount += pricePerBlock * amount;

                inventory.remove(item);
            }
        }

        if (totalSaleAmount > 0) {
            economy.depositPlayer(player, totalSaleAmount);
            player.sendMessage("§aВы продали блоки на сумму " + totalSaleAmount + " монет.");
        } else {
            player.sendMessage("§cУ вас нет блоков, которые можно продать!");
        }
    }
}
