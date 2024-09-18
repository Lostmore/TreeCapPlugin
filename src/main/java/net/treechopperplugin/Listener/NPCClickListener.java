package net.treechopperplugin.Listener;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCClickListener implements Listener {
    private final TreeCapPlugin plugin;

    public NPCClickListener(TreeCapPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(NPCClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();

        plugin.getLogger().info("NPC clicked with ID: " + npc.getId());

        if (npc.getId() == 2) {
            player.performCommand("upgradehero");
            plugin.getLogger().info("Command executed for player: " + player.getName());
        }
    }
}
