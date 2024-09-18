package net.treechopperplugin.Measurement;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.treechopperplugin.GUI.SawmillGUI;
import net.treechopperplugin.TreeCapPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class Village implements CommandExecutor, Listener {
    private final TreeCapPlugin plugin;
    private final HashMap<UUID, Location> playerLocations = new HashMap<>();
    private NPC sawmillNpc;

    public Village(TreeCapPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("village").setExecutor(this);
        plugin.getCommand("return").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadSawmillNpc();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("village")) {
            teleportToVillage(player);
        } else if (label.equalsIgnoreCase("return")) {
            returnToPreviousLocation(player);
        }

        return true;
    }

    private void teleportToVillage(Player player) {
        playerLocations.put(player.getUniqueId(), player.getLocation());
        World villageWorld = Bukkit.getWorld("village");
        if (villageWorld == null) {
            villageWorld = Bukkit.createWorld(new WorldCreator("village_world"));
        }
        Location villageLocation = new Location(villageWorld, 54, 73, -142);
        player.teleport(villageLocation);
        player.sendMessage("§aYou have been teleported to the village!");
    }

    @EventHandler
    public void onPlayerInteractWithNPC(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof org.bukkit.entity.Villager)) return;

        Player player = event.getPlayer();
        NPC clickedNPC = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());

        if (clickedNPC != null && clickedNPC.getId() == 25) {
            SawmillGUI.openSawmillGUI(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Sawmill")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) return;

            String displayName = clickedItem.getItemMeta().getDisplayName();
            Player player = (Player) event.getWhoClicked();

            if (displayName.equals("§aConfirm")) {
                player.sendMessage("§aВы подтвердили пожертвование!");
                player.closeInventory();
            } else if (displayName.equals("§cCancel")) {
                player.sendMessage("§cВы отменили пожертвование.");
                player.closeInventory();
            }
        }
    }

    private void returnToPreviousLocation(Player player) {
        Location returnLocation = playerLocations.get(player.getUniqueId());
        if (returnLocation != null) {
            player.teleport(returnLocation);
            player.sendMessage("§aYou have been returned to your previous location!");
        } else {
            player.sendMessage("§cCould not find your previous location.");
        }
    }

    private void loadSawmillNpc() {
        sawmillNpc = CitizensAPI.getNPCRegistry().getById(25);
        if (sawmillNpc == null) {
            Bukkit.getLogger().warning("NPC with ID 25 not found!");
        }
    }
}
