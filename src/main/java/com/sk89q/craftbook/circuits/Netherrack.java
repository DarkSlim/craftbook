// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.circuits;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.SourcedBlockRedstoneEvent;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

/**
 * This mechanism allow players to toggle the fire on top of Netherrack.
 *
 * @author sk89q
 */
public class Netherrack extends AbstractMechanic {

    public static class Factory extends AbstractMechanicFactory<Netherrack> {

        public Factory() {

        }

        @Override
        public Netherrack detect(BlockWorldVector pt) {

            int type = BukkitUtil.toWorld(pt).getBlockTypeIdAt(BukkitUtil.toLocation(pt));

            if (type == BlockID.NETHERRACK) return new Netherrack(pt);

            return null;
        }
    }

    /**
     * Construct the mechanic for a location.
     *
     * @param pt
     */
    private Netherrack(BlockWorldVector pt) {

        super();
    }

    /**
     * Raised when an input redstone current changes.
     */
    @Override
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event) {

        Block above = event.getBlock().getRelative(0, 1, 0);

        if (event.getNewCurrent() > 0 && canReplaceWithFire(above.getTypeId())) {
            above.setTypeId(BlockID.FIRE, false);
        } else if (event.getNewCurrent() < 1 && above != null && above.getTypeId() == BlockID.FIRE) {
            above.setTypeId(BlockID.AIR, false);
        }
    }

    /**
     * Raised when clicked.
     */
    @Override
    public void onLeftClick(PlayerInteractEvent event) {

        if (event.getBlockFace() != BlockFace.UP) return;

        Block block = event.getClickedBlock();

        if (block.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
            return;
        }

        block = block.getRelative(0, -1, 0);
        if (block.isBlockIndirectlyPowered()) {
            event.setCancelled(true);
        }
    }

    private boolean canReplaceWithFire(int t) {

        switch (t) {
            case BlockID.SNOW:
            case BlockID.LONG_GRASS:
            case BlockID.VINE:
            case BlockID.DEAD_BUSH:
            case BlockID.AIR:
                return true;
            default:
                return false;
        }
    }
}