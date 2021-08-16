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
import me.nuymakstone.HrainAC.event.InteractWorldEvent;
import me.nuymakstone.HrainAC.util.Placeholder;

import java.util.List;

public abstract class BlockInteractionCheck extends Check<InteractWorldEvent> {

    protected BlockInteractionCheck(String name, boolean enabled, int cancelThreshold, int flagThreshold, double vlPassMultiplier, long flagCooldown, String flag, List<String> punishCommands) {
        super(name, enabled, cancelThreshold, flagThreshold, vlPassMultiplier, flagCooldown, flag, punishCommands);
        HrainAC.getCheckManager().getBlockInteractionChecks().add(this);
    }

    protected BlockInteractionCheck(String name, String flag) {
        this(name, true, 0, 5, 0.9, 5000, flag, null);
    }

    protected void punishAndTryCancelAndBlockRespawn(HrainACPlayer offender, InteractWorldEvent event, Placeholder... placeholders) {
        punishAndTryCancelAndBlockRespawn(offender, 1, event, placeholders);
    }

    protected void punishAndTryCancelAndBlockRespawn(HrainACPlayer offender, double vlAmnt, InteractWorldEvent event, Placeholder... placeholders) {
        punish(offender, vlAmnt, true, event, placeholders);
        if (offender.getVL(this) >= cancelThreshold)
            event.resync();
    }
}
