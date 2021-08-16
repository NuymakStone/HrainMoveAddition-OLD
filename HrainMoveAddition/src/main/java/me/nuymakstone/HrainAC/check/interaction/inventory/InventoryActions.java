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

package me.nuymakstone.HrainAC.check.interaction.inventory;

/*
 * InventoryActions check originally written by Havesta; modified, split apart, and implemented into HrainMoveAddition by NuymakStone
 *
 * InventoryActions checks if a player is
 * - interacting with entities/blocks while inventory is opened
 */

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.check.CustomCheck;
import me.nuymakstone.HrainAC.event.*;

public class InventoryActions extends CustomCheck {

    public InventoryActions() {
        super("inventoryactions", true, 3, 5, 0.999, 5000, "%player% 没能绕过 inventory-actions, VL: %vl%", null);
    }

    @Override
    protected void check(Event e) {
        HrainACPlayer pp = e.getHrainACPlayer();
        if(pp.hasInventoryOpen() != 0 && (e instanceof InteractEntityEvent || e instanceof BlockDigEvent ||
                e instanceof ArmSwingEvent || e instanceof InteractWorldEvent)) {
            punish(pp, true, e);
            e.resync();
            //TODO After failing several times, there's a chance that they could be legit, but the inventory state is glitched. Close the player's inventory.
        }
        else if(pp.hasInventoryOpen() == 0 && e instanceof ClickInventoryEvent) {
            punish(pp, true, e);
            e.resync();
        }
        else {
            reward(pp);
        }
    }
}
