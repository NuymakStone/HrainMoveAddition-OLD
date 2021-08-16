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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MsgArgument extends Argument {

    public MsgArgument() {
        super("msg", "<player> <message>", "向玩家发送消息。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 3)
            return false;
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "未知玩家 \"" + args[1] + "\"");
            return true;
        }
        List<String> list = new LinkedList<>(Arrays.asList(args));
        list.remove(0);
        list.remove(0);
        String msg = HrainAC.FLAG_PREFIX + ChatColor.translateAlternateColorCodes('&', String.join(" ", list));
        target.sendMessage(msg);
        sender.sendMessage(ChatColor.GREEN + "已经向" + target.getName() + "发送消息.");
        return true;
    }
}
