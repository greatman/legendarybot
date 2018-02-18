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
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.core.entities.Guild;
import org.bson.Document;
import org.pf4j.PluginWrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class CustomCommandsPlugin extends LegendaryBotPlugin {

    private Map<String, Map<String, String>> guildCustomCommands = new HashMap<>();
    private GuildJoinListener listener = new GuildJoinListener(this);

    private static final String MONGO_COLLECTION_NAME = "guild";
    private static final String MONGO_DOCUMENT_NAME = "customCommands";

    public CustomCommandsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Loading custom commands");
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(g -> {
            MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
            Map<String, String> result = new HashMap<>();
            collection.find(eq("guild_id",g.getId())).forEach((Block<Document>) document -> {
                if (document.containsKey(MONGO_DOCUMENT_NAME)) {
                    ((Document)document.get(MONGO_DOCUMENT_NAME)).forEach((k, v) -> result.put(k, (String) v));
                }
            });
            guildCustomCommands.put(g.getId(), result);
        }));

        getBot().getJDA().forEach(jda -> jda.addEventListener(listener));
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
        getBot().getJDA().forEach((jda) -> jda.removeEventListener(listener));
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
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        collection.updateOne(eq("guild_id", guild.getId()),set(MONGO_DOCUMENT_NAME+ "." + commandName, value),new UpdateOptions().upsert(true));
        guildCustomCommands.get(guild.getId()).put(commandName, value);
    }

    /**
     * Remove a custom command
     * @param guild The guild to remove the custom command from.
     * @param commandName The command name (The trigger)
     */
    public void removeCommand(Guild guild, String commandName) {
        if (guildCustomCommands.get(guild.getId()).containsKey(commandName)) {
            MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
            collection.updateOne(and(eq("guild_id",guild.getId()), exists(MONGO_DOCUMENT_NAME + "." + commandName)), unset(MONGO_DOCUMENT_NAME + "." + commandName));
            guildCustomCommands.get(guild.getId()).remove(commandName);
        }
    }

    /**
     * Handler for the joinGuild event. Add the guild to the map.
     * @param guild The guild to add
     */
    public void joinGuildEvent(Guild guild) {
        //TODO load current commands if it's a rejoin
        guildCustomCommands.put(guild.getId(), new HashMap<>());
    }

    /**
     * Retrieve the list of custom commands of the Guild.
     * @param guild The Guild to retrieve the custom commands from.
     * @return A Map containing the Trigger and the value of each custom commands.
     */
    public Map<String,String> getServerCommands(Guild guild) {
        return Collections.unmodifiableMap(guildCustomCommands.get(guild.getId()));
    }
}
