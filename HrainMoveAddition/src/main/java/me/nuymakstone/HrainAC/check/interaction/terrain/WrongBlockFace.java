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
import me.nuymakstone.HrainAC.check.BlockInteractionCheck;
import me.nuymakstone.HrainAC.event.InteractWorldEvent;
import me.nuymakstone.HrainAC.util.AABB;
import me.nuymakstone.HrainAC.util.MathPlus;
import me.nuymakstone.HrainAC.util.ServerUtils;
import me.nuymakstone.HrainAC.wrap.block.WrappedBlock;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.bukkit.entity.Player;

/** This check prevents players from interacting on
 * unavailable locations on blocks. Players must be
 * looking at the face of the block they want to interact
 * with.
 */
public class WrongBlockFace extends BlockInteractionCheck {

    public WrongBlockFace() {
        super("wrongblockface", true, 0, 10, 0.99, 5000, "%player% 没能绕过 wrongblockface; interacted on invalid block face, VL: %vl%", null);
    }

    @Override
    protected void check(InteractWorldEvent e) {
        HrainACPlayer pp = e.getHrainACPlayer();

        Block b = ServerUtils.getBlockAsync(e.getTargetedBlockLocation());
        AABB hitbox;
        if(b != null) {
            hitbox = WrappedBlock.getWrappedBlock(b, pp.getClientVersion()).getHitBox();
        }
        else {
            hitbox = new AABB(new Vector(), new Vector());
        }

        Vector headPos;
        if(pp.isInVehicle()) {
            Player p = e.getPlayer();
            headPos = HrainAC.getLagCompensator().getHistoryLocation(ServerUtils.getPing(p), p).toVector();
            headPos.setY(headPos.getY() + p.getEyeHeight());
        }
        else {
            headPos = pp.getHeadPosition();
        }

        if(e.getTargetedBlockFaceNormal().dot(MathPlus.getDirection(pp.getYaw(), pp.getPitch())) >= 0 &&
            !hitbox.containsPoint(headPos)) {
            punishAndTryCancelAndBlockRespawn(pp, e);
        }
        else {
            reward(pp);
        }
    }
}
