package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.ICUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;

public class LiquidFlood extends AbstractSelfTriggeredIC {

    Vector radius;
    String liquid;
    Location centre;

    public LiquidFlood(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Liquid Flooder";
    }

    @Override
    public String getSignTitle() {

        return "LIQUID FLOOD";
    }

    @Override
    public void load() {

        centre = ICUtil.parseBlockLocation(getSign()).getLocation();
        radius = ICUtil.parseRadius(getSign());

        liquid = getSign().getLine(2).equalsIgnoreCase("lava") ? "lava" : "water";
    }

    public void doStuff(ChipState chip) {

        if (chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == 0 || b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER
                                : BlockID.LAVA)) {
                            b.setTypeId(liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER : BlockID
                                    .STATIONARY_LAVA);
                        }
                    }
                }
            }
        } else if (!chip.getInput(0)) {
            for (int x = -radius.getBlockX() + 1; x < radius.getBlockX(); x++) {
                for (int y = -radius.getBlockY() + 1; y < radius.getBlockY(); y++) {
                    for (int z = -radius.getBlockZ() + 1; z < radius.getBlockZ(); z++) {
                        int rx = centre.getBlockX() - x;
                        int ry = centre.getBlockY() - y;
                        int rz = centre.getBlockZ() - z;
                        Block b = BukkitUtil.toSign(getSign()).getWorld().getBlockAt(rx, ry, rz);
                        if (b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.WATER : BlockID.LAVA)
                                || b.getTypeId() == (liquid.equalsIgnoreCase("water") ? BlockID.STATIONARY_WATER :
                                    BlockID.STATIONARY_LAVA)) {
                            b.setTypeId(BlockID.AIR);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void trigger(ChipState chip) {

        doStuff(chip);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        doStuff(state);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LiquidFlood(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"+owater/lava", "+oradius=x:y:z offset"};
            return lines;
        }
    }
}