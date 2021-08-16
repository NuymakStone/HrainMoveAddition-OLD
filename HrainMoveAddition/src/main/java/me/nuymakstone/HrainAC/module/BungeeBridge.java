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

package me.nuymakstone.HrainAC.module;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.nuymakstone.HrainAC.HrainMoveAddition;
import org.bukkit.Bukkit;

public class BungeeBridge {

    private final HrainMoveAddition HrainAC;
    private final boolean enabled;

    public BungeeBridge(HrainMoveAddition HrainAC, boolean enabled) {
        this.HrainAC = HrainAC;
        if(enabled)
            HrainAC.getServer().getMessenger().registerOutgoingPluginChannel(HrainAC, "BungeeCord");
        this.enabled = enabled;
    }

    public void sendAlertForBroadcast(String msg) {
        if(!enabled)
            return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("HrainACACAlert");
        out.writeUTF(msg);
        Bukkit.getServer().sendPluginMessage(HrainAC, "BungeeCord", out.toByteArray());
    }

}
