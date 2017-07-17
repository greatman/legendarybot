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
import org.slf4j.Logger;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LegendaryCheckPlugin extends LegendaryBotPlugin{

    public static final String SETTING_NAME = "legendary_check";
    private Map<String, LegendaryCheck> legendaryCheckMap = new HashMap<>();

    public LegendaryCheckPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        String LEGENDARY_TABLE = "CREATE TABLE IF NOT EXISTS `legendarycheck` (\n" +
                "  `region` VARCHAR(25) NOT NULL,\n" +
                "  `serverName` VARCHAR(25) NOT NULL,\n" +
                "  `playerName` VARCHAR(25) NOT NULL,\n" +
                "  `lastModified` BIGINT NOT NULL,\n" +
                "  PRIMARY KEY (`region`,`serverName`,`playerName`));\n";
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement(LEGENDARY_TABLE);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        log.info("Starting LegendaryCheck plugin.");
        getBot().getJDA().getGuilds().forEach(this::startLegendaryCheck);
        getBot().getCommandHandler().addCommand("enablelc", new EnableLegendaryCheckCommand(this));
        getBot().getCommandHandler().addCommand("disablelc", new DisableLegendaryCheckCommand(this));
        getBot().getCommandHandler().addCommand("mutelc", new MuteLegendaryCheckCommand(this));
        log.info("Command !enablelc, !disablelc and !mutelc added!");
        log.info("Plugin LegendaryCheck started!");
    }

    @Override
    public void stop() throws PluginException {
        legendaryCheckMap.forEach((k,v) -> v.shutdown());
        legendaryCheckMap.clear();
        getBot().getCommandHandler().removeCommand("enablelc");
        getBot().getCommandHandler().removeCommand("disablelc");
        getBot().getCommandHandler().removeCommand("mutelc");
        log.info("Plugin LegendaryCheck unloaded! Command !enablelc, !disablelc  and !mutelc removed");
    }


    public void startLegendaryCheck(Guild guild) {
        if (getBot().getGuildSettings(guild).getSetting(SETTING_NAME) != null) {
            if (legendaryCheckMap.containsKey(guild.getId())) {
                legendaryCheckMap.get(guild.getId()).shutdown();
                legendaryCheckMap.remove(guild.getId());
            }
            legendaryCheckMap.put(guild.getId(), new LegendaryCheck(guild,this));
            log.info("Started check for guild " + guild.getName());
        }
    }

    public void destroyLegendaryCheck(Guild guild) {
        getBot().getGuildSettings(guild).unsetSetting(SETTING_NAME);
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

    public void stopLegendaryCheck(Guild guild) {
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

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
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        return time;
    }

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
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
    }

    public Logger getLog() {
        return log;
    }

    public int getLegendaryCheckEnabledCount() {
        return legendaryCheckMap.size();
    }
}
