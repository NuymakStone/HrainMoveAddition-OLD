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
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TestWindow extends Window {

    public TestWindow(HrainMoveAddition HrainAC, Player p) {
        super(HrainAC, p, 1, "Test");
        elements[4] = new Element(Material.STONE, "Click on me to update inventory") {
            @Override
            public void doAction(Player p, HrainMoveAddition HrainAC) {
                Element element = elements[4];
                if(element.getItemStack().getType() == Material.STONE) {
                    element.getItemStack().setType(Material.WOOD);
                }
                else {
                    element.getItemStack().setType(Material.STONE);
                }
                updateWindow();
            }
        };

        prepareInventory();
    }


}
