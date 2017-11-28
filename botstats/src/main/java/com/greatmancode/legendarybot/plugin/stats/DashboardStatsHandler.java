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

import com.greatmancode.legendarybot.api.LegendaryBot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardStatsHandler {

    /**
     * Scheduler to send stats at a specific interval
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public DashboardStatsHandler(StatsPlugin plugin) {
        LegendaryBot bot = plugin.getBot();
        final Runnable postStats = () -> {
            bot.getStatsClient().gauge("legendarybot.totalservers", plugin.getGuildCount());
            bot.getStatsClient().gauge("legendarybot.textchannels", plugin.getTextChannelCount());
            bot.getStatsClient().gauge("legendarybot.voicechannels", plugin.getVoiceChannelCount());
            bot.getStatsClient().gauge("legendarybot.membercount", plugin.getMemberCount());
            bot.getStatsClient().gauge("legendarybot.legendary.enabled", plugin.getLegendaryCount());
            bot.getStatsClient().gauge("legendarybot.music.songqueue",plugin.getSongQueue());
            bot.getStatsClient().gauge("legendarybot.music.audioconnections", plugin.getAudioConnections());
            bot.getStatsClient().gauge("legendarybot.system.usedram",plugin.getUsedRam());
            bot.getStatsClient().gauge("legendarybot.system.ping",bot.getJDA().get(0).getPing());
            bot.getStatsClient().gauge("legendarybot.guilds.configurated", plugin.getGuildConfiguredCount());

        };
        scheduler.scheduleAtFixedRate(postStats,0, 10, TimeUnit.SECONDS);
    }

    /**
     * Stop the Stats Handler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }
}
