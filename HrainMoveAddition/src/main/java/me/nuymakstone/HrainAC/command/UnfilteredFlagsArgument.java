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

package me.nuymakstone.HrainAC.command;

import me.nuymakstone.HrainAC.check.Check;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public class UnfilteredFlagsArgument extends Argument {

    UnfilteredFlagsArgument() {
        super("unfilteredflags", "", "如果存在违规，则发送Flags。 您需要重新加载 HrainMoveAddition 才能将其恢复！");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        List<Check> checks = HrainAC.getCheckManager().getChecks();
        for(Check check : checks) {
            check.setFlagThreshold(0);
            check.setFlagCooldown(0);
        }
        sender.sendMessage(ChatColor.AQUA + "玩家现在将发送违规Flags。 要恢复此功能，您需要重新加载 Hrain AC。");
        return true;
    }
}
