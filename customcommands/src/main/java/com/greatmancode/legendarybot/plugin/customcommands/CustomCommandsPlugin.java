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
package com.greatmancode.legendarybot.plugin.customcommands;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomCommandsPlugin extends LegendaryBotPlugin {

    private Map<String, Map<String, String>> guildCustomCommands = new HashMap<>();
    private GuildJoinListener listener = new GuildJoinListener(this);

    public CustomCommandsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        try {
            Connection connection = getBot().getDatabase().getConnection();
            //We create the commands table
            String SERVER_COMMANDS_TABLE = "CREATE TABLE IF NOT EXISTS `guild_commands` (\n" +
                    "  `guild_id` varchar(64) NOT NULL,\n" +
                    "  `command_name` VARCHAR(45) NOT NULL,\n" +
                    "  `text` LONGTEXT NOT NULL,\n" +
                    "  PRIMARY KEY (`guild_id`, `command_name`));\n";
            PreparedStatement statement = connection.prepareStatement(SERVER_COMMANDS_TABLE);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        log.info("Loading custom commands");
        getBot().getJDA().getGuilds().forEach(g -> {
            Map<String, String> result = new HashMap<>();
            try {
                Connection connection = getBot().getDatabase().getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM guild_commands WHERE guild_id=?");
                statement.setString(1, g.getId());
                ResultSet set = statement.executeQuery();
                while (set.next()) {
                    result.put(set.getString("command_name"), set.getString("text"));
                }
                statement.close();
                connection.close();
                guildCustomCommands.put(g.getId(), result);
            } catch (SQLException e) {
                e.printStackTrace();
                getBot().getStacktraceHandler().sendStacktrace(e);
            }
        });
        getBot().getJDA().addEventListener(listener);
        log.info("Custom commands loaded");
        getBot().getCommandHandler().setUnknownCommandHandler(new IUnknownCommandHandler(this));
        getBot().getCommandHandler().addCommand("createcmd", new CreateCommand(this));
        log.info("Plugin Custom Commands loaded!");
        log.info("Command !createcmd loaded!");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("createcmd");
        getBot().getCommandHandler().setUnknownCommandHandler(null);
        getBot().getJDA().removeEventListener(listener);
        log.info("Plugin Custom Commands unloaded!");
        log.info("Command !createcmd unloaded!");
    }

    public void createCommand(Guild guild, String commandName, String value) {
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("INSERT INTO guild_commands(guild_id, command_name, text) VALUES(?,?,?) ON DUPLICATE KEY UPDATE text=VALUES(text)");
            statement.setString(1, guild.getId());
            statement.setString(2, commandName);
            statement.setString(3, value);
            statement.executeUpdate();
            statement.close();
            conn.close();
            guildCustomCommands.get(guild.getId()).put(commandName, value);
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
    }

    public void joinGuildEvent(Guild guild) {
        guildCustomCommands.put(guild.getId(), new HashMap<>());
    }

    public Map<String,String> getServerCommands(Guild guild) {
        return Collections.unmodifiableMap(guildCustomCommands.get(guild.getId()));
    }
}
