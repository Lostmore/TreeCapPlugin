package net.treechopperplugin.GUI;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SawmillGUI {

    public static void openSawmillGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Sawmill");

        ItemStack confirm = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta confirmMeta = confirm.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.setDisplayName("§aConfirm");
            confirm.setItemMeta(confirmMeta);
        }

        ItemStack cancel = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta cancelMeta = cancel.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName("§cCancel");
            cancel.setItemMeta(cancelMeta);
        }

        inv.setItem(3, confirm);
        inv.setItem(5, cancel);

        player.openInventory(inv);
    }
}