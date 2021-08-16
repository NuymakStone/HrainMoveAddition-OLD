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

package me.nuymakstone.HrainAC.check.interaction.terrain;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.BlockDigCheck;
import me.nuymakstone.HrainAC.event.BlockDigEvent;
import me.nuymakstone.HrainAC.util.AABB;
import me.nuymakstone.HrainAC.util.MathPlus;
import me.nuymakstone.HrainAC.util.Placeholder;
import me.nuymakstone.HrainAC.util.ServerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;

public class BlockBreakReach extends BlockDigCheck {

    private final float MAX_REACH;
    private final float MAX_REACH_CREATIVE;

    public BlockBreakReach() {
        super("blockbreakreach", "%player% 没能绕过 block break reach, Reach: %distance%m, VL: %vl%");
        MAX_REACH = (float) customSetting("maxReach", "", 3.1);
        MAX_REACH_CREATIVE = (float) customSetting("maxReachCreative", "", 5.0);
    }

    @Override
    protected void check(BlockDigEvent e) {
        Player p = e.getPlayer();
        HrainACPlayer pp = e.getHrainACPlayer();

        Location bLoc = e.getBlock().getLocation();
        Vector min = bLoc.toVector();
        Vector max = bLoc.toVector().add(new Vector(1, 1, 1));
        AABB targetAABB = new AABB(min, max);

        Vector ppPos;
        if(pp.isInVehicle()) {
            ppPos = HrainAC.getLagCompensator().getHistoryLocation(ServerUtils.getPing(p), p).toVector();
            ppPos.setY(ppPos.getY() + p.getEyeHeight());
        }
        else {
            ppPos = pp.getHeadPosition();
        }

        double maxReach = pp.getPlayer().getGameMode() == GameMode.CREATIVE ? MAX_REACH_CREATIVE : MAX_REACH;
        double dist = targetAABB.distanceToPosition(ppPos);

        if (dist > maxReach) {
            punish(pp, 1, true, e, new Placeholder("distance", MathPlus.round(dist, 2)));
            e.resync();
        } else {
            reward(pp);
        }
    }
}
