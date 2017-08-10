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
import com.timgroup.statsd.StatsDClient;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.elasticsearch.client.RestClient;
import ro.fortsoft.pf4j.PluginManager;

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
     * Retrieve the Discord Bot library
     * @return An instance of the {@link JDA} class
     */
    public abstract JDA getJDA();

    /**
     * Add a Guild to the bot.
     * @param guild The Discord Guild being added
     */
    public abstract void addGuild(Guild guild);

    /**
     * Retrieve the unhandler StacktraceHandler
     * @return The {@link StacktraceHandler} instance
     */
    public abstract StacktraceHandler getStacktraceHandler();


    public abstract RestClient getElasticSearch();

    public abstract StatsDClient getStatsClient();

}
