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

public class ForceArgument extends Argument {

    public ForceArgument() {
        super("force", "<player>", "强制玩家检查，不管玩家的权限如何。");
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
        Set<UUID> forced = HrainAC.getCheckManager().getForcedPlayers();
        if(forced.contains(uuid)) {
            HrainAC.getCheckManager().removeForced(uuid);
            sender.sendMessage(ChatColor.AQUA + "已经移除 " + target.getName() + " 的强制检查");
        } else {
            HrainAC.getCheckManager().addForced(uuid);
            sender.sendMessage(ChatColor.AQUA + "已经使 " + target.getName() + " 强制检查");
        }
        return true;
    }
}
