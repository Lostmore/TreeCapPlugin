package net.treechopperplugin.Commands;

import net.treechopperplugin.Measurement.SawmillManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SawmillDonateCommand implements CommandExecutor, Listener {

    private final SawmillManager sawmillManager;

    public SawmillDonateCommand(SawmillManager sawmillManager) {
        this.sawmillManager = sawmillManager;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("TreeCapPlugin"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sawmillManager.openDonationInventory(player);
            return true;
        } else {
            sender.sendMessage("§cЭта команда доступна только игрокам.");
            return false;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Пожертвовать ресурсы")) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == null) {
                return;
            }

            if (clickedItem.getType() == Material.EMERALD_BLOCK) {
                player.closeInventory();
                player.sendMessage("§aВы можете пожертвовать ресурсы, помещая их в инвентарь.");
            } else if (clickedItem.getType() == Material.REDSTONE_BLOCK) {
                player.closeInventory();
                player.sendMessage("§cПожертвование отменено.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Пожертвовать ресурсы")) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            boolean hasResources = false;
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    hasResources = true;
                    sawmillManager.handleResourceDonation(player, item);
                }
            }

            if (!hasResources) {
                player.sendMessage("§cУ вас нет ресурсов для пожертвования.");
            }
        }
    }
}
