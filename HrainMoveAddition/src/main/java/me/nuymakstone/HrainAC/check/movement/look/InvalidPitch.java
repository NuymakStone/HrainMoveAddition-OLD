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

package me.nuymakstone.HrainAC.check.movement.look;

import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

//Not really an important check. This just stops skids from thinking they're so cool.
public class InvalidPitch extends MovementCheck {

    //PASSED (9/11/18)

    public InvalidPitch() {
        super("invalidpitch", "%player% 没能绕过 invalid pitch. VL: %vl%");
    }

    @EventHandler
    public void onPearl(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player && e.getEntity() instanceof EnderPearl) {

            final Player player = (Player) e.getEntity().getShooter();

            new BukkitRunnable() {
                @Override
                public void run() {
                    return;
                }

            }.runTaskTimer(HrainAC, 0, 10);
        }
    }

    @Override
    protected void check(MoveEvent event) {
        if (!event.hasDeltaRot())
            return;
        if (Math.abs(event.getTo().getPitch()) > 90)
            punishAndTryRubberband(event.getHrainACPlayer(), event);
        else
            reward(event.getHrainACPlayer());
    }
}
