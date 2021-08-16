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

import me.nuymakstone.HrainAC.HrainACPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleAlertsArgument extends Argument {

    public ToggleAlertsArgument() {
        super("talerts", "", "自动切换警报。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HrainACCommand.PLAYER_ONLY);
            return true;
        }
        HrainACPlayer pp = HrainAC.getHrainACPlayer((Player) sender);
        pp.setReceiveNotificationsPreference(!pp.getReceiveNotificationsPreference());
        sender.sendMessage(ChatColor.AQUA + "游戏内警报已 " + (pp.getReceiveNotificationsPreference() ? ChatColor.GREEN + "开启" : ChatColor.RED + "关闭"));

        String perm = HrainAC.BASE_PERMISSION + ".alerts";
        if(!pp.getPlayer().hasPermission(perm)) {
            pp.getPlayer().sendMessage(ChatColor.GRAY + "注：你没有许可 \"" + perm + "\" 接收 HrainMoveAddition 通知/警报。");
        }

        return true;
    }
}
