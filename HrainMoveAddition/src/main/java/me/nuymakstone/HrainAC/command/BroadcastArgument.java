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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BroadcastArgument extends Argument {

    public BroadcastArgument() {
        super("broadcast", "<message>", "广播消息。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> list = new LinkedList<>(Arrays.asList(args));
        list.remove(0);
        String msg = HrainAC.FLAG_PREFIX + ChatColor.translateAlternateColorCodes('&', String.join(" ", list));
        Bukkit.broadcastMessage(msg);
        return true;
    }
}
