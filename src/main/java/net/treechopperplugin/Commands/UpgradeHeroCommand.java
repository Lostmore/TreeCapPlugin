package net.treechopperplugin.Commands;

import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.GUI.CharacterUpgradeMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpgradeHeroCommand implements CommandExecutor {
    private final TreeCapPlugin plugin;
    private final CharacterUpgradeMenu characterUpgradeMenu;

    public UpgradeHeroCommand(TreeCapPlugin plugin, CharacterUpgradeMenu characterUpgradeMenu) {
        this.plugin = plugin;
        this.characterUpgradeMenu = characterUpgradeMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            characterUpgradeMenu.openMenu(player);
            return true;
        } else {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
            return false;
        }
    }
}
