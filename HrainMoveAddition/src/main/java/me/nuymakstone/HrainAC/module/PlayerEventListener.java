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

import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.event.bukkit.HrainACAsyncPlayerAbilitiesEvent;
import me.nuymakstone.HrainAC.event.bukkit.HrainACAsyncPlayerMetadataEvent;
import me.nuymakstone.HrainAC.event.bukkit.HrainACAsyncPlayerTeleportEvent;
import me.nuymakstone.HrainAC.event.bukkit.HrainACAsyncPlayerVelocityChangeEvent;
import me.nuymakstone.HrainAC.util.Pair;
import me.nuymakstone.HrainAC.util.ServerUtils;
import me.nuymakstone.HrainAC.wrap.WrappedWatchableObject;
import me.nuymakstone.HrainAC.wrap.entity.MetaData;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class PlayerEventListener implements Listener {

    private final HrainMoveAddition HrainAC;

    public PlayerEventListener(HrainMoveAddition HrainAC) {
        this.HrainAC = HrainAC;

        HrainAC.getHrainACSyncTaskScheduler().addRepeatingTask(new Runnable() {
            @Override
            public void run() {
                for(HrainACPlayer pp : HrainAC.getHrainACPlayers()) {
                    Player p = pp.getPlayer();
                    pp.setPing(ServerUtils.getPing(p));
                }
            }
        }, 40);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        HrainAC.addProfile(p); //This line is necessary since it must get called BEFORE HrainMoveAddition listens to the player's packets
        HrainAC.getHrainACPlayer(p).setOnline(true);
    }

    //Set protocol version
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        HrainAC.getHrainACPlayer(e.getPlayer()).setClientVersion(ServerUtils.getProtocolVersion(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        HrainAC.getHrainACPlayer(e.getPlayer()).setOnline(false);
        HrainAC.removeProfile(e.getPlayer().getUniqueId()); //TODO call this elsewhere
        HrainAC.getCheckManager().removeData(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(HrainACAsyncPlayerVelocityChangeEvent e) {
        if(e.isAdditive())
            return;
        HrainACPlayer pp = HrainAC.getHrainACPlayer(e.getPlayer());
        Vector vector = e.getVelocity();

        List<Pair<Vector, Long>> pendingVelocities = pp.getPendingVelocities();
        pendingVelocities.add(new Pair<>(vector, System.currentTimeMillis()));
        if(pendingVelocities.size() > 20)
            pendingVelocities.remove(0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpenServerSide(InventoryOpenEvent e) {
        HumanEntity hE = e.getPlayer();
        if(!(hE instanceof Player))
            return;

        //Fixes issues regarding the client not releasing item usage when a server inventory is opened.
        //Consumables may not have this issue.
        HrainACPlayer pp = HrainAC.getHrainACPlayer((Player) hE);
        pp.sendSimulatedAction(new Runnable() {
            @Override
            public void run() {
                pp.setBlocking(false);
                pp.setPullingBow(false);
                pp.setInventoryOpen((byte)2);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryCloseServerSide(InventoryCloseEvent e) {
        HumanEntity hE = e.getPlayer();
        if(!(hE instanceof Player))
            return;

        HrainACPlayer pp = HrainAC.getHrainACPlayer((Player) hE);
        pp.sendSimulatedAction(new Runnable() {
            @Override
            public void run() {
                pp.setInventoryOpen((byte)0);
            }
        });
    }

    @EventHandler
    public void sendMetadataEvent(HrainACAsyncPlayerMetadataEvent e) {
        List<WrappedWatchableObject> objects = e.getMetaData();
        for(WrappedWatchableObject object : objects) {
            if(object.getIndex() == 0) {
                Player p = e.getPlayer();
                HrainACPlayer pp = HrainAC.getHrainACPlayer(p);
                byte status = (byte)object.getObject();

                //bitmask
                if((status & 16) == 16) {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.USE_ITEM, true));
                } else {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.USE_ITEM, false));
                }
                if((status & 8) == 8) {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.SPRINT, true));
                } else {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.SPRINT, false));
                }
                /*if((status & 2) == 2) {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.SNEAK, true));
                } else {
                    pp.addMetaDataUpdate(new MetaData(MetaData.Type.SNEAK, false));
                }*/

                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void vehicleEnter(VehicleEnterEvent e) {
        if(e.getEntered() instanceof Player) {
            HrainACPlayer pp = HrainAC.getHrainACPlayer((Player)e.getEntered());
            pp.sendSimulatedAction(new Runnable() {
                @Override
                public void run() {
                    pp.setInVehicle(true);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void vehicleExit(VehicleExitEvent e) {
        if(e.getExited() instanceof Player) {
            HrainACPlayer pp = HrainAC.getHrainACPlayer((Player)e.getExited());
            pp.sendSimulatedAction(new Runnable() {
                @Override
                public void run() {
                    pp.setInVehicle(false);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void abilitiesServerSide(HrainACAsyncPlayerAbilitiesEvent e) {
        HrainACPlayer pp = HrainAC.getHrainACPlayer(e.getPlayer());
        pp.sendSimulatedAction(new Runnable() {
            @Override
            public void run() {
                pp.setAllowedToFly(e.isAllowedToFly());
                pp.setFlying(e.isFlying());
                pp.setInCreative(e.isCreativeMode());
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void teleport(HrainACAsyncPlayerTeleportEvent e) {
        HrainACPlayer pp = HrainAC.getHrainACPlayer(e.getPlayer());
        pp.setTeleporting(true);
        pp.setTeleportLoc(e.getPlayer().getLocation());
        pp.addPendingTeleport(new Location(pp.getWorld(), e.getX(), e.getY(), e.getZ(), e.getYaw() % 360F, e.getPitch() % 360F));
    }
}
