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

package me.nuymakstone.HrainAC.check;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.event.MoveEvent;
import me.nuymakstone.HrainAC.util.Placeholder;

import java.util.List;

public abstract class MovementCheck extends Check<MoveEvent> {

    //BYPASS WARNING:
    //Move checks must check getTo() locations, and if they rubberband, they MUST NOT rubberband to getTo() locations.
    //Checks implementing their own rubberband locations must set them to Player#getLocation() (but if handling teleportation, use getTo()),
    //since that check may not be the last one in the list. Do not change getFrom() or getTo() locations.
    //Player#getLocation() is recommended for rubberbanding for some checks since Spigot has additional movement checks after HrainMoveAddition's checks.
    //A chain is as strong as its weakest link.

    protected MovementCheck(String name, boolean enabled, int cancelThreshold, int flagThreshold, double vlPassMultiplier, long flagCooldown, String flag, List<String> punishCommands) {
        super(name, enabled, cancelThreshold, flagThreshold, vlPassMultiplier, flagCooldown, flag, punishCommands);
        HrainAC.getCheckManager().getMovementChecks().add(this);
    }

    protected MovementCheck(String name, String flag) {
        this(name, true, 0, 5, 0.9, 5000, flag, null);
    }

    protected void tryRubberband(MoveEvent event) {
        if (canCancel() && event.getHrainACPlayer().getVL(this) >= cancelThreshold)
            event.resync();
    }

    protected void punishAndTryRubberband(HrainACPlayer offender, MoveEvent event, Placeholder... placeholders) {
        punishAndTryRubberband(offender, 1, event, placeholders);
    }

    protected void punishAndTryRubberband(HrainACPlayer offender, double vlAmnt, MoveEvent event, Placeholder... placeholders) {
        punish(offender, vlAmnt, false, event, placeholders);
        tryRubberband(event);
    }
}
