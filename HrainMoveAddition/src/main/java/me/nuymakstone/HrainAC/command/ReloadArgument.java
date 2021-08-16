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

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadArgument extends Argument {

    public ReloadArgument() {
        super("reload", "", "重新加载 HrainMoveAddition 配置、模块和检查。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        HrainAC.reloadConfig();
        HrainAC.unloadModules();
        HrainAC.loadModules();
        sender.sendMessage(ChatColor.AQUA + "重新加载 HrainMoveAddition 配置文件和模块。");
        return true;
    }
}
