package net.treechopperplugin.Listener;

import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class NBTListener implements Listener {

    private final JavaPlugin plugin;

    public NBTListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null) {
            ItemStack item = event.getItem();
            net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

            if (nmsItem.hasTag()) {
                NBTTagCompound tag = nmsItem.getTag();
                tag.remove("");
                nmsItem.setTag(tag);
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, player.getInventory().getHeldItemSlot(), nmsItem));
            }
        }
    }
}
