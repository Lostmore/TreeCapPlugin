package net.treechopperplugin.Commands;

import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.Upgrades.UpgradesHero;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class UpgradeHeroAdminCommand implements CommandExecutor {

    private final UpgradesHero upgradesHero;
    private final TreeCapPlugin plugin;

    public UpgradeHeroAdminCommand(TreeCapPlugin plugin, UpgradesHero upgradesHero) {
        this.plugin = plugin;
        this.upgradesHero = upgradesHero;
        this.plugin.getCommand("setuphero").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может выполнять только игрок.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage("Используйте команду: /setuphero <название прокачки> <уровень>");
            player.sendMessage("Доступные прокачки: sharpblades, lumberjackmastery, strikeprecision, lumberjackstrength, naturecall.");
            return false;
        }

        String upgradeName = args[0].toLowerCase();
        int level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("Уровень должен быть числом.");
            return false;
        }

        String formattedUpgradeName = formatUpgradeName(upgradeName);

        Map<String, Integer> maxLevels = upgradesHero.getMaxLevels();
        if (!maxLevels.containsKey(formattedUpgradeName)) {
            player.sendMessage("Неизвестная прокачка: " + upgradeName);
            player.sendMessage("Доступные прокачки: " + String.join(", ", maxLevels.keySet()));
            return false;
        }

        int maxLevel = maxLevels.get(formattedUpgradeName);
        if (level < 1 || level > maxLevel) {
            player.sendMessage("Уровень должен быть от 1 до " + maxLevel + " для " + formattedUpgradeName + ".");
            return false;
        }

        if ("NatureCall".equalsIgnoreCase(formattedUpgradeName) && level != 1) {
            player.sendMessage("NatureCall может быть только уровня 1.");
            return false;
        }

        upgradesHero.setUpgradeLevel(player, formattedUpgradeName, level);
        // player.sendMessage("Уровень " + formattedUpgradeName + " установлен на " + level + ".");
        return true;
    }

    private String formatUpgradeName(String upgradeName) {
        switch (upgradeName) {
            case "sharpblades":
                return "SharpBlades";
            case "lumberjackmastery":
                return "LumberjackMastery";
            case "strikeprecision":
                return "StrikePrecision";
            case "lumberjackstrength":
                return "LumberjackStrength";
            case "naturecall":
                return "NatureCall";
            default:
                return null;
        }
    }
}
