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
import me.nuymakstone.HrainAC.util.Ray;
import me.nuymakstone.HrainAC.util.ServerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlockBreakDirection extends BlockDigCheck {

    private final boolean DEBUG_HITBOX;
    private final boolean DEBUG_RAY;
    private final boolean CHECK_DIG_START;
    private final boolean CHECK_DIG_CANCEL;
    private final boolean CHECK_DIG_COMPLETE;

    public BlockBreakDirection() {
        super("blockbreakdirection", true, 5, 10, 0.9, 5000, "%player% 没能绕过 block break direction, VL: %vl%", null);
        DEBUG_HITBOX = (boolean) customSetting("hitbox", "debug", false);
        DEBUG_RAY = (boolean) customSetting("ray", "debug", false);
        CHECK_DIG_START = (boolean) customSetting("checkDigStart", "", false);
        CHECK_DIG_CANCEL = (boolean) customSetting("checkDigCancel", "", false);
        CHECK_DIG_COMPLETE = (boolean) customSetting("checkDigComplete", "", true);
    }

    @Override
    protected void check(BlockDigEvent e) {
        Player p = e.getPlayer();
        HrainACPlayer pp = e.getHrainACPlayer();
        Location bLoc = e.getBlock().getLocation();
        Vector pos;
        if(pp.isInVehicle()) {
            pos = HrainAC.getLagCompensator().getHistoryLocation(ServerUtils.getPing(p), p).toVector();
            pos.setY(pos.getY() + p.getEyeHeight());
        }
        else {
            pos = pp.getHeadPosition();
        }
        Vector dir = MathPlus.getDirection(pp.getYaw(), pp.getPitch());
        Vector extraDir = MathPlus.getDirection(pp.getYaw() + pp.getDeltaYaw(), pp.getPitch() + pp.getDeltaPitch());

        switch (e.getDigAction()) {
            case START:
                if(CHECK_DIG_START || (p.getGameMode() == GameMode.CREATIVE && CHECK_DIG_COMPLETE))
                    break;
                return;
            case CANCEL:
                if(CHECK_DIG_CANCEL)
                    break;
                return;
            case COMPLETE:
                if(CHECK_DIG_COMPLETE)
                    break;
                return;
        }

        Vector min = bLoc.toVector();
        Vector max = bLoc.toVector().add(new Vector(1, 1, 1));
        AABB targetAABB = new AABB(min, max);

        if (DEBUG_HITBOX)
            targetAABB.highlight(HrainAC, p.getWorld(), 0.25);
        if (DEBUG_RAY)
            new Ray(pos, extraDir).highlight(HrainAC, p.getWorld(), 6F, 0.3);

        if(targetAABB.betweenRays(pos, dir, extraDir)) {
            reward(pp);
        }
        else {
            punish(pp, true, e);
            e.resync();
        }
    }
}
