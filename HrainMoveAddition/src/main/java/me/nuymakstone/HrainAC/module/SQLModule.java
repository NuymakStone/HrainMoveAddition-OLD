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
import me.nuymakstone.HrainAC.util.ConfigHelper;
import me.nuymakstone.HrainAC.util.Violation;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLModule {

    private final HrainMoveAddition HrainAC;
    private boolean enabled;
    private final int postInterval;
    private final List<Violation> violations;
    private Connection conn;
    private short loopCounter;
    private static final String DEFAULT_CHARACTER_ENCODING = "utf8";
    private static final String INSERT_TABLE = "INSERT INTO `HrainACviolations` " +
            "(`id`, `uuid`, `check`, `ping`, `vl`, `server`, `time`) VALUES " +
            "(?,?,?,?,?,?,?)";

    //ehhh... I think it would be better if HrainMoveAddition had the control to enable or disable this module.
    //kinda scary to think that instantiating this would immediately enable it...
    public SQLModule(HrainMoveAddition HrainAC) {
        this.HrainAC = HrainAC;
        enabled = ConfigHelper.getOrSetDefault(false, HrainAC.getConfig(), "sql.enabled");
        postInterval = ConfigHelper.getOrSetDefault(60, HrainAC.getConfig(), "sql.updateInterval");
        violations = Collections.synchronizedList(new ArrayList<>());
        String host = ConfigHelper.getOrSetDefault("127.0.0.1", HrainAC.getConfig(), "sql.host");
        String port = ConfigHelper.getOrSetDefault("3306", HrainAC.getConfig(), "sql.port");
        String characterEncoding = ConfigHelper.getOrSetDefault(DEFAULT_CHARACTER_ENCODING, HrainAC.getConfig(), "sql.characterEncoding");
        String database = ConfigHelper.getOrSetDefault("", HrainAC.getConfig(), "sql.database");
        String user = ConfigHelper.getOrSetDefault("", HrainAC.getConfig(), "sql.username");
        String password = ConfigHelper.getOrSetDefault("", HrainAC.getConfig(), "sql.password");
        openConnection(host, port, user, database, password, characterEncoding);
    }

    private void openConnection(String hostname, String port, String username, String database, String password, String charEncoding) {
        if (!enabled) return;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            //https://stackoverflow.com/a/3042646
            String url = "jdbc:mysql://" + hostname + ":" + port + "/" + database + "?characterEncoding=" + charEncoding;
            conn = DriverManager.getConnection(url, username, password);
            HrainAC.getLogger().info("Connected to SQL server.");
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection();
            enabled = false;
        }
    }

    public void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
                HrainAC.getLogger().info("Closed SQL connection.");
            } catch (SQLException e) {
                e.printStackTrace();
                enabled = false;
            }
        }
    }

    public void createTableIfNotExists() {
        if (!enabled) return;
        try {
            PreparedStatement create = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `HrainACviolations` ( `id` INT(8) NOT NULL AUTO_INCREMENT, `uuid` VARCHAR(64) NOT NULL , `check` VARCHAR(32) NOT NULL , `ping` INT(5) NOT NULL , `vl` INT(5) NOT NULL , `server` VARCHAR(255) NOT NULL , `time` TIMESTAMP NOT NULL , PRIMARY KEY (`id`))");
            create.executeUpdate();
            HrainAC.getLogger().info("SQL logging enabled successfully.");
        } catch (Exception e) {
            HrainAC.getLogger().warning("An error occurred while attempting to check if table \"HrainACviolations\" exists!");
            e.printStackTrace();
            closeConnection();
            enabled = false;
        }
    }

    //to be called every second by HrainMoveAddition's scheduler task
    public void tick() {
        if(loopCounter >= postInterval) {
            loopCounter = 0;
            postBuffer();
        }
        loopCounter++;
    }

    public void addToBuffer(Violation violation) {
        if (!enabled) return;
        violations.add(violation);
    }

    private void postBuffer() {
        if (!enabled || violations.size() == 0)
            return;

        List<Violation> asyncList = new ArrayList<>(violations);
        violations.clear();

        BukkitScheduler HrainACLogger = Bukkit.getServer().getScheduler();
        HrainACLogger.runTaskAsynchronously(HrainAC, () -> {
            PreparedStatement ps;
            try {
                ps = conn.prepareStatement(INSERT_TABLE);

                for (Violation loopViolation : asyncList) {
                    ps.setNull(1, Types.INTEGER);
                    ps.setObject(2, loopViolation.getPlayer().getUniqueId(), Types.VARCHAR);
                    ps.setObject(3, loopViolation.getCheck(), Types.VARCHAR);
                    ps.setObject(4, loopViolation.getPing(), Types.INTEGER);
                    ps.setObject(5, loopViolation.getVl(), Types.INTEGER);
                    ps.setObject(6, loopViolation.getServer(), Types.VARCHAR);
                    ps.setTimestamp(7, new Timestamp(loopViolation.getTime()));
                    ps.addBatch();
                }

                ps.executeBatch();

            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isRunning() {
        return conn != null && enabled;
    }

}
