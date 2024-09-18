package net.treechopperplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class FastLeafDecay implements Listener {
    private final List<Block> scheduledBlocks = new ArrayList();
    private static final List<BlockFace> NEIGHBORS;
    private static final int PLAYER_PLACED_BIT = 4;

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onBlockBreak(BlockBreakEvent event) {
        this.onBlockRemove(event.getBlock(), 5L);
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onLeavesDecay(LeavesDecayEvent event) {
        this.onBlockRemove(event.getBlock(), 2L);
    }

    private boolean isLog(Material mat) {
        return mat == Material.LOG || mat == Material.LOG_2;
    }

    private boolean isLeaf(Material mat) {
        return mat == Material.LEAVES || mat == Material.LEAVES_2;
    }

    private boolean isYellowConcrete(Block block) {
        return block.getType() == Material.CONCRETE && block.getData() == 4;
    }

    private boolean isPersistent(Block block) {
        return (block.getData() & 4) != 0;
    }

    private int getDistance(Block block) {
        List<Block> todo = new ArrayList();
        todo.add(block);
        Set<Block> done = new HashSet();
        Map<Block, Integer> distances = new HashMap();
        distances.put(block, 0);

        while(!todo.isEmpty()) {
            Block current = (Block)todo.remove(0);
            done.add(current);
            int distance = (Integer)distances.get(current);
            if (distance >= 5) {
                return distance;
            }

            BlockFace[] var7 = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
            int var8 = var7.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                BlockFace face = var7[var9];
                Block nbor = current.getRelative(face);
                if (!done.contains(nbor)) {
                    if (this.isLog(nbor.getType())) {
                        return distance + 1;
                    }
                    if (nbor.getType() == Material.CONCRETE && nbor.getData() == 4) {
                        return distance + 1;
                    }
                    if (this.isLeaf(nbor.getType())) {
                        todo.add(nbor);
                        distances.put(nbor, distance + 1);
                    }
                }
            }
        }

        return 5;
    }

    private void onBlockRemove(Block oldBlock, long delay) {
        if (this.isLog(oldBlock.getType()) || this.isLeaf(oldBlock.getType()) || this.isYellowConcrete(oldBlock)) {
            Iterator var4 = NEIGHBORS.iterator();

            while(var4.hasNext()) {
                BlockFace neighborFace = (BlockFace)var4.next();
                Block block = oldBlock.getRelative(neighborFace);
                if (this.isLeaf(block.getType()) && !this.isPersistent(block) && !this.scheduledBlocks.contains(block)) {
                    this.scheduledBlocks.add(block);
                    Bukkit.getScheduler().runTaskLater(TreeCapPlugin.getInstance(), () -> {
                        this.decay(block);
                    }, delay);
                }
            }

        }
    }

    private void decay(final Block block) {
        if (this.scheduledBlocks.remove(block)) {
            if (this.isLeaf(block.getType())) {
                if (!this.isPersistent(block)) {
                    if (this.getDistance(block) > 4) {
                        LeavesDecayEvent event = new LeavesDecayEvent(block);
                        Bukkit.getPluginManager().callEvent(event);
                        if (!event.isCancelled()) {
                            block.getWorld().spawnParticle(Particle.BLOCK_DUST, block.getLocation().add(0.5D, 0.5D, 0.5D), 8, 0.2D, 0.2D, 0.2D, 0.0D, block.getType().getNewData(block.getData()));
                            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 0.05F, 1.2F);
                            (new BukkitRunnable() {
                                public void run() {
                                    if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
                                        block.setType(Material.AIR);
                                    }

                                }
                            }).runTaskLater(TreeCapPlugin.getInstance(), 60L);
                        }
                    }
                }
            }
        }
    }
    static {
        NEIGHBORS = Arrays.asList(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN);
    }
}