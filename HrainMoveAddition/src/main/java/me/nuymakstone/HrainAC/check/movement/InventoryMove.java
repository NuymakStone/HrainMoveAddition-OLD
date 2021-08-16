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

package me.nuymakstone.HrainAC.check.movement;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.MovementCheck;
import me.nuymakstone.HrainAC.event.MoveEvent;

/*
 * InventoryActions check originally written by Havesta; modified, split apart, and implemented into HrainMoveAddition by NuymakStone
 *
 * InventoryMove checks if a player is
 * - rotating, sprinting, or sneaking while inventory is opened
 */

public class InventoryMove extends MovementCheck {

    public InventoryMove() {
        super("inventorymove", true, 3, 5, 0.999, 5000, "%player% 没能绕过 inventory-move, VL: %vl%", null);
    }

    @Override
    protected void check(MoveEvent e) {
        HrainACPlayer pp = e.getHrainACPlayer();

        //TODO: false flag: rotation is still possible at least 1 tick after opening inventory
        //TODO: false flag: you gotta do that TP grace period thing for this too
        if((e.isUpdateRot() || pp.isSprinting() || pp.isSneaking()) && pp.hasInventoryOpen() != 0 && !e.isTeleportAccept()) {
            punishAndTryRubberband(pp, e);
        }
        else {
            reward(pp);
        }
    }
}