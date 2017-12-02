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
package com.greatmancode.legendarybot.plugin.stats;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.plugin.legendarycheck.LegendaryCheckPlugin;
import com.greatmancode.legendarybot.plugin.music.MusicPlugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * The stats plugin
 */
public class StatsPlugin extends LegendaryBotPlugin {

    /**
     * The handler for the DataDog stats
     */
    private DashboardStatsHandler dashboardStatsHandler;

    /**
     * The MessageListener that listens for all messages that comes in for stats purpose.
     */
    private MessageListener messageListener;

    /**
     * The Discord bot list handler.
     */
    private DiscordBotListHandler statsHandler;

    public StatsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        //Load the configuration
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        getBot().getCommandHandler().addCommand("botstats", new BotStatsCommands(this), "Admin Commands");
        dashboardStatsHandler = new DashboardStatsHandler(this);
        messageListener = new MessageListener(this);
        getBot().getJDA().forEach(jda -> jda.addEventListener(messageListener));
        log.info("Command !botstats loaded!");
        if (Boolean.parseBoolean(props.getProperty("stats.enable"))) {
            statsHandler = new DiscordBotListHandler(props, this);
        }
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("botstats");
        if (statsHandler != null) {
            statsHandler.stop();
        }
        dashboardStatsHandler.stop();
        getBot().getJDA().forEach(jda -> jda.removeEventListener(messageListener));
        log.info("Command !botstats unloaded!");
    }

    /**
     * Retrieve the total member count on all servers.
     * @return The number of members on all discord servers the bot is connected to.
     */
    public int getMemberCount() {
        final int[] membersAmount = new int[1];
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(g -> membersAmount[0] += g.getMembers().stream().filter((m) -> !m.getUser().isBot()).count()));
        return membersAmount[0];
    }

    /**
     * Get the Used ram of the bot
     * @return The used ram of the bot in MB.
     */
    public long getUsedRam() {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;
        return (runtime.totalMemory() - runtime.freeMemory()) / mb;
    }

    /**
     * Get the total amount of audio connections the bot is currently connected in.
     * @return The amount of audio connections.
     */
    public int getAudioConnections() {
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(g -> i[0] += g.getAudioManager().isConnected() ? 1 : 0));
        return i[0];
    }

    /**
     * Get the amount of songs in the queue
     * @return The total amount of songs in the queue.
     */
    public int getSongQueue() {
        final int[] i = {0};
        ((MusicPlugin)getBot().getPluginManager().getPlugin("musicPlugin").getPlugin()).getMusicManager().getGuildsMusicManager().forEach((k, v) -> i[0] += v.scheduler.getQueueLength());
        return i[0];
    }

    /**
     * Get the total count of text channels in all Discord guilds currently connected to.
     * @return The total count of text channels.
     */
    public int getTextChannelCount() {
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> i[0] += jda.getTextChannels().size() );
        return i[0];
    }

    /**
     * Get the total count of voice channels in all Discord guilds currently connected to.
     * @return The total count of voice channels.
     */
    public int getVoiceChannelCount() {
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> i[0] += jda.getVoiceChannels().size() );
        return i[0];
    }

    /**
     * Get the amount of guilds that enabled the LegendaryCheck feature.
     * @return The amount of guilds that enabled legendaryCheck.
     */
    public int getLegendaryCount() {
        return ((LegendaryCheckPlugin)getBot().getPluginManager().getPlugin("legendaryCheckPlugin").getPlugin()).getLegendaryCheckEnabledCount();
    }

    /**
     * Get the amount of guilds the bot is connected to.
     * @return The total amount of guilds the bot is connected to.
     */
    public int getGuildCount() {
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> i[0] += jda.getGuilds().size());
        return i[0];
    }

    /**
     * Get the amount of guilds that went through the setup wizard.
     * @return The amount of guilds that went through the setup wizard.
     */
    public int getGuildConfiguredCount() {
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(g -> {
            GuildSettings setting = getBot().getGuildSettings(g);
            if (setting.getRegionName() != null && setting.getWowServerName() != null && setting.getGuildName() != null) {
                i[0]++;
            }
        }));
        return i[0];
    }
}
