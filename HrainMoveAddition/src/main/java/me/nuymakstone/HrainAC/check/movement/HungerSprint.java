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

import me.nuymakstone.HrainAC.check.CustomCheck;
import me.nuymakstone.HrainAC.event.Event;

public class HungerSprint extends CustomCheck {

    public HungerSprint() {
        super("hungersprint", "%player% 没能绕过 hunger-sprint, VL: %vl%");
    }

    @Override
    protected void check(Event e) {

    }
}
