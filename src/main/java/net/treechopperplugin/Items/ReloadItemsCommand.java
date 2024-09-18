package net.treechopperplugin.Items;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadItemsCommand implements CommandExecutor {
    private Items items;

    public ReloadItemsCommand(Items items) {
        this.items = items;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.items.reloadItemsConfig();
        sender.sendMessage("Items configuration reloaded and items updated.");
        return true;
    }
}
    