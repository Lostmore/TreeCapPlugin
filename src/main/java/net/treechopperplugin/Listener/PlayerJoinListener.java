package net.treechopperplugin.Listener;

import net.treechopperplugin.Items.Items;
import net.treechopperplugin.Upgrades.UpgradesHero;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final Items items;
    private final UpgradesHero upgradesHero;

    public PlayerJoinListener(Items items, UpgradesHero upgradesHero) {
        this.items = items;
        this.upgradesHero = upgradesHero;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        items.updatePlayerInventory(event.getPlayer());
        upgradesHero.loadUpgradesFromFile(event.getPlayer());
    }
}
