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

package me.nuymakstone.HrainAC.check;

import me.nuymakstone.HrainAC.HrainMoveAddition;
import me.nuymakstone.HrainAC.HrainACPlayer;
import me.nuymakstone.HrainAC.event.Event;
import me.nuymakstone.HrainAC.event.bukkit.HrainFlagEvent;
import me.nuymakstone.HrainAC.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * The Check class provides essential functions and utilities for
 * validating events before they are processed by CraftBukkit.
 * Every check inheriting this class will have access to the JavaPlugin
 * instance and other functions in the anticheat's framework.
 * Every check inheriting this class will also have the ability to send
 * notification flags with custom placeholders and have their own
 * configurations. The default placeholders are %player%, %check%,
 * %ping%, %tps%, and %vl%. Checks may also implement Bukkit's Listener
 * interface for listening to Bukkit events and they do not need to
 * register themselves, since CheckManager already handles that.
 * <p>
 * Make sure to register checks in CheckManager.
 */
public abstract class Check<E extends Event> {

    protected boolean enabled;
    protected int cancelThreshold;
    protected int flagThreshold;
    protected final double vlPassMultiplier;
    protected long flagCooldown; //in milliseconds
    protected static HrainMoveAddition HrainAC;
    protected final String permission;
    protected final String name;
    protected final String configPath;
    protected final String flag;
    protected final List<String> punishCommands;
    protected final Map<UUID, Long> lastFlagTimes;

    /**
     * Default values set in these constructors. Configuration may override them.
     *
     * @param name             name of check
     * @param enabled          enable check
     * @param cancelThreshold  VL required to cancel
     * @param flagThreshold    VL required to flag
     * @param vlPassMultiplier VL pass multiplier (eg: 0.95)
     * @param flagCooldown     flag cooldown duration (in milliseconds)
     * @param flag             flag message
     * @param punishCommands   list of commands to run
     */
    Check(String name, boolean enabled, int cancelThreshold, int flagThreshold, double vlPassMultiplier, long flagCooldown, String flag, List<String> punishCommands) {
        this.permission = HrainAC.BASE_PERMISSION + ".bypass." + name;
        this.name = name;
        FileConfiguration checkConfig = HrainAC.getChecksConfig();
        FileConfiguration msgs = HrainAC.getMessages();
        configPath = this.name + ".";
        this.enabled = ConfigHelper.getOrSetDefault(enabled, checkConfig, configPath + "enabled");
        if (!(this instanceof Cancelless))
            this.cancelThreshold = ConfigHelper.getOrSetDefault(cancelThreshold, checkConfig, configPath + "cancelThreshold");
        this.flagThreshold = ConfigHelper.getOrSetDefault(flagThreshold, checkConfig, configPath + "flagThreshold");
        this.vlPassMultiplier = ConfigHelper.getOrSetDefault(vlPassMultiplier, checkConfig, configPath + "vlPassMultiplier");
        this.flagCooldown = ConfigHelper.getOrSetDefault(flagCooldown, checkConfig, configPath + "flagCooldown");
        if (punishCommands == null)
            punishCommands = Collections.emptyList();
        this.punishCommands = ConfigHelper.getOrSetDefault(new ArrayList<>(punishCommands), checkConfig, configPath + "punishCommands");
        String msgPath = "flags." + this.name;
        this.flag = ChatColor.translateAlternateColorCodes('&', ConfigHelper.getOrSetDefault(flag, msgs, msgPath));
        this.lastFlagTimes = new HashMap<>();
        if (this instanceof Listener)
            Bukkit.getPluginManager().registerEvents((Listener) this, HrainAC);
        HrainAC.getCheckManager().getChecks().add(this);
    }

    public void checkEvent(E e) {
        boolean exempt = HrainAC.getCheckManager().getExemptedPlayers().contains(e.getPlayer().getUniqueId());
        boolean forced = HrainAC.getCheckManager().getForcedPlayers().contains(e.getPlayer().getUniqueId());
        if (!enabled || ((e.getPlayer().hasPermission(permission) || exempt) && !forced))
            return;
        check(e);
    }

    //assume player does not have permission to bypass and this check is enabled.
    protected abstract void check(E e);

    protected void punish(HrainACPlayer offender, boolean tryCancel, E e, Placeholder... placeholders) {
        punish(offender, 1, tryCancel, e, placeholders);
    }

    protected void punish(HrainACPlayer offender, double vlAmnt, boolean tryCancel, E e, Placeholder... placeholders) {
        if (tryCancel && canCancel() && offender.getVL(this) >= cancelThreshold)
            e.setCancelled(true);
        punish(offender, vlAmnt, placeholders);
    }

    private void punish(HrainACPlayer pp, double vlAmnt, Placeholder... placeholders) {
        Player offender = pp.getPlayer();
        pp.addVL(this, vlAmnt);

        flag(offender, pp, placeholders);

        HrainAC.getCommandExecutor().runACommand(punishCommands, this, vlAmnt, offender, pp, HrainAC, placeholders);
    }

    protected void reward(HrainACPlayer pp) {
        pp.multiplyVL(this, vlPassMultiplier);
    }

