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

package me.nuymakstone.HrainAC;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.nuymakstone.HrainAC.command.HrainACCommand;
import me.nuymakstone.HrainAC.module.*;
import me.nuymakstone.HrainAC.util.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HrainMoveAddition extends JavaPlugin {
    public static Plugin instance;
    public static ProtocolManager protocolManager;

    //I take that back: this game does have its problems, but it's still fun to play on.
    //If you don't like my HashMap soup, then guess what: I don't care.
    //Made with passion in U.S.A.

    private CheckManager checkManager;
    private SQLModule sqlModule;
    private HrainMoveAddition plugin;
    private PacketHandler packetHandler;
    private ViolationLogger violationLogger;
    private FileConfiguration messages;
    private FileConfiguration checksConfig;
    private GUIManager guiManager;
    private LagCompensator lagCompensator;
    private MouseRecorder mouseRecorder;
    private BungeeBridge bungeeBridge;
    private HrainACSyncTaskScheduler HrainACSyncTaskScheduler;
    private CommandExecutor commandExecutor;
    private Map<UUID, HrainACPlayer> profiles;
    private static int SERVER_VERSION;
    public static String FLAG_PREFIX;
    public static final String BASE_PERMISSION = "HrainMoveAddition";
    public static String BUILD_NAME;
    public static String FLAG_CLICK_COMMAND;
    public static boolean USING_PACKETEVENTS;
    public static final String NO_PERMISSION = ChatColor.RED + "あなたには許可がありません \"%s\" このアクションを実行します。";
    private boolean sendJSONMessages;
    private boolean playSoundOnFlag;
    @Override
    public void onEnable() {
        plugin = this;
        BUILD_NAME = getDescription().getVersion();
        setServerVersion();
        loadModules();
        getLogger().info("HrainAC已被加载。 Copyright（C）2018-2021 NuymakStone。");
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onDisable() {
        unloadModules();
        plugin = null;
        this.getLogger().info("HrainAC已进行卸载");
    }

    public void loadModules() {
        getLogger().info("正在加载检查模块...");
        //Prioritize packetevents
        USING_PACKETEVENTS = getServer().getPluginManager().isPluginEnabled("packetevents");
        new File(plugin.getDataFolder().getAbsolutePath()).mkdirs();
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "messages.yml"));
        checksConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "checks.yml"));
        FLAG_PREFIX = ChatColor.translateAlternateColorCodes('&', ConfigHelper.getOrSetDefault("&bHrainAC: &7", messages, "prefix"));
        sendJSONMessages = ConfigHelper.getOrSetDefault(false, getConfig(), "sendJSONMessages");
        playSoundOnFlag = ConfigHelper.getOrSetDefault(false, getConfig(), "playSoundOnFlag");
        FLAG_CLICK_COMMAND = ConfigHelper.getOrSetDefault("tp %player%", getConfig(), "flagClickCommand");
        if (sendJSONMessages && getServerVersion() == 7) {
            sendJSONMessages = false;
            Bukkit.getLogger().warning("HrainAC不支持1.7.10的JSON！ 请使用1.8.9的JSON。");
        }

        HrainACSyncTaskScheduler = new HrainACSyncTaskScheduler(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, HrainACSyncTaskScheduler, 0L, 1L);

        profiles = new ConcurrentHashMap<>();
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        sqlModule = new SQLModule(this);
        sqlModule.createTableIfNotExists();
        commandExecutor = new CommandExecutor(this);
        guiManager = new GUIManager(this);
        lagCompensator = new LagCompensator(this);
        startLoggerFile();
        bungeeBridge = new BungeeBridge(this, ConfigHelper.getOrSetDefault(false, getConfig(), "enableBungeeAlerts"));
        checkManager = new CheckManager(plugin);
        checkManager.loadChecks();
        packetHandler = new PacketHandler(this);
        packetHandler.startListener();
        packetHandler.setupListenerForOnlinePlayers();
        mouseRecorder = new MouseRecorder(this);

        PluginManager pm = Bukkit.getPluginManager();
        //NPC检查
        //pm.registerEvents(new NpcMonitor(),this);
        registerCommand();

        saveConfigs();
    }

    public void unloadModules() {
        getLogger().info("正在卸载检查模块...");
        if(packetHandler != null)
            packetHandler.stopListener();
        getCommand("HrainMoveAddition").setExecutor(null);
        HandlerList.unregisterAll(this);
        if(guiManager != null)
            guiManager.stop();
        guiManager = null;
        lagCompensator = null;
        if(checkManager != null)
            checkManager.unloadChecks();
        checkManager = null;
        bungeeBridge = null;
        profiles = null;
        if(sqlModule != null)
            sqlModule.closeConnection();
        sqlModule = null;
        commandExecutor = null;
        Bukkit.getScheduler().cancelTasks(this);
        HrainACSyncTaskScheduler = null;
        violationLogger = null;
        mouseRecorder = null;
    }

    public void disable() {
        Bukkit.getScheduler().runTask(this, () -> Bukkit.getPluginManager().disablePlugin(plugin));
    }

    private void saveConfigs() {
        if(plugin == null)
            return;
        saveConfig();
        try {
            messages.save(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "messages.yml"));
            checksConfig.save(new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "checks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommand() {
        if(plugin == null)
            return;
        PluginCommand cmd = plugin.getCommand("HrainMoveAddition");
        cmd.setExecutor(new HrainACCommand(this));
        cmd.setPermission(HrainMoveAddition.BASE_PERMISSION + ".cmd");
        if (messages.isSet("unknownCommandMsg"))
            cmd.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("unknownCommandMsg")));
        else {
            messages.set("unknownCommandMsg", "Unknown command. Type \"/help\" for help.");
            cmd.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', messages.getString("unknownCommandMsg")));
        }
    }

    private void startLoggerFile() {
        boolean enabled = ConfigHelper.getOrSetDefault(true, getConfig(), "logToFile");
        String pluginFolder = plugin.getDataFolder().getAbsolutePath();
        File storageFile = new File(pluginFolder + File.separator + "logs.txt");
        violationLogger = new ViolationLogger(this, enabled);
        violationLogger.prepare(storageFile);
    }

    private void setServerVersion() {
        if(Package.getPackage("net.minecraft.server.v1_8_R3") != null) {
            SERVER_VERSION = 8;
        }
        else if(Package.getPackage("net.minecraft.server.v1_7_R4") != null) {
            SERVER_VERSION = 7;
        }
        else {
            SERVER_VERSION = 0;
        }
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getChecksConfig() {
        return checksConfig;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public SQLModule getSQLModule() {
        return plugin.sqlModule;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public static int getServerVersion() {
        return SERVER_VERSION;
    }

    public HrainACPlayer getHrainACPlayer(Player p) {
        HrainACPlayer result = profiles.get(p.getUniqueId());
        if (result == null) {
            addProfile(p);
            result = profiles.get(p.getUniqueId());
        }
        return result;
    }

    public Collection<HrainACPlayer> getHrainACPlayers() {
        return profiles.values();
    }

    public void broadcastAlertToAdmins(String msg) {
        for(HrainACPlayer pp : getHrainACPlayers()) {
            if(pp.getReceiveNotificationsPreference() && pp.getPlayer().hasPermission(HrainMoveAddition.BASE_PERMISSION + ".alerts"))
                pp.getPlayer().sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public void broadcastPrefixedAlertToAdmins(String msg) {
        String alert = FLAG_PREFIX + msg;
        broadcastAlertToAdmins(alert);
    }

    public void addProfile(Player p) {
        profiles.put(p.getUniqueId(), new HrainACPlayer(p, this));
    }

    public void removeProfile(UUID uuid) {
        profiles.remove(uuid);
    }

    public ViolationLogger getViolationLogger() {
        return violationLogger;
    }

    public LagCompensator getLagCompensator() {
        return lagCompensator;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public MouseRecorder getMouseRecorder() {
        return mouseRecorder;
    }

    public BungeeBridge getBungeeBridge() {
        return bungeeBridge;
    }

    public HrainACSyncTaskScheduler getHrainACSyncTaskScheduler() {
        return HrainACSyncTaskScheduler;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public boolean canSendJSONMessages() {
        return sendJSONMessages;
    }

    public boolean canPlaySoundOnFlag() {
        return playSoundOnFlag;
    }
}
