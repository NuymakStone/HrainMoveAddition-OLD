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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MouseRecArgument extends Argument {

    public MouseRecArgument() {
        super("mouserec", "<\"start\"|\"stop\"> <player> [seconds]", "记录玩家鼠标的移动。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 3)
            return false;

        boolean start;
        String status = args[1].toLowerCase();
        switch (status) {
            case "start":
                start = true;
                break;
            case "stop":
                start = false;
                break;
            default:
                return false;
        }

        Player target = Bukkit.getPlayer(args[2]);
        if(target == null) {
            sender.sendMessage(ChatColor.RED + "未知玩家 \"" + args[2] + "\"");
            return true;
        }

        if(!start) {
            HrainAC.getMouseRecorder().stop(sender, target);
            return true;
        }

        float time = 0;
        if(args.length == 4) {
            try {
                time = Float.parseFloat(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "第四个参数必须是非负实数。");
                return true;
            }
            if(time < 0) {
                sender.sendMessage(ChatColor.RED + "第四个参数必须是非负实数。");
                return true;
            }
        }

        HrainAC.getMouseRecorder().start(sender, target, time);

        return true;
    }
}
