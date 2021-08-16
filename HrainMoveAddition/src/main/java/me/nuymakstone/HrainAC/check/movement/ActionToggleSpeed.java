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
import me.nuymakstone.HrainAC.check.CustomCheck;
import me.nuymakstone.HrainAC.event.Event;
import me.nuymakstone.HrainAC.event.PlayerActionEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This check limits clients' sprint and sneak toggle rates to
 * prevent exploitation of the speed check and combat mechanics.
 */
public class ActionToggleSpeed extends CustomCheck {

    private Map<UUID, Long> lastSneakToggle;
    private Map<UUID, Long> lastSprintToggle;

    public ActionToggleSpeed() {
        super("actiontogglespeed", "%player% 没能绕过 action toggle speed, VL: %vl%");
        lastSneakToggle = new HashMap<>();
        lastSprintToggle = new HashMap<>();
    }

    @Override
    protected void check(Event e) {
        if(!(e instanceof PlayerActionEvent))
            return;
        PlayerActionEvent aE = (PlayerActionEvent)e;
        PlayerActionEvent.PlayerAction action = aE.getAction();
        HrainACPlayer pp = e.getHrainACPlayer();
        UUID uuid = e.getPlayer().getUniqueId();
        if(action == PlayerActionEvent.PlayerAction.SNEAK_START || action == PlayerActionEvent.PlayerAction.SNEAK_STOP) {

            if(pp.getCurrentTick() - lastSneakToggle.getOrDefault(uuid, 0L) < 1) {
                punish(pp, canCancel(), e);
            } else {
                reward(pp);
            }

            lastSneakToggle.put(uuid, pp.getCurrentTick());
        } else if(action == PlayerActionEvent.PlayerAction.SPRINT_START || action == PlayerActionEvent.PlayerAction.SPRINT_STOP) {

            if(pp.getCurrentTick() - lastSprintToggle.getOrDefault(uuid, 0L) < 1) {
                punish(pp, canCancel(), e);
            } else {
                reward(pp);
            }

            lastSprintToggle.put(uuid, pp.getCurrentTick());
        }
    }

    public void removeData(Player p) {
        UUID uuid = p.getUniqueId();
        lastSneakToggle.remove(uuid);
        lastSprintToggle.remove(uuid);
    }
}
