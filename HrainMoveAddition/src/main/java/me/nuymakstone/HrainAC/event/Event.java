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

import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.wrap.packet.WrappedPacket;
import org.bukkit.entity.Player;

public abstract class Event {

    protected boolean cancelled;
    protected final Player p;
    protected final HrainACPlayer pp;
    protected final WrappedPacket wPacket;
    protected static HrainMoveAddition HrainAC;

    public Event(Player p, HrainACPlayer pp, WrappedPacket wPacket) {
        this.p = p;
        this.pp = pp;
        this.wPacket = wPacket;
    }

    //to be implemented by other Events
    public boolean preProcess() {
        return true;
    }

    //to be implemented by other Events
    public void postProcess() {
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    //Forces a reconciliation. Syncs the client to the server's game state for this particular action.
    //Make sure you check that they are allowed to be resynchronized by calling Event.allowedToResync(HrainACPlayer)
    public void resync() {
    }

    public Player getPlayer() {
        return p;
    }

    public HrainACPlayer getHrainACPlayer() {
        return pp;
    }

    public WrappedPacket getWrappedPacket() {
        return wPacket;
    }

    public static void setHrainACReference(HrainMoveAddition plugin) {
        HrainAC = plugin;
    }

    public static boolean allowedToResync(HrainACPlayer pp) {
        return (!pp.getPlayer().hasPermission(HrainAC.BASE_PERMISSION + ".bypassresync") &&
                !HrainAC.getCheckManager().getExemptedPlayers().contains(pp.getUuid())) ||
                HrainAC.getCheckManager().getForcedPlayers().contains(pp.getUuid());
    }
}
