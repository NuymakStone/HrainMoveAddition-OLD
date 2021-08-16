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

import me.nuymakstone.HrainAC.check.Cancelless;
import me.nuymakstone.HrainAC.check.Check;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChkinfoArgument extends Argument {

    ChkinfoArgument() {
        super("chkinfo", "<check>", "显示有关指定检查的信息。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 2)
            return false;
        for (Check check : HrainAC.getCheckManager().getChecks()) {
            if (check.getName().equalsIgnoreCase(args[1])) {
                sender.sendMessage(ChatColor.AQUA + "检查基本信息 \"" + check.getName() + "\":");
                sender.sendMessage(ChatColor.AQUA + "检查状态: " + (check.isEnabled() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
                sender.sendMessage(ChatColor.AQUA + "类别: " + check.getClass().getSuperclass().getSimpleName());
                sender.sendMessage(ChatColor.AQUA + "回弹: " + (check instanceof Cancelless ? ChatColor.GRAY + "N/A" : ((check.canCancel() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"))));
                sender.sendMessage(ChatColor.AQUA + "Flag: " + ((check.canFlag() ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED")));

                boolean bypass;
                if(sender instanceof Player) {
                    UUID uuid = ((Player) sender).getUniqueId();
                    bypass = !HrainAC.getCheckManager().getForcedPlayers().contains(uuid) && (sender.hasPermission(check.getBypassPermission()) || HrainAC.getCheckManager().getExemptedPlayers().contains(uuid));
                }
                else
                    bypass = true;

                sender.sendMessage(ChatColor.AQUA + "You " + (!bypass ? "do not " : "") + "have permission to bypass this check.");
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "Unknown check \"" + args[1] + "\"");
        return true;
    }
}
