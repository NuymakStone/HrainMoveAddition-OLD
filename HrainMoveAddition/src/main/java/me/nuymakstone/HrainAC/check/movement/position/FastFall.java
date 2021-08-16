/*
 * This file is part of HrainMoveAddition Anticheat.
 * Copyright (C) 2018 HrainMoveAddition Development Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.nuymakstone.HrainAC.check.movement.position;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.AdjacentBlocks;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Significantly limits y-port speed bypasses
 */
public class FastFall extends MovementCheck {

    //TODO: You need to support "insignificant" moves

    public FastFall() {
        super("fastfall", "%player% 没能绕过 fast-fall, VL: %vl%");
    }

    @Override
    protected void check(MoveEvent e) {
        HrainACPlayer pp = e.getHrainACPlayer();
        if(AdjacentBlocks.onGroundReally(new Location(pp.getWorld(), pp.getPosition().getX(), pp.getPosition().getY(), pp.getPosition().getZ()), -1, false, 0.001, pp) ||
                e.isTeleportAccept() || e.getPlayer().isFlying() || e.hasAcceptedKnockback() || pp.getPlayer().isSleeping() || pp.isSwimming())
            return;
        double deltaY = e.getTo().getY() - e.getFrom().getY();
        double expected = (pp.getVelocity().getY() - 0.08F) * 0.98F;
        Location chkPos = e.getFrom().clone().add(0, 2.5, 0);

        //I HATE this game's movement.
        if(!AdjacentBlocks.blockAdjacentIsSolid(chkPos) &&
                !AdjacentBlocks.blockNearbyIsSolid(chkPos, false) &&
                !AdjacentBlocks.matIsAdjacent(e.getTo(), Material.LADDER, Material.VINE)) {
            if(deltaY + 0.02 < expected) {
                punishAndTryRubberband(pp, e);
            }
            else {
                reward(pp);
            }
        }
    }
}
