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

package me.nuymakstone.HrainAC.gui;

import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.HrainACPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenuWindow extends Window {

    public MainMenuWindow(HrainMoveAddition HrainAC, Player player) {
        super(HrainAC, player, 1, ChatColor.AQUA + "HrainMoveAddition");
        HrainACPlayer pp = HrainAC.getHrainACPlayer(player);

        /*elements[0] = new Element(Material.SAND, "dummy") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainMoveAddition) {
                Window testWindow = new TestWindow(HrainMoveAddition, p);
                HrainMoveAddition.getGuiManager().sendWindow(p, testWindow);
            }
        };*/

        elements[4] = new Element(Material.WORKBENCH, "切换检查") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                Window checks = new ToggleChecksWindow(HrainAC, p);
                HrainAC.getGuiManager().sendWindow(p, checks);
            }
        };

        elements[5] = new Element(Material.PAPER, "重新加载配置") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                Bukkit.dispatchCommand(p, "HrainMoveAddition reload");
            }
        };

        ItemStack notify = new ItemStack(Material.INK_SACK);
        notify.setDurability((short) (pp.getReceiveNotificationsPreference() ? 10 : 8));
        ItemMeta notifyName = notify.getItemMeta();
        notifyName.setDisplayName(pp.getReceiveNotificationsPreference() ? "通知: 开启" : "通知: 关闭");
        notify.setItemMeta(notifyName);
        elements[3] = new Element(notify) {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                pp.setReceiveNotificationsPreference(!pp.getReceiveNotificationsPreference());

                String perm = HrainAC.BASE_PERMISSION + ".alerts";
                if(!pp.getPlayer().hasPermission(perm)) {
                    pp.getPlayer().sendMessage(ChatColor.GRAY + "注：你没有许可 \"" + perm + "\" 接收 HrainMoveAddition 通知/警报。");
                }

                Window mainMenu = new MainMenuWindow(HrainAC, p);
                HrainAC.getGuiManager().sendWindow(p, mainMenu);
            }
        };

        elements[8] = new Element(Material.WOOD_DOOR, "退出") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                p.closeInventory();
            }
        };

        prepareInventory();
    }
}
