package net.treechopperplugin.Commands;

import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.GUI.AxeUpgradeMenu;
import net.treechopperplugin.Upgrades.UpgradesAxe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UpgradeCommand implements CommandExecutor {
    private final TreeCapPlugin plugin;

    public UpgradeCommand(TreeCapPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null && itemInHand.getType().toString().endsWith("_AXE")) {
                UpgradesAxe upgradesAxe = new UpgradesAxe(this.plugin);
                if (upgradesAxe.isMaxLevel(itemInHand)) {
                    player.sendMessage("§cВаш топор уже достиг максимального уровня.");
                    return true;
                }

                AxeUpgradeMenu axeUpgradeMenu = new AxeUpgradeMenu(this.plugin);
                axeUpgradeMenu.openMenu(player);
                return true;
            }

            player.sendMessage("§cВы должны держать топор в руке, чтобы его улучшить.");
        } else {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
        }

        return false;
    }
}