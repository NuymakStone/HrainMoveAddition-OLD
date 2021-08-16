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

package me.nuymakstone.HrainAC.wrap.itemstack;

import me.nuymakstone.HrainAC.HrainMoveAddition;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public abstract class WrappedItemStack {

    public static WrappedItemStack getWrappedItemStack(ItemStack obiItemStack) {
        if (HrainMoveAddition.getServerVersion() == 8)
            return new WrappedItemStack8(obiItemStack);
        else
            return new WrappedItemStack7(obiItemStack);
    }

    public abstract float getDestroySpeed(Block obbBlock);

    public abstract boolean canDestroySpecialBlock(Block obbBlock);
}
