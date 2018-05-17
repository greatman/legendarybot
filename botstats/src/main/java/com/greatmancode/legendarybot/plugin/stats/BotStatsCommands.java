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

import com.greatmancode.legendarybot.api.commands.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * The !botstats command.
 */
public class BotStatsCommands implements Command {

    /**
     * An instance of the stats plugin.
     */
    private StatsPlugin plugin;

    /**
     * Build the !botstats command
     * @param plugin An instance of the class {@link StatsPlugin}
     */
    public BotStatsCommands(StatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.getHSBColor(217,65,0));
        eb.setAuthor("LegendaryBot Stats","https://github.com/greatman/legendarybot","https://cdn.discordapp.com/app-icons/267134720700186626/ba3e3856b551e0c425280571db7746ef.jpg");

        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        String uptime = DurationFormatUtils.formatDuration(mxBean.getUptime(), "d") + " days " + DurationFormatUtils.formatDuration(mxBean.getUptime()," HH:mm:ss");

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;


        eb.addField(":homes:Guilds", plugin.getGuildCount() + "",true);
        eb.addField(":busts_in_silhouette: Members", plugin.getMemberCount() + "", true);
        eb.addField(":notepad_spiral:Text Channels", plugin.getTextChannelCount() + "", true);
        eb.addField(":loudspeaker:Voice Channels", plugin.getVoiceChannelCount() + "", true);
        eb.addField(":timer:Uptime", uptime, true);

        eb.addField(":computer:Memory", "U: " +plugin.getUsedRam() + "MB / M: " + runtime.maxMemory() / mb + "MB", true);
        eb.addField(":speaker:Audio connections", plugin.getAudioConnections() + "", true);

        eb.addField(":musical_note: Song Queue", plugin.getSongQueue() + "", true);

        eb.setColor(Color.BLUE);

        event.getChannel().sendMessage(eb.build()).queue();


    }

    @Override
    public boolean canExecute(Member member) {
        return member.getUser().getId().equals("95709957629939712");
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 0;
    }

    @Override
    public String help() {
        return "Shows Global stats about the bot.";
    }

    @Override
    public String shortDescription() {
        return "Shows Global stats about the bot.";
    }
}
