/*
 * MIT License
 *
 * Copyright (c) Copyright (c) 2017-2017, Greatmancode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.greatmancode.legendarybot.plugin.legendarycheck;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import ro.fortsoft.pf4j.PluginWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Legendary Check plugin
 */
public class LegendaryCheckPlugin extends LegendaryBotPlugin{

    /**
     * The setting name where we save the legendary check channel name.
     */
    public static final String SETTING_NAME = "legendary_check";

    /**
     * The Map of running legendary check.
     */
    private Map<String, LegendaryCheck> legendaryCheckMap = new HashMap<>();

    public LegendaryCheckPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        String LEGENDARY_TABLE = "CREATE TABLE IF NOT EXISTS `legendarycheck` (\n" +
                "  `region` VARCHAR(25) NOT NULL,\n" +
                "  `serverName` VARCHAR(25) NOT NULL,\n" +
                "  `playerName` VARCHAR(25) NOT NULL,\n" +
                "  `lastModified` BIGINT NOT NULL,\n" +
                "  PRIMARY KEY (`region`,`serverName`,`playerName`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;\n";
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement(LEGENDARY_TABLE);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        log.info("Starting LegendaryCheck plugin.");
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(guild -> startLegendaryCheck(guild, i[0]++)));
        getBot().getCommandHandler().addCommand("enablelc", new EnableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        getBot().getCommandHandler().addCommand("disablelc", new DisableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        getBot().getCommandHandler().addCommand("mutelc", new MuteLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        log.info("Command !enablelc, !disablelc and !mutelc added!");
        log.info("Plugin LegendaryCheck started!");
    }

    @Override
    public void stop() {
        legendaryCheckMap.forEach((k,v) -> v.shutdown());
        legendaryCheckMap.clear();
        getBot().getCommandHandler().removeCommand("enablelc");
        getBot().getCommandHandler().removeCommand("disablelc");
        getBot().getCommandHandler().removeCommand("mutelc");
        log.info("Plugin LegendaryCheck unloaded! Command !enablelc, !disablelc  and !mutelc removed");
    }


    /**
     * Start the legendary check for a guild
     * @param guild The guild to start the Legendary check.
     */
    public void startLegendaryCheck(Guild guild) {
        startLegendaryCheck(guild, 0);
    }

    /**
     * Start a legendary check for a guild.
     * @param guild The guild to start the LC check in.
     * @param initialDelay the initial delay before starting the check.
     */
    public void startLegendaryCheck(Guild guild, int initialDelay) {
        if (getBot().getGuildSettings(guild).getSetting(SETTING_NAME) != null) {
            if (legendaryCheckMap.containsKey(guild.getId())) {
                legendaryCheckMap.get(guild.getId()).shutdown();
                legendaryCheckMap.remove(guild.getId());
            }
            legendaryCheckMap.put(guild.getId(), new LegendaryCheck(getBot(), guild,this, initialDelay));
            log.info("Started check for guild " + guild.getName());
        }
    }

    /**
     * Stops and deletes the config of a legendary check for a guild.
     * @param guild The guild to disable the legendary check.
     */
    public void destroyLegendaryCheck(Guild guild) {
        getBot().getGuildSettings(guild).unsetSetting(SETTING_NAME);
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

    /**
     * Stops the legendary check for a guild.
     * @param guild The guild to stop the legendary check.
     */
    public void stopLegendaryCheck(Guild guild) {
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

    /**
     * Retrieve the last modified date of a player in the database.
     * @param region The region of the player
     * @param serverName The server name of the player.
     * @param playerName The player name.
     * @return a long containing the last modified date in UNIX timestamp format. If not found, returns -1.
     */
    public long getPlayerInventoryDate(String region, String serverName, String playerName) {
        long time = -1;
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT lastmodified FROM legendarycheck WHERE region=? AND serverName=? AND playerName=?");
            statement.setString(1, region);
            statement.setString(2, serverName);
            statement.setString(3, playerName);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                time = set.getLong("lastModified");
            }
            set.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName, "playerName:" + playerName);
        }
        return time;
    }

    /**
     * Set the last modified date of a player in the database.
     * @param region The region of the player
     * @param serverName The server name of the player.
     * @param playerName The player name
     * @param time The time of the last modified in UNIX timestamp format.
     */
    public void setPlayerInventoryDate(String region, String serverName, String playerName, long time) {
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("INSERT INTO legendarycheck(region,serverName,playerName,lastModified) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE lastModified=VALUES(lastModified)");
            statement.setString(1, region);
            statement.setString(2, serverName);
            statement.setString(3, playerName);
            statement.setLong(4, time);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName, "playerName:" + playerName, "time:" + time);
        }
    }

    /**
     * Retrieve the count of enabled LC check.
     * @return The amount of enabled LC checks.
     */
    public int getLegendaryCheckEnabledCount() {
        return legendaryCheckMap.size();
    }
}
