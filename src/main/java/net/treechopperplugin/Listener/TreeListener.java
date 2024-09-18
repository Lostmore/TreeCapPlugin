package net.treechopperplugin.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.treechopperplugin.TreeCapPlugin;
import net.treechopperplugin.Upgrades.UpgradesHero;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class TreeListener implements Listener {
    private final TreeCapPlugin plugin;
    private final UpgradesHero upgradesHero;
    private final Map<Player, Block> playerCurrentTree = new HashMap();
    private final Map<Player, Integer> playerHitCount = new HashMap();
    private final Map<Block, Byte> lastLogBlockData = new HashMap();
    private final Map<Block, Boolean> protectedSaplings = new HashMap();
    private final Random random = new Random();
    private final Logger logger;

    public TreeListener(TreeCapPlugin plugin, UpgradesHero upgradesHero) {
        this.plugin = plugin;
        this.upgradesHero = upgradesHero;
        this.logger = plugin.getLogger();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (block.getType() == Material.SAPLING) {
            event.setCancelled(true);
        } else if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
            if (!this.isBottomLog(block)) {
                event.setCancelled(true);
                return;
            }

            if (itemInHand == null || !itemInHand.getType().toString().endsWith("_AXE")) {
                event.setCancelled(true);
                return;
            }

            Block treeRoot = this.getTreeRoot(block);
            if (treeRoot == null) {
                return;
            }

            if (this.playerCurrentTree.containsKey(player) && !((Block) this.playerCurrentTree.get(player)).equals(treeRoot)) {
                this.playerHitCount.put(player, 0);
            }

            this.playerCurrentTree.put(player, treeRoot);
            this.lastLogBlockData.put(block, block.getData());
            event.setCancelled(true);
            int hits = (Integer) this.playerHitCount.getOrDefault(player, 0);
            double randomProgressIncrease = (double) (5 + this.random.nextInt(11));
            double precisionHitBonus = this.upgradesHero.getStrikePrecisionBonus();
            double criticalHitChance = precisionHitBonus / 100.0D;
            boolean criticalHit = this.random.nextDouble() < criticalHitChance;
            double sharpBladesBonus;
            double progressPercentage;
            if (criticalHit) {
                sharpBladesBonus = this.upgradesHero.getLumberjackStrengthBonus();
                progressPercentage = sharpBladesBonus / 100.0D * randomProgressIncrease;
                randomProgressIncrease += progressPercentage;
            }

            hits = (int) ((double) hits + randomProgressIncrease);
            sharpBladesBonus = this.upgradesHero.getSharpBladesBonus();
            if (sharpBladesBonus > 0.0D) {
                progressPercentage = randomProgressIncrease * (sharpBladesBonus / 100.0D);
                hits = (int) ((double) hits + progressPercentage);
            }

            progressPercentage = Math.min(100.0D, (double) hits);
            if (hits >= 100) {
                this.playerHitCount.put(player, 0);
                Set<Block> connectedBlocks = this.findConnectedLogBlocks(treeRoot);
                ItemStack[] drops;
                int var23;
                if (connectedBlocks.size() == 2) {
                    Iterator var20 = connectedBlocks.iterator();

                    while (var20.hasNext()) {
                        Block logBlock = (Block) var20.next();
                        drops = (ItemStack[]) ((ItemStack[]) logBlock.getDrops(itemInHand).toArray(new ItemStack[0]));
                        drops = this.applyUpgradeBonuses(player, drops);
                        ItemStack[] var22 = drops;
                        var23 = drops.length;

                        for (int var24 = 0; var24 < var23; ++var24) {
                            ItemStack drop = var22[var24];
                            player.getInventory().addItem(new ItemStack[]{drop});
                        }

                        logBlock.setType(Material.AIR);
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "100%"));
                } else {
                    Block topBlock = this.getTopLogBlock(connectedBlocks);
                    if (topBlock != null) {
                        drops = (ItemStack[]) ((ItemStack[]) topBlock.getDrops(itemInHand).toArray(new ItemStack[0]));
                        drops = this.applyUpgradeBonuses(player, drops);
                        ItemStack[] var28 = drops;
                        int var30 = drops.length;

                        for (var23 = 0; var23 < var30; ++var23) {
                            ItemStack drop = var28[var23];
                            player.getInventory().addItem(new ItemStack[]{drop});
                        }

                        topBlock.setType(Material.AIR);
                    }

                    Iterator var29 = connectedBlocks.iterator();

                    while (var29.hasNext()) {
                        Block bottomLog = (Block) var29.next();
                        Byte logData = (Byte) this.lastLogBlockData.get(bottomLog);
                        if (logData != null) {
                            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                                if (bottomLog.getType() == Material.AIR) {
                                    this.plantSapling(bottomLog, logData);
                                }

                            }, 1200L);
                        }
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "100%"));
                }
            } else {
                this.playerHitCount.put(player, hits);
                ChatColor color = progressPercentage < 30.0D ? ChatColor.RED : (progressPercentage < 70.0D ? ChatColor.GOLD : ChatColor.GREEN);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(color + String.format("%.1f%%", progressPercentage)));
            }
        }

    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.LEAVES) {
            event.setCancelled(true);
        }

    }

    private boolean isBottomLog(Block block) {
        Block belowBlock = block.getRelative(0, -1, 0);
        return belowBlock.getType() != Material.LOG && belowBlock.getType() != Material.LOG_2;
    }

    private Block getTreeRoot(Block block) {
        Set<Block> visitedBlocks = new HashSet();
        return this.findTreeRoot(block, visitedBlocks);
    }

    private Block findTreeRoot(Block block, Set<Block> visitedBlocks) {
        if (block != null && !visitedBlocks.contains(block)) {
            visitedBlocks.add(block);
            Block belowBlock = block.getRelative(0, -1, 0);
            return this.isLogBlock(belowBlock) ? this.findTreeRoot(belowBlock, visitedBlocks) : block;
        } else {
            return null;
        }
    }

    private boolean isLogBlock(Block block) {
        return block.getType() == Material.LOG || block.getType() == Material.LOG_2;
    }

    private Set<Block> findConnectedLogBlocks(Block block) {
        Set<Block> logBlocks = new HashSet();
        Set<Block> visitedBlocks = new HashSet();
        this.exploreLogBlocks(block, logBlocks, visitedBlocks);
        return logBlocks;
    }

    private void exploreLogBlocks(Block block, Set<Block> logBlocks, Set<Block> visitedBlocks) {
        if (block != null && !visitedBlocks.contains(block) && this.isLogBlock(block)) {
            visitedBlocks.add(block);
            logBlocks.add(block);

            for (int dx = -1; dx <= 1; ++dx) {
                for (int dy = -1; dy <= 1; ++dy) {
                    for (int dz = -1; dz <= 1; ++dz) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            this.exploreLogBlocks(block.getRelative(dx, dy, dz), logBlocks, visitedBlocks);
                        }
                    }
                }
            }
        }

    }

    private Block findBottomLog(Set<Block> connectedBlocks) {
        Block bottomBlock = null;
        Iterator var3 = connectedBlocks.iterator();

        while (true) {
            Block block;
            do {
                if (!var3.hasNext()) {
                    return bottomBlock;
                }

                block = (Block) var3.next();
            } while (bottomBlock != null && block.getY() >= bottomBlock.getY());

            bottomBlock = block;
        }
    }

    private void breakTree(final Player player, Block rootBlock, final ItemStack itemInHand) {
        final Set<Block> logBlocks = this.findConnectedLogBlocks(rootBlock);
        Iterator iterator = logBlocks.iterator();

        while (iterator.hasNext()) {
            final Block logBlock = (Block) iterator.next();
            (new BukkitRunnable() {
                public void run() {
                    ItemStack[] drops = (ItemStack[]) ((ItemStack[]) logBlock.getDrops(itemInHand).toArray(new ItemStack[0]));
                    drops = TreeListener.this.applyUpgradeBonuses(player, drops);
                    ItemStack[] var2 = drops;
                    int var3 = drops.length;

                    for (int var4 = 0; var4 < var3; ++var4) {
                        ItemStack drop = var2[var4];
                        player.getInventory().addItem(new ItemStack[]{drop});
                    }

                    logBlock.setType(Material.AIR);
                }
            }).runTaskLater(this.plugin, 1L);
        }

        (new BukkitRunnable() {
            public void run() {
                TreeListener.this.checkForSaplingPlanting(logBlocks);
            }
        }).runTaskLater(this.plugin, 20L);
    }

    private void checkForSaplingPlanting(Set<Block> logBlocks) {
        final Block bottomLog = this.findBottomLog(logBlocks);
        if (bottomLog != null) {
            final Byte logData = (Byte) this.lastLogBlockData.get(bottomLog);
            if (logData != null) {
                (new BukkitRunnable() {
                    public void run() {
                        if (bottomLog.getType() == Material.AIR) {
                            TreeListener.this.plantSapling(bottomLog, logData);
                        }

                    }
                }).runTaskLater(this.plugin, 100L);
            }
        }

    }

    private void plantSapling(Block block, byte logData) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            final Material saplingType;
            byte data;
            switch (logData) {
                case 0:
                    saplingType = Material.SAPLING;
                    data = 0;
                    break;
                case 1:
                    saplingType = Material.SAPLING;
                    data = 1;
                    break;
                case 2:
                    saplingType = Material.SAPLING;
                    data = 2;
                    break;
                case 3:
                    saplingType = Material.SAPLING;
                    data = 3;
                    break;
                case 4:
                    saplingType = Material.SAPLING;
                    data = 4;
                    break;
                case 5:
                    saplingType = Material.SAPLING;
                    data = 5;
                    break;
                default:
                    return;
            }

            Block belowBlock = block.getRelative(0, -1, 0);
            if (belowBlock.getType() == Material.DIRT || belowBlock.getType() == Material.GRASS) {
                block.setType(saplingType);
                block.setData(data);
                (new BukkitRunnable() {
                    public void run() {
                        if (block.getType() == saplingType) {
                            TreeListener.this.growTree(block);
                        }

                    }
                }).runTaskLater(this.plugin, 200L);
            }

        });
    }

    private Block getTopLogBlock(Set<Block> connectedBlocks) {
        Block topBlock = null;
        Iterator iterator = connectedBlocks.iterator();

        while (true) {
            Block block;
            do {
                if (!iterator.hasNext()) {
                    return topBlock;
                }

                block = (Block) iterator.next();
            } while (topBlock != null && block.getY() <= topBlock.getY());

            topBlock = block;
        }
    }

    private void growTree(Block block) {
        TreeType treeType;
        switch (block.getData()) {
            case 0:
                treeType = TreeType.TREE;
                break;
            case 1:
                treeType = TreeType.REDWOOD;
                break;
            case 2:
                treeType = TreeType.BIRCH;
                break;
            case 3:
                treeType = TreeType.SMALL_JUNGLE;
                break;
            case 4:
                treeType = TreeType.ACACIA;
                break;
            case 5:
                treeType = TreeType.DARK_OAK;
                break;
            default:
                return;
        }

        block.setType(Material.AIR);
        block.getWorld().generateTree(block.getLocation(), treeType);
    }

    private ItemStack[] applyCriticalDamage(ItemStack[] drops, double criticalDamageBonus) {
        List<ItemStack> modifiedDrops = new ArrayList();
        ItemStack[] var5 = drops;
        int var6 = drops.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            ItemStack drop = var5[var7];
            if (drop != null) {
                ItemStack critDrop = drop.clone();
                critDrop.setAmount((int) ((double) critDrop.getAmount() * (1.0D + criticalDamageBonus)));
                modifiedDrops.add(critDrop);
            }
        }

        return (ItemStack[]) ((ItemStack[]) modifiedDrops.toArray(new ItemStack[0]));
    }

    private ItemStack[] applyUpgradeBonuses(Player player, ItemStack[] drops) {
        List<ItemStack> modifiedDrops = new ArrayList();
        double lumberjackMasteryBonus = this.upgradesHero.getLumberjackMasteryBonus() / 100.0D;
        boolean criticalHit = this.upgradesHero.isCriticalHit();
        double criticalDamageBonus = this.upgradesHero.getLumberjackStrengthBonus() / 100.0D;
        ItemStack[] var9 = drops;
        int var10 = drops.length;

        ItemStack critDrop;
        for (int var11 = 0; var11 < var10; ++var11) {
            critDrop = var9[var11];
            if (critDrop != null) {
                modifiedDrops.add(critDrop.clone());
                if (Math.random() < lumberjackMasteryBonus) {
                    ItemStack additionalDrop = critDrop.clone();
                    additionalDrop.setAmount(critDrop.getAmount());
                    modifiedDrops.add(additionalDrop);
                }
            }
        }

        if (criticalHit) {
            List<ItemStack> critDrops = new ArrayList();
            Iterator var14 = modifiedDrops.iterator();

            while (var14.hasNext()) {
                ItemStack drop = (ItemStack) var14.next();
                if (drop != null) {
                    critDrop = drop.clone();
                    critDrop.setAmount((int) ((double) critDrop.getAmount() * (1.0D + criticalDamageBonus)));
                    critDrops.add(critDrop);
                }
            }

            modifiedDrops.addAll(critDrops);
        }

        return (ItemStack[]) ((ItemStack[]) modifiedDrops.toArray(new ItemStack[0]));
    }
}