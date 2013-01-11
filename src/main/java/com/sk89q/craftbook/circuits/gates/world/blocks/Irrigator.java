package com.sk89q.craftbook.circuits.gates.world.blocks;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.craftbook.util.VerifyUtil;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class Irrigator extends AbstractIC {

    public Irrigator(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    Location centre;
    int radius;

    @Override
    public void load() {

        centre = BukkitUtil.toSign(getSign()).getLocation();

        try {
            String[] splitEquals = RegexUtil.EQUALS_PATTERN.split(getSign().getLine(2), 2);
            radius = Integer.parseInt(splitEquals[0]);
            if (getSign().getLine(2).contains("=")) {
                String[] splitCoords = RegexUtil.COLON_PATTERN.split(splitEquals[1]);
                int x = Integer.parseInt(splitCoords[0]);
                int y = Integer.parseInt(splitCoords[1]);
                int z = Integer.parseInt(splitCoords[2]);
                if (x > 16) x = 16;
                if (x < -16) x = -16;
                if (y > 16) y = 16;
                if (y < -16) y = -16;
                if (z > 16) z = 16;
                if (z < -16) z = -16;
                centre.add(x, y, z);
            }
        } catch (Exception ignored) {
            radius = 10;
        }

        radius = VerifyUtil.verifyRadius(radius, 15);
    }

    @Override
    public String getTitle() {

        return "Irrigator";
    }

    @Override
    public String getSignTitle() {

        return "IRRIGATOR";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) chip.setOutput(0, irrigate());
    }

    public boolean irrigate() {

        for (int x = -radius + 1; x < radius; x++) {
            for (int y = -radius + 1; y < radius; y++) {
                for (int z = -radius + 1; z < radius; z++) {
                    int rx = centre.getBlockX() - x;
                    int ry = centre.getBlockY() - y;
                    int rz = centre.getBlockZ() - z;
                    Block b = centre.getWorld().getBlockAt(rx, ry, rz);
                    if (b.getTypeId() == BlockID.SOIL && b.getData() < 0x1) {
                        if (consumeWater()) {
                            b.setData((byte) 0x8, false);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean consumeWater() {

        Block chest = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock()).getRelative(0, 1, 0);
        if (chest.getTypeId() == BlockID.CHEST) {
            Chest c = (Chest) chest.getState();
            HashMap<Integer, ItemStack> over = c.getInventory().removeItem(new ItemStack(BlockID.WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(BlockID.STATIONARY_WATER, 1));
            if (over.isEmpty()) return true;
            over = c.getInventory().removeItem(new ItemStack(ItemID.WATER_BUCKET, 1));
            if (over.isEmpty()) {
                c.getInventory().addItem(new ItemStack(ItemID.BUCKET, 1));
                return true;
            }
        }

        return false;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Irrigator(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Irrigates nearby farmland using water in above chest.";
        }

        @Override
        public String getLongDescription() {

            return "The Irrigator IC uses either water blocks or water buckets from the chest above it, and irrigates a block of farmland in the radius specified by line 3. Each farmland block requires either 1 bucket of water or 1 water block to become fully irrigated.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"radius=x:y:z offset", null};
            return lines;
        }
    }
}