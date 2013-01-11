// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.circuits.gates.world.blocks;

import org.bukkit.Server;
import org.bukkit.block.Block;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;

public class MultipleSetBlock extends AbstractIC {

    public MultipleSetBlock(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    int x, y, z;

    int block;
    byte data;

    String[] dim;

    @Override
    public void load() {

        String line3 = getSign().getLine(2).toUpperCase();
        String line4 = getSign().getLine(3);

        String[] coords;
        coords = RegexUtil.COLON_PATTERN.split(RegexUtil.PLUS_PATTERN.matcher(line3).replaceAll(""));

        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        x = body.getX();
        y = body.getY();
        z = body.getZ();

        if (coords.length < 4) return;

        try {
            block = Integer.parseInt(coords[3]);
        } catch (Exception e) {
            return;
        }

        if (coords.length == 5) {
            try {
                data = Byte.parseByte(coords[4]);
            } catch (Exception e) {
                return;
            }
        }

        x += Integer.parseInt(coords[0]);
        y += Integer.parseInt(coords[1]);
        z += Integer.parseInt(coords[2]);

        dim = RegexUtil.COLON_PATTERN.split(line4);
    }

    @Override
    public String getTitle() {

        return "Multiple SetBlock";
    }

    @Override
    public String getSignTitle() {

        return "MULTI-SET BLOCK";
    }

    @Override
    public void trigger(ChipState chip) {

        int setblock = block;

        chip.setOutput(0, chip.getInput(0));

        boolean inp = chip.getInput(0);

        if (!inp) {
            setblock = 0;
        }

        Block body = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());

        if (dim.length == 3) {
            int dimX = Integer.parseInt(dim[0]);
            int dimY = Integer.parseInt(dim[1]);
            int dimZ = Integer.parseInt(dim[2]);
            for (int lx = 0; lx < dimX; lx++) {
                for (int ly = 0; ly < dimY; ly++) {
                    for (int lz = 0; lz < dimZ; lz++) {
                        body.getWorld().getBlockAt(x + lx, y + ly, z + lz).setTypeIdAndData(setblock, data, true);
                    }
                }
            }
        } else {
            body.getWorld().getBlockAt(x, y, z).setTypeIdAndData(setblock, data, true);
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new MultipleSetBlock(getServer(), sign, this);
        }
    }
}