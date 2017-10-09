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
import com.greatmancode.legendarybot.api.server.WoWGuild;
import com.greatmancode.legendarybot.plugin.legendarycheck.LegendaryCheckPlugin;
import com.greatmancode.legendarybot.plugin.music.MusicPlugin;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.util.Properties;

public class StatsPlugin extends LegendaryBotPlugin {

    private DashboardStatsHandler dashboardStatsHandler;
    private MessageListener messageListener;
    private DiscordBotListHandler statsHandler;

    public StatsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        //Load the configuration
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        getBot().getCommandHandler().addCommand("botstats", new BotStatsCommands(this));
        dashboardStatsHandler = new DashboardStatsHandler(this);
        messageListener = new MessageListener(this);
        getBot().getJDA().addEventListener(messageListener);
        log.info("Command !botstats loaded!");
        if (Boolean.parseBoolean(props.getProperty("stats.enable"))) {
            statsHandler = new DiscordBotListHandler(props, this);
        }
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("botstats");
        if (statsHandler != null) {
            statsHandler.stop();
        }
        dashboardStatsHandler.stop();
        getBot().getJDA().removeEventListener(messageListener);
        log.info("Command !botstats unloaded!");
    }

    public int getMemberCount() {
        final int[] membersAmount = new int[1];
        getBot().getJDA().getGuilds().forEach(g -> membersAmount[0] += g.getMembers().stream().filter((m) -> !m.getUser().isBot()).count());
        return membersAmount[0];
    }

    public long getUsedRam() {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;
        return (runtime.totalMemory() - runtime.freeMemory()) / mb;
    }

    public int getAudioConnections() {
        final int[] i = {0};
        getBot().getJDA().getGuilds().forEach(g -> i[0] += g.getAudioManager().isConnected() ? 1 : 0);
        return i[0];
    }

    public int getSongQueue() {
        final int[] i = {0};
        ((MusicPlugin)getBot().getPluginManager().getPlugin("musicPlugin").getPlugin()).getMusicManager().getGuildsMusicManager().forEach((k, v) -> i[0] += v.scheduler.getQueueLength());
        return i[0];
    }

    public int getTextChannelCount() {
        return getBot().getJDA().getTextChannels().size();
    }

    public int getVoiceChannelCount() {
        return getBot().getJDA().getVoiceChannels().size();
    }

    public int getLegendaryCount() {
        return ((LegendaryCheckPlugin)getBot().getPluginManager().getPlugin("legendaryCheckPlugin").getPlugin()).getLegendaryCheckEnabledCount();
    }

    public int getGuildCount() {
        return getBot().getJDA().getGuilds().size();
    }

    public int getGuildConfiguredCount() {
        final int[] i = {0};
        getBot().getJDA().getGuilds().forEach(g -> {
            if (getBot().getWowGuildManager(g).getDefaultGuild() != null) {
                i[0]++;
            }
        });
        return i[0];
    }
}
