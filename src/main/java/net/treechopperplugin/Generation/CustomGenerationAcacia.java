package net.treechopperplugin.Generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomGenerationAcacia {

    private final World world;
    private final Location corner1;
    private final Location corner2;
    private final int treesToGeneratePerInterval;
    private BukkitRunnable treeGenerationTask;

    public CustomGenerationAcacia(World world, Location corner1, Location corner2, int treesToGeneratePerInterval) {
        this.world = world;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.treesToGeneratePerInterval = treesToGeneratePerInterval;

        // startTreeGenerationTask();
    }

    public CustomGenerationAcacia(World world, GenerationArea area, int treesToGeneratePerInterval) {
        this(world, area.getCorner1(), area.getCorner2(), treesToGeneratePerInterval);
    }

    private void startTreeGenerationTask() {
        treeGenerationTask = new BukkitRunnable() {
            @Override
            public void run() {
                generateAcaciaTrees(treesToGeneratePerInterval);
            }
        };
        treeGenerationTask.runTaskTimer(Bukkit.getPluginManager().getPlugin("TreeCapPlugin"), 0L, 6000L);
    }

    public void generateAcaciaTrees(int treeCount) {
        final int[] generatedTrees = {0};

        for (int i = 0; i < treeCount; i++) {
            Location randomLocation = getRandomLocationInArea();
            Block blockBelow = randomLocation.getBlock().getRelative(BlockFace.DOWN);

            if (isSuitableForTree(blockBelow)) {
                Block saplingBlock = blockBelow.getRelative(BlockFace.UP);

                saplingBlock.setType(Material.SAPLING);
                saplingBlock.setData((byte) 4);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        saplingBlock.setType(Material.AIR);

                        if (world.generateTree(randomLocation, TreeType.ACACIA)) {
                            generatedTrees[0]++;
                            Bukkit.getLogger().info("Acacia tree generated at: " + randomLocation.toString());
                        } else {
                            Bukkit.getLogger().warning("Failed to generate Acacia tree at: " + randomLocation.toString());
                        }
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugin("TreeCapPlugin"), 60L);

            } else {
                Bukkit.getLogger().warning("Location not suitable for tree: " + randomLocation.toString());
            }
        }
        Bukkit.getLogger().info(generatedTrees[0] + " Acacia trees were successfully generated in the selected area!");
    }

    private Location getRandomLocationInArea() {
        double x = getRandomCoordinate(corner1.getX(), corner2.getX());
        double z = getRandomCoordinate(corner1.getZ(), corner2.getZ());
        int y = world.getHighestBlockYAt((int) x, (int) z);
        return new Location(world, x, y, z);
    }

    private double getRandomCoordinate(double min, double max) {
        return min + (max - min) * Math.random();
    }

    private boolean isSuitableForTree(Block blockBelow) {
        Material type = blockBelow.getType();
        return type == Material.GRASS || type == Material.DIRT || type == Material.SAND;
    }
}
