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

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.CustomCheck;
import me.nuymakstone.HrainAC.event.Event;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.Pair;
import me.nuymakstone.HrainAC.util.Ray;
import me.nuymakstone.HrainAC.util.ServerUtils;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AimbotConvergence extends CustomCheck {

    //You better turn off your aimbot when your opponent is standing still!
    //Flags basic aimbots that update rotation before motion updates

    private Map<UUID, Vector> lastConvergencePointMap;

    public AimbotConvergence() {
        super("aimbotconvergence", true, -1, 5, 0.999, 5000, "%player% 没能绕过 aimbot (convergence), VL: %vl%", null);
        this.lastConvergencePointMap = new HashMap<>();
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
    protected void check(Event event) {
        if(event instanceof MoveEvent) {
            MoveEvent e = (MoveEvent) event;
            HrainACPlayer pp = e.getHrainACPlayer();
            if(!e.hasDeltaPos())
                return;
            UUID uuid = pp.getUuid();
            Vector prePos = e.getFrom().toVector().clone().subtract(pp.getVelocity()).add(new Vector(0, 1.62, 0));
            Vector postPos = e.getFrom().toVector().add(new Vector(0, 1.62, 0));
            Vector preDir = e.getFrom().getDirection();
            Vector postDir = e.getTo().getDirection();
            Pair<Vector, Vector> points = new Ray(prePos, preDir).closestPointsBetweenLines(new Ray(postPos, postDir));

            Vector lastConvergence = lastConvergencePointMap.get(uuid);
            Vector convergence = points.getKey().add(points.getValue()).multiply(0.5);

            if(lastConvergence != null &&
                    //make sure TPs don't false this check
                    pp.getCurrentTick() - pp.getLastTeleportAcceptTick() > ServerUtils.getPing(e.getPlayer()) / 50 + 10 &&
                    pp.getCurrentTick() > 100) {

                double distance = lastConvergence.distanceSquared(convergence);
                if(!Double.isNaN(distance)) {

                    if(distance < 0.00000001)
                        punish(pp, false, e);
                    else
                        reward(pp);

                }
            }

            lastConvergencePointMap.put(uuid, convergence);
        }
    }

    @Override
    public void removeData(Player p) {
        lastConvergencePointMap.remove(p.getUniqueId());
    }
}