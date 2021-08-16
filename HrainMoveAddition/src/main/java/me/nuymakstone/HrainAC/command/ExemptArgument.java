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

import java.util.Set;
import java.util.UUID;

public class ExemptArgument extends Argument {

    public ExemptArgument() {
        super("exempt", "<player>", "无论是否获得许可，都免除玩家检查。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length < 2) {
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if(target == null) {
            sender.sendMessage(ChatColor.RED + "未知玩家 \"" + args[1] + "\"");
            return true;
        }
        UUID uuid = target.getUniqueId();
        Set<UUID> exempted = HrainAC.getCheckManager().getExemptedPlayers();
        if(exempted.contains(uuid)) {
            HrainAC.getCheckManager().removeExemption(uuid);
            sender.sendMessage(ChatColor.AQUA + "" + target.getName() + " 不再免于检查。");
        } else {
            HrainAC.getCheckManager().addExemption(uuid);
            sender.sendMessage(ChatColor.AQUA + "" + target.getName() + " 检查绕过权限已添加。");
        }
        return true;
    }
}
