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
package com.greatmancode.legendarybot.api;

import com.greatmancode.legendarybot.api.commands.CommandHandler;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.utils.StacktraceHandler;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.elasticsearch.client.RestClient;
import org.influxdb.InfluxDB;
import ro.fortsoft.pf4j.PluginManager;

import java.util.List;

/**
 * Represents a LegendaryBot bot
 */
public abstract class LegendaryBot {

    /**
     * Retrieve the {@link CommandHandler}
     * @return The {@link CommandHandler} currently loaded
     */
    public abstract CommandHandler getCommandHandler();

    /**
     * Retrieve the {@link GuildSettings} instance for a Discord Guild.
     * @param guild the Discord Guild to retrieve the Settings from
     * @return A {@link GuildSettings} instance to access the Guild settings.
     */
    public abstract GuildSettings getGuildSettings(Guild guild);

    /**
     * The Plugin Manager that handles all Plugins.
     * @return the {@link PluginManager} instance.
     */
    public abstract PluginManager getPluginManager();

    /**
     * Retrieve the Database handler
     * @return A instance of the {@link HikariDataSource} class.
     */
    public abstract HikariDataSource getDatabase();

    /**
     * Retrieve the Discord Bot library instance of a specific guild
     * @param guild The Guild to get the JDA instance from
     * @return An instance of the {@link JDA} class linked to the guild
     */
    public abstract JDA getJDA(Guild guild);

    /**
     * Retrieve all JDA instances of the Bot
     * @return A List containing all JDA instances.
     */
    public abstract List<JDA> getJDA();

    /**
     * Add a Guild to the bot.
     * @param guild The Discord Guild being added
     */
    public abstract void addGuild(Guild guild);

    /**
     * Retrieve the StacktraceHandler
     * @return The {@link StacktraceHandler} instance
     */
    public abstract StacktraceHandler getStacktraceHandler();

    /**
     * Retrieve the ElasticSearch client
     * @return a instance of the ElasticSearch client.
     */
    public abstract RestClient getElasticSearch();

    /**
     * Retrieve the DataDog client
     * @return The DataDog client instance.
     */
    public abstract InfluxDB getStatsClient();

    /**
     * Retrieve the status of the bot
     * @return True if the bot is ready, else false.
     */
    public abstract boolean isReady();

}
