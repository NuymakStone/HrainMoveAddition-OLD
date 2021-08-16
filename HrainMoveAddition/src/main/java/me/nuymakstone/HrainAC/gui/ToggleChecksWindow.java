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
import me.nuymakstone.HrainAC.check.Check;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ToggleChecksWindow extends Window {

    //TODO: Paginate this menu. Max checks per page should be 45 so that we can leave one row for page actions.

    public ToggleChecksWindow(HrainMoveAddition HrainAC, Player p) {
        super(HrainAC, p, 6, ChatColor.AQUA + "切换检查");
        List<Check> list = HrainAC.getCheckManager().getChecks();

        for (int i = 0; i < list.size(); i++) {
            ItemStack status;
            String display = list.get(i).getName();
            status = new ItemStack(Material.INK_SACK);
            if (list.get(i).isEnabled()) {
                status.setDurability((short) 10);
                display += ": 开启";
            } else {
                status.setDurability((short) 8);
                display += ": 关闭";
            }
            ItemMeta buttonName = status.getItemMeta();
            buttonName.setDisplayName(display);
            status.setItemMeta(buttonName);
            final int location = i;
            elements[i] = new Element(status) {
                @Override
                public void doAction(Player p, HrainMoveAddition HrainAC) {
                    Check check = HrainAC.getCheckManager().getChecks().get(location);
                    check.setEnabled(!check.isEnabled());
                    Window window = new ToggleChecksWindow(HrainAC, p);
                    HrainAC.getGuiManager().sendWindow(p, window);
                }
            };
        }

        elements[53] = new Element(Material.WOOD_DOOR, ChatColor.RED + "返回主菜单") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                HrainAC.getGuiManager().sendWindow(p, new MainMenuWindow(HrainAC, p));
            }
        };

        prepareInventory();
    }
}
