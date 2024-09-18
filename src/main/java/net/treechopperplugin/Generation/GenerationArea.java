package net.treechopperplugin.Generation;

import org.bukkit.Location;

public class GenerationArea {
    private final Location corner1;
    private final Location corner2;
    private final String generationType;
    private final int height;

    public GenerationArea(Location corner1, Location corner2, String generationType) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.generationType = generationType;
        this.height = corner1.getBlockY();
    }

    public Location getCorner1() {
        return corner1;
    }

    public Location getCorner2() {
        return corner2;
    }

    public String getGenerationType() {
        return generationType;
    }

    public int getHeight() {
        return height;
    }
}
