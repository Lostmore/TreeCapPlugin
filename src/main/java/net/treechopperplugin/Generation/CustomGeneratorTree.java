package net.treechopperplugin.Generation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class CustomGeneratorTree implements Listener {

    private final World world;
    private final Random random = new Random();

    public CustomGeneratorTree(World world) {
        this.world = world;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("TreeCapPlugin"));
    }

    public void plantTree(Block location) {
        if (location.getType() != Material.FLOWER_POT) {
            return;
        }
        location.setType(Material.AIR);

        new BukkitRunnable() {
            @Override
            public void run() {
                generateTree(location.getLocation());
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("TreeCapPlugin"), 100L);
    }

    private void generateTree(Location location) {
        int trunkHeight = 5;
        for (int y = 0; y < trunkHeight; y++) {
            Block block = location.clone().add(0, y, 0).getBlock();
            block.setType(Material.CONCRETE);
            block.setData((byte) 4);
        }
        Block base = location.clone().add(0, trunkHeight - 1, 0).getBlock();
        int leafRadius = 4; // Радиус листвы

        for (int x = -leafRadius; x <= leafRadius; x++) {
            for (int z = -leafRadius; z <= leafRadius; z++) {
                for (int y = -leafRadius; y <= leafRadius; y++) {
                    double distance = Math.sqrt(x * x + z * z + y * y);
                    if (distance <= leafRadius) {
                        Block leafBlock = base.getRelative(x, y, z);
                        if (leafBlock.getType() == Material.AIR && shouldPlaceLeaf(x, y, z, leafRadius)) {
                            leafBlock.setType(Material.LEAVES);
                        }
                    }
                }
            }
        }
        for (int x = -leafRadius; x <= leafRadius; x++) {
            for (int z = -leafRadius; z <= leafRadius; z++) {
                if (Math.sqrt(x * x + z * z) <= leafRadius) {
                    Block leafBlock = base.getRelative(x, 1, z);
                    if (leafBlock.getType() == Material.AIR) {
                        leafBlock.setType(Material.LEAVES);
                    }
                }
            }
        }
    }

    private boolean shouldPlaceLeaf(int x, int y, int z, int radius) {
        double distance = Math.sqrt(x * x + y * y + z * z);
        double chance = 0.7;

        if (distance > radius * 0.8 || distance < radius * 0.3) {
            chance *= 0.5;
        }

        if (y == 0) {
            chance *= 1.2;
        }
        if (y > 1 && distance < radius * 0.5) {
            chance *= 0.8;
        }
        if (x == 0 && z == 0 && y > 1) {
            chance *= 0.5;
        }

        return random.nextDouble() < chance;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null && block.getType() == Material.FLOWER_POT) {
                ItemStack item = event.getItem();
                if (item != null && item.getType() == Material.INK_SACK && item.getDurability() == 15) {
                    plantTree(block);
                    item.setAmount(item.getAmount() - 1);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CONCRETE && block.getData() == 4) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

            if (item.getType().name().endsWith("_PICKAXE")) {
                event.setDropItems(false);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.CONCRETE, 1, (short) 4));
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage("Ваш инструмент не подходит.");
            }
        }
    }
}
