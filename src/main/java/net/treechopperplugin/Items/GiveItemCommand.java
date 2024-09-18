package net.treechopperplugin.Items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GiveItemCommand implements CommandExecutor, Listener {
    private Items items;

    public GiveItemCommand(Items items) {
        this.items = items;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players.");
            return true;
        }

        Player player = (Player) sender;
        openGiveItemInventory(player);
        return true;
    }

    private void openGiveItemInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 5 * 9, "Give Items");

        List<ItemStack> woodenItems = new ArrayList<>();
        List<ItemStack> stoneItems = new ArrayList<>();
        List<ItemStack> ironItems = new ArrayList<>();
        List<ItemStack> goldItems = new ArrayList<>();
        List<ItemStack> diamondItems = new ArrayList<>();

        for (String itemId : items.getItemIds()) {
            ItemStack item = items.getItem(itemId);
            if (item != null) {
                Material material = item.getType();
                if (material.name().contains("WOOD")) {
                    woodenItems.add(item.clone());
                } else if (material.name().contains("STONE")) {
                    stoneItems.add(item.clone());
                } else if (material.name().contains("IRON")) {
                    ironItems.add(item.clone());
                } else if (material.name().contains("GOLD")) {
                    goldItems.add(item.clone());
                } else if (material.name().contains("DIAMOND")) {
                    diamondItems.add(item.clone());
                }
            }
        }

        int index = 0;
        for (ItemStack item : woodenItems) {
            inv.setItem(index++, item);
        }
        for (ItemStack item : stoneItems) {
            inv.setItem(index++, item);
        }
        for (ItemStack item : ironItems) {
            inv.setItem(index++, item);
        }
        for (ItemStack item : goldItems) {
            inv.setItem(index++, item);
        }
        for (ItemStack item : diamondItems) {
            inv.setItem(index++, item);
        }

        player.openInventory(inv);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equals("Give Items")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                player.getInventory().addItem(clickedItem.clone());
                player.sendMessage("You have been given: " + clickedItem.getType().toString());
            }
        }
    }
}
