package net.treechopperplugin.Commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.configuration.file.FileConfiguration;

public class NPCSaveCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    public NPCSaveCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может выполнять только игрок.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("Пожалуйста, укажите ID NPC.");
            return false;
        }

        int npcId;
        try {
            npcId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("ID NPC должен быть числом.");
            return false;
        }

        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
        if (npc == null) {
            player.sendMessage("NPC с таким ID не найден.");
            return false;
        }

        Location loc = npc.getStoredLocation();
        FileConfiguration config = plugin.getConfig();

        String path = "npcs." + npcId;
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());

        plugin.saveConfig();
        player.sendMessage("Позиция NPC сохранена.");

        return true;
    }
}

