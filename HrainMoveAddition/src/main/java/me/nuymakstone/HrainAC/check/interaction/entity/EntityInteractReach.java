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

package me.nuymakstone.HrainAC.check.interaction.entity;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.EntityInteractionCheck;
import me.nuymakstone.HrainAC.event.InteractEntityEvent;
import me.nuymakstone.HrainAC.util.AABB;
import me.nuymakstone.HrainAC.util.MathPlus;
import me.nuymakstone.HrainAC.util.Placeholder;
import me.nuymakstone.HrainAC.util.ServerUtils;
import me.nuymakstone.HrainAC.wrap.entity.WrappedEntity;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EntityInteractReach extends EntityInteractionCheck {

    //PASSED (6/24/2019)

    private final double MAX_REACH;
    private final double MAX_REACH_CREATIVE;
    private final int PING_LIMIT;
    private final boolean LAG_COMPENSATION;
    private final boolean CHECK_OTHER_ENTITIES;

    public EntityInteractReach() {
        super("entityinteractreach", "%player% 没能绕过 reach. Reach: %distance%m VL: %vl%");
        MAX_REACH = (double) customSetting("maxReach", "", 3.1);
        MAX_REACH_CREATIVE = (double) customSetting("maxReachCreative", "", 5.0);
        PING_LIMIT = (int) customSetting("pingLimit", "", -1);
        LAG_COMPENSATION = (boolean) customSetting("lagCompensation", "", true);
        CHECK_OTHER_ENTITIES = (boolean) customSetting("checkOtherEntities", "", false);
    }

    @Override
    protected void check(InteractEntityEvent e) {
        Entity victimEntity = e.getEntity();
        if (!(victimEntity instanceof Player) && !CHECK_OTHER_ENTITIES)
            return;
        int ping = ServerUtils.getPing(e.getPlayer());
        if (PING_LIMIT > -1 && ping > PING_LIMIT)
            return;
        Player p = e.getPlayer();
        HrainACPlayer att = e.getHrainACPlayer();

        Location victimLocation;
        if (LAG_COMPENSATION)
            victimLocation = HrainAC.getLagCompensator().getHistoryLocation(ping, victimEntity);
        else
            victimLocation = victimEntity.getLocation();

        AABB victimAABB = WrappedEntity.getWrappedEntity(victimEntity).getHitbox(victimLocation.toVector());

        Vector attackerPos;
        if (att.isInVehicle()) {
            attackerPos = HrainAC.getLagCompensator().getHistoryLocation(ServerUtils.getPing(p), p).toVector();
            attackerPos.setY(attackerPos.getY() + p.getEyeHeight());
        } else {
            attackerPos = att.getHeadPosition();
        }

        double maxReach = att.getPlayer().getGameMode() == GameMode.CREATIVE ? MAX_REACH_CREATIVE : MAX_REACH;
        double dist = victimAABB.distanceToPosition(attackerPos);
        if (dist > 10) {
            return;
        }else{
            if (dist > maxReach) {
            punish(att, 1, true, e, new Placeholder("distance", MathPlus.round(dist, 2)));
        } else {
            reward(att);
        } }
    }
}
