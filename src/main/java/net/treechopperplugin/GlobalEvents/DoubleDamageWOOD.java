package net.treechopperplugin.GlobalEvents;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DoubleDamageWOOD implements Listener {

    private final JavaPlugin plugin;
    private boolean thunderstormActive = false;

    public DoubleDamageWOOD(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            thunderstormActive = true;
        } else {
            thunderstormActive = false;
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (thunderstormActive) {
            Player player = event.getPlayer();
            Block block = event.getBlock();

            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                event.setExpToDrop((int) (event.getExpToDrop() * 1.0));
                // TODO: ...
            }
        }
    }
}
