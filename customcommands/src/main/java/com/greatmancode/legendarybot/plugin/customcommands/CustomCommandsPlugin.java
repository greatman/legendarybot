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
import com.greatmancode.legendarybot.plugin.customcommands.commands.CreateCommand;
import com.greatmancode.legendarybot.plugin.customcommands.commands.ListCommand;
import com.greatmancode.legendarybot.plugin.customcommands.commands.RemoveCommand;
import net.dv8tion.jda.core.entities.Guild;
import org.json.JSONObject;
import org.pf4j.PluginWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomCommandsPlugin extends LegendaryBotPlugin {

    private Map<String, Map<String, String>> guildCustomCommands = new HashMap<>();

    public CustomCommandsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Custom commands loaded");
        getBot().getCommandHandler().setUnknownCommandHandler(new IUnknownCommandHandler(this));
        getBot().getCommandHandler().addCommand("createcmd", new CreateCommand(this), "Custom Commands Admin Commands");
        getBot().getCommandHandler().addCommand("removecmd", new RemoveCommand(this), "Custom Commands Admin Commands");
        getBot().getCommandHandler().addCommand("listcommands", new ListCommand(this), "General Commands");
        log.info("Plugin Custom Commands loaded!");
        log.info("Command !createcmd loaded!");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("createcmd");
        getBot().getCommandHandler().removeCommand("removecmd");
        getBot().getCommandHandler().removeCommand("listcommands");
        getBot().getCommandHandler().setUnknownCommandHandler(null);
        log.info("Plugin Custom Commands unloaded!");
        log.info("Command !createcmd unloaded!");
    }

    /**
     * Create a custom command.
     * @param guild The guild to create the custom command in.
     * @param commandName The command name (The trigger)
     * @param value The value of the custom command (What the bot will say).
     */
    public void createCommand(Guild guild, String commandName, String value) {
        if (!guildCustomCommands.containsKey(guild.getId())) {
            loadCommands(guild);
        }
        String customCommandsOutput = getBot().getGuildSettings(guild).getSetting("customCommands");
        JSONObject customCommands;
        if (customCommandsOutput != null) {
            customCommands = new JSONObject(customCommandsOutput);
        } else {
            customCommands = new JSONObject();
        }
        JSONObject command = new JSONObject();
        command.put("value", value);
        command.put("type", "text");
        customCommands.put(commandName, command);
        getBot().getGuildSettings(guild).setSetting("customCommands", customCommands.toString());
        guildCustomCommands.get(guild.getId()).put(commandName, value);
    }

    /**
     * Remove a custom command
     * @param guild The guild to remove the custom command from.
     * @param commandName The command name (The trigger)
     */
    public void removeCommand(Guild guild, String commandName) {
        if (guildCustomCommands.get(guild.getId()).containsKey(commandName)) {
            JSONObject customCommands = new JSONObject(getBot().getGuildSettings(guild).getSetting("customCommands"));
            customCommands.remove(commandName);
            getBot().getGuildSettings(guild).setSetting("customCommands", customCommands.toString());
            guildCustomCommands.get(guild.getId()).remove(commandName);
        }
    }

    /**
     * Retrieve the list of custom commands of the Guild.
     * @param guild The Guild to retrieve the custom commands from.
     * @return A Map containing the Trigger and the value of each custom commands.
     */
    public Map<String,String> getServerCommands(Guild guild) {
        if (!guildCustomCommands.containsKey(guild.getId())) {
            loadCommands(guild);
        }
        return Collections.unmodifiableMap(guildCustomCommands.get(guild.getId()));
    }

    public void loadCommands(Guild guild) {
        Map<String, String> customCommandsMap = new HashMap<>();
        String customCommandsOutput = getBot().getGuildSettings(guild).getSetting("customCommands");
        if (customCommandsOutput != null) {
            JSONObject customCommands = new JSONObject(customCommandsOutput);
            customCommands.keys().forEachRemaining(k -> {
                //TODO support more than text custom commands
                customCommandsMap.put(k,customCommands.getJSONObject(k).getString("value"));
            });
        }
        guildCustomCommands.put(guild.getId(),customCommandsMap);
    }
}
