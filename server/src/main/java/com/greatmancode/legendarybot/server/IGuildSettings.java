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

package com.greatmancode.legendarybot.server;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A representation of Discord Guild settings. Use a MySQL database to save the parameters
 */
public class IGuildSettings implements GuildSettings {

    /**
     * A instance of the bot.
     */
    private LegendaryBot bot;

    /**
     * The ID of the Guild
     */
    private String guildId;

    /**
     * A cache of the Guild settings
     */
    private Map<String, String> settings = new HashMap<>();

    /**
     * Create a {@link GuildSettings} instance
     * @param guild The Guild those settings are being linked to.
     * @param bot A {@link LegendaryBot} instance.
     */
    public IGuildSettings(Guild guild, LegendaryBot bot) {
        this.bot = bot;
        this.guildId = guild.getId();
        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT configName,configValue FROM guild_config WHERE guildId=?");
            statement.setString(1, guildId);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                settings.put(set.getString("configName"),set.getString("configValue"));
            }
            set.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e, "guildId:" + guildId);
        }
    }

    @Override
    public String getSetting(String setting) {
        return settings.get(setting);
    }

    @Override
    public void setSetting(String setting, String value) {
        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("INSERT INTO guild_config(guildId, configName, configValue) VALUES(?,?,?) ON DUPLICATE KEY UPDATE configValue=VALUES(configValue)");
            statement.setString(1, guildId);
            statement.setString(2, setting);
            statement.setString(3, value);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e, "guildId:" + guildId, "setting:" + setting, "value:" + value);
        }
        settings.put(setting,value);
    }

    @Override
    public void unsetSetting(String setting) {
        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM guild_config WHERE guildId=? AND configName=?");
            statement.setString(1, guildId);
            statement.setString(2, setting);
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e, "guildId:" + guildId, "setting:" + setting);
        }
        settings.remove(setting);
    }
}
