package net.treechopperplugin.Commands;

import net.treechopperplugin.Measurement.HologramProgress;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class HologramCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("Используйте: /hogo list, /hogo setprogress <ID> <значение>, /hogo delete <ID>, /hogo create [-i] [x y z] или /hogo reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            player.sendMessage("Список голограмм:");
            for (Map.Entry<Integer, HologramProgress> entry : HologramProgress.getAllHolograms().entrySet()) {
                player.sendMessage("ID: " + entry.getKey() + " - Местоположение: " + entry.getValue().getLocation().toString());
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("setprogress") && args.length == 3) {
            try {
                int id = Integer.parseInt(args[1]);
                int percent = Integer.parseInt(args[2]);

                if (percent < 0 || percent > 100) {
                    player.sendMessage("Процент должен быть в диапазоне от 0 до 100.");
                    return true;
                }

                HologramProgress hologram = HologramProgress.getHologramById(id);
                if (hologram != null) {
                    int totalResources = hologram.getTotalResourcesRequired();
                    int newResources = (int) ((percent / 100.0) * totalResources);
                    hologram.setCurrentResources(newResources);
                    player.sendMessage("Прогресс для голограммы с ID " + id + " установлен на " + percent + "% (" + newResources + " ресурсов)");
                } else {
                    player.sendMessage("Голограмма с ID " + id + " не найдена.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("ID и процент должны быть числами.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
            try {
                int id = Integer.parseInt(args[1]);

                HologramProgress hologram = HologramProgress.getHologramById(id);
                if (hologram != null) {
                    hologram.remove();
                    HologramProgress.getAllHolograms().remove(id);
                    hologram.saveProgress();
                    player.sendMessage("Голограмма с ID " + id + " была успешно удалена.");
                } else {
                    player.sendMessage("Голограмма с ID " + id + " не найдена.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("ID должен быть числом.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            Location location;

            if (args.length == 2 && args[1].equalsIgnoreCase("-i")) {
                location = player.getLocation();
            } else if (args.length == 4) {
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    location = new Location(player.getWorld(), x, y, z);
                } catch (NumberFormatException e) {
                    player.sendMessage("Координаты должны быть числами.");
                    return true;
                }
            } else {
                player.sendMessage("Используйте: /hogo create [-i] [x y z]");
                return true;
            }

            int totalResources = 100;
            HologramProgress hologram = new HologramProgress(location, totalResources);
            player.sendMessage("Создана новая голограмма с ID " + hologram.getId() + " в локации " + location.toString());
            hologram.saveProgress();
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            HologramProgress.reloadAllHolograms();
            player.sendMessage("Все голограммы были перезагружены.");
            return true;
        }

        player.sendMessage("Неверная команда. Используйте: /hogo list\n /hogo setprogress <ID> <значение>\n /hogo delete <ID>\n /hogo create [-i] [x y z]\n /hogo reload");
        return true;
    }
}
