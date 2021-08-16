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

package me.nuymakstone.HrainAC.event;

import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.util.ClientBlock;
import me.nuymakstone.HrainAC.wrap.block.WrappedBlock;
import me.nuymakstone.HrainAC.wrap.block.WrappedBlock7;
import me.nuymakstone.HrainAC.wrap.block.WrappedBlock8;
import me.nuymakstone.HrainAC.wrap.packet.WrappedPacket;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BlockDigEvent extends Event {

    private final DigAction digAction;
    private final Block block;

    public BlockDigEvent(Player p, HrainACPlayer pp, DigAction action, Block block, WrappedPacket packet) {
        super(p, pp, packet);
        digAction = action;
        this.block = block;
    }

    @Override
    public boolean preProcess() {
        if(pp.isTeleporting()) {
            resync();
            return false;
        }
        return true;
    }

    @Override
    public void postProcess() {
        pp.setDigging(digAction == DigAction.START &&
                !pp.isInCreative() && WrappedBlock.getWrappedBlock(block, pp.getClientVersion()).getStrength() != 0);
        if (!isCancelled() && getDigAction() == DigAction.COMPLETE) {
            ClientBlock clientBlock = new ClientBlock(pp.getCurrentTick(), Material.AIR);
            pp.addClientBlock(getBlock().getLocation(), clientBlock);
        }
    }

    @Override
    public void resync() {
        if(allowedToResync(pp)) {
            if (HrainAC.getServerVersion() == 7) {
                WrappedBlock7.getWrappedBlock(getBlock(), pp.getClientVersion()).sendPacketToPlayer(pp.getPlayer());
            } else if (HrainAC.getServerVersion() == 8) {
                WrappedBlock8.getWrappedBlock(getBlock(), pp.getClientVersion()).sendPacketToPlayer(pp.getPlayer());
            }
        }

    }

    public DigAction getDigAction() {
        return digAction;
    }

    public Block getBlock() {
        return block;
    }

    public enum DigAction {
        START,
        CANCEL,
        COMPLETE
    }
}