    private void flag(Player offender, HrainACPlayer pp, Placeholder... placeholders) {
        if (!canFlag())
            return;
        if (System.currentTimeMillis() - lastFlagTimes.getOrDefault(offender.getUniqueId(), 0L) < flagCooldown)
            return;
        if (pp.getVL(this) < flagThreshold)
            return;
        lastFlagTimes.put(offender.getUniqueId(), System.currentTimeMillis());
        String flag = this.flag;
        double tps = MathPlus.round(ServerUtils.getTps(), 2);
        int vl = pp.getVL(this);
        flag = flag.replace("%player%", offender.getName()).replace("%check%", this.name).replace("%tps%", tps + "").replace("%ping%", ServerUtils.getPing(offender) + "ms").replace("%vl%", vl + "");
        Violation violation = new Violation(pp, this, (short) vl);

        for (Placeholder placeholder : placeholders)
            flag = flag.replace("%" + placeholder.getName() + "%", placeholder.getValue().toString());

        final String flagFinal = flag;

        //I can't believe that for all this time, this was being ran on the netty thread...
        Bukkit.getScheduler().runTask(HrainAC, () -> {
            broadcastMessage(flagFinal, violation);
            logToConsole(flagFinal);
            logToFile(flagFinal);
        });

        if (HrainAC.getSQLModule().isRunning())
            HrainAC.getSQLModule().addToBuffer(violation);
        Bukkit.getScheduler().runTask(HrainAC, () -> Bukkit.getServer().getPluginManager().callEvent(new HrainFlagEvent(violation)));
    }

    private void broadcastMessage(String message, Violation violation) {
        if (HrainAC.canSendJSONMessages()) {
            String offenderName = violation.getPlayer().getName();
            String command = HrainAC.FLAG_CLICK_COMMAND.replace("%player%", offenderName);
            String commandPrompt = command.equals("") ? "" : "\n" + ChatColor.GRAY + "Click to run \"/" + command + "\"";
            JSONMessageSender msg = new JSONMessageSender(HrainAC.FLAG_PREFIX + message);
            msg.setHoverMsg("Check: " + violation.getCheck() + "\nVL: " + violation.getVl() + "\nPing: " + violation.getPing() + "ms\nTPS: " + MathPlus.round(violation.getTps(), 2) + "\nPlayer: " + offenderName + commandPrompt);
            if (!commandPrompt.equals("")) msg.setClickCommand(command);
            for (HrainACPlayer pp : HrainAC.getHrainACPlayers()) {
                if (pp.getReceiveNotificationsPreference() && pp.getPlayer().hasPermission(HrainAC.BASE_PERMISSION + ".alerts")) {
                    if (HrainAC.canPlaySoundOnFlag())
                        pp.getPlayer().playSound(pp.getPosition().toLocation(pp.getWorld()), Sound.NOTE_PIANO, 1, 1);
                    msg.sendMessage(pp.getPlayer());
                }
            }
        } else {
            for (HrainACPlayer pp : HrainAC.getHrainACPlayers()) {
                if (pp.getReceiveNotificationsPreference() && pp.getPlayer().hasPermission(HrainAC.BASE_PERMISSION + ".alerts")) {
                    if (HrainAC.canPlaySoundOnFlag())
                        pp.getPlayer().playSound(pp.getPosition().toLocation(pp.getWorld()), Sound.NOTE_PIANO, 1, 1);
                    pp.getPlayer().sendMessage(HrainAC.FLAG_PREFIX + message);
                }
            }
        }
        HrainAC.getBungeeBridge().sendAlertForBroadcast(message);
    }

    private void logToConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(HrainAC.FLAG_PREFIX + message);
    }

    private void logToFile(String message) {
        HrainAC.getViolationLogger().logMessage(ChatColor.RESET + "" + message);
    }

    protected Object customSetting(String name, String localConfigPath, Object defaultValue) {
        return ConfigHelper.getOrSetDefault(defaultValue, HrainAC.getChecksConfig(), configPath + localConfigPath + "." + name);
    }

    public String getName() {
        return name;
    }

    public void setEnabled(boolean status) {
        enabled = status;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean canCancel() {
        return cancelThreshold > -1 && !(this instanceof Cancelless);
    }

    public void setCancelThreshold(int cancelThreshold) {
        this.cancelThreshold = cancelThreshold;
    }

    public boolean canFlag() {
        return flagThreshold > -1;
    }

    public int getFlagThreshold() {
        return flagThreshold;
    }

    public void setFlagThreshold(int flagThreshold) {
        this.flagThreshold = flagThreshold;
    }

    public long getFlagCooldown() {
        return flagCooldown;
    }

    public void setFlagCooldown(int flagCooldown) {
        this.flagCooldown = flagCooldown;
    }

    public int getCancelThreshold() {
        return cancelThreshold;
    }

    public String getBypassPermission() {
        return permission;
    }

    public static void setHrainACReference(HrainMoveAddition plugin) {
        HrainAC = plugin;
    }

    //to be overridden by checks
    public void removeData(Player p) {}

    @Override
    public String toString() {
        return name;
    }
}

