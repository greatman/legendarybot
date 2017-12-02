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

package com.greatmancode.legendarybot.plugin.botplay;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.plugin.stats.StatsPlugin;
import net.dv8tion.jda.core.entities.Game;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Plugin to set the "play" field of Discord to the amount of servers the bot is joined to.
 */
public class BotPlayPlugin extends LegendaryBotPlugin {

    /**
     * Scheduler to update the server count
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public BotPlayPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        final Runnable run = () -> {
                Game game = Game.streaming("on " + ((StatsPlugin)getBot().getPluginManager().getPlugin("statsPlugin").getPlugin()).getGuildCount()  + " servers","https://github.com/greatman/legendarybot");
                getBot().getJDA().forEach(jda -> jda.getPresence().setGame(game));
        };
        scheduler.scheduleAtFixedRate(run,0,30, TimeUnit.MINUTES);
        log.info("BotPlay loaded!");
    }

    @Override
    public void stop() {
        scheduler.shutdownNow();
        log.info("BotPlay unloaded!");
    }
}
