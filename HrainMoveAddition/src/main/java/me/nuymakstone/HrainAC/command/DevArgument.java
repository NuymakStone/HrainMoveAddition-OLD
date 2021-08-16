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

import me.nuymakstone.HrainAC.util.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class DevArgument extends Argument {

    //something crude but simple to easily troubleshoot errors

    DevArgument() {
        super("dev", "", "显示有关服务器和客户端的信息。");
    }

    @Override
    public boolean process(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage("服务器版本: " + Bukkit.getVersion());
        sender.sendMessage("Bukkit版本: " + Bukkit.getBukkitVersion());
        String nmsPackage = "";
        if (HrainAC.getServerVersion() == 8)
            nmsPackage = net.minecraft.server.v1_8_R3.MinecraftServer.class.getPackage().getName();
        else if (HrainAC.getServerVersion() == 7)
            nmsPackage = net.minecraft.server.v1_7_R4.MinecraftServer.class.getPackage().getName();
        sender.sendMessage("NMS版本: " + nmsPackage.substring(nmsPackage.lastIndexOf(".") + 1));
        sender.sendMessage("HrainAC版本: " + HrainAC.BUILD_NAME);
        boolean async = HrainAC.getPacketHandler().getPacketListener().isAsync();
        sender.sendMessage("异步检查: " + (async ? ChatColor.RED + "" : "") + async);
        sender.sendMessage("Java約: " + System.getProperty("java.version") + "; " + System.getProperty("java.vm.vendor") + "; " + System.getProperty("java.vm.name"));
        if (sender instanceof Player) {
            int clientVer = HrainAC.getHrainACPlayer((Player) sender).getClientVersion();
            sender.sendMessage("客户端版本: 1." + clientVer + ".x");
            sender.sendMessage("延迟: " + ServerUtils.getPing((Player) sender) + "ms");
        } else {
            sender.sendMessage("客户端版本: N/A");
            sender.sendMessage("延迟: N/A");
        }
        sender.sendMessage("TPS: " + ServerUtils.getTps());
        sender.sendMessage("IP: " + ServerUtils.getStress());
        List<String> plugNames = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugNames.add(plugin.getName()); //we want ALL loaded plugins, even disabled plugins
        }
        sender.sendMessage("加载的插件 (" + Bukkit.getPluginManager().getPlugins().length + "): "
                + String.join(", ", plugNames));
        return true;
    }
}
