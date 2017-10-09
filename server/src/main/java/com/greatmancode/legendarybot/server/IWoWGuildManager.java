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
import com.greatmancode.legendarybot.api.server.WoWGuild;
import com.greatmancode.legendarybot.api.server.WowGuildManager;
import net.dv8tion.jda.core.entities.Guild;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IWoWGuildManager implements WowGuildManager {

    private final LegendaryBot bot;
    private final String guildId;
    private List<WoWGuild> wowGuilds = new ArrayList<>();
    private WoWGuild defaultGuild = null;

    public IWoWGuildManager(Guild guild, LegendaryBot bot) {
        this.bot = bot;
        this.guildId = guild.getId();

        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM guild_wowguilds WHERE guildId=?");
            statement.setString(1, guildId);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                WoWGuild wowGuild = new WoWGuild(set.getString("regionName"), set.getString("serverName"), set.getString("guildName"), set.getBoolean("default"));
                wowGuilds.add(wowGuild);
                if (wowGuild.isDefault()) {
                    defaultGuild = wowGuild;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e);
        }
    }

    @Override
    public WoWGuild getDefaultGuild() {
        return  defaultGuild;
    }

    @Override
    public List<WoWGuild> getServerGuilds() {
        return Collections.unmodifiableList(wowGuilds);
    }

    @Override
    public void addServerGuild(WoWGuild guild) {
        String query = "INSERT INTO guild_wowguilds VALUES(?,?,?";
        if (guild.isDefault()) {
            query += ",?)";
            //We remove the default old guild
            try {
                Connection conn = bot.getDatabase().getConnection();
                PreparedStatement statement = conn.prepareStatement("UPDATE guild_wowguilds SET isDefault = false WHERE guildId=? AND isDefault = true");
                statement.setString(1, guildId);
                statement.executeUpdate();
                statement.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                bot.getStacktraceHandler().sendStacktrace(e);
            }
            defaultGuild = guild;
        } else {
            query += ")";
        }
        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, guild.getRegion());
            statement.setString(2, guild.getServer());
            statement.setString(3, guild.getGuild());
            if (guild.isDefault()) {
                statement.setBoolean(4, guild.isDefault());
            }
            statement.executeUpdate();
            statement.close();
            wowGuilds.add(guild);
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e);
        }
    }

    @Override
    public void removeServerGuild(WoWGuild guild) {
        try {
            Connection conn = bot.getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("DELETE FROM guild_wowguilds WHERE guildId=? AND regionName=? AND serverName=? AND guildName=?");
            statement.setString(1,guildId);
            statement.setString(2, guild.getRegion());
            statement.setString(3, guild.getServer());
            statement.setString(4, guild.getGuild());
            statement.executeUpdate();
            statement.close();
            wowGuilds.remove(guild);
            if (guild.isDefault()) {
                //It was a default guild, let's select a new default one.
                WoWGuild newguild = wowGuilds.get(0);
                statement = conn.prepareStatement("UPDATE guild_wowguilds SET isDefault = true WHERE guildId=? AND regionName=? AND serverName=? AND guildName=?");
                statement.setString(1,guildId);
                statement.setString(2, newguild.getRegion());
                statement.setString(3, newguild.getServer());
                statement.setString(4, newguild.getGuild());
                statement.executeUpdate();
                defaultGuild = newguild;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e);
        }
    }
}
