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

package me.nuymakstone.HrainAC.event.bukkit;

import me.nuymakstone.HrainAC.util.Violation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HrainFlagEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Violation violation;

    public HrainFlagEvent(Violation violation) {
        super();
        this.violation = violation;
    }

    public Violation getViolation() {
        return violation;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
