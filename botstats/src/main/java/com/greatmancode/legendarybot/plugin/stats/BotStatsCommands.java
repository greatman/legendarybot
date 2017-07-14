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
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class BotStatsCommands implements Command {

    private StatsPlugin plugin;

    public BotStatsCommands(StatsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        EmbedBuilder eb = new EmbedBuilder();
        MessageBuilder builder = new MessageBuilder();
        eb.setColor(Color.getHSBColor(217,65,0));
        eb.setAuthor("LegendaryBot Stats","https://github.com/greatman/legendarybot","https://cdn.discordapp.com/app-icons/267134720700186626/ba3e3856b551e0c425280571db7746ef.jpg");

        builder.append("__**General Stats**__\n");

        builder.append("**Amount of Servers**: ");
        builder.append(plugin.getBot().getJDA().getGuilds().size());
        builder.append("\n");

        final int[] membersAmount = new int[1];
        plugin.getBot().getJDA().getGuilds().forEach(g -> membersAmount[0] += g.getMembers().size());
        builder.append("**Amount of members**: ");
        builder.append(membersAmount[0]);
        builder.append("\n");


        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();
        String uptime = DurationFormatUtils.formatDuration(mxBean.getUptime(), "d") + " days " + DurationFormatUtils.formatDuration(mxBean.getUptime()," HH:mm:ss");
        builder.append("**Uptime**: " + uptime);
        builder.append("\n\n");
        builder.append("__**Memory**__:\n");
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        builder.append("**Used Memory**: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + " MB\n");
        builder.append("**Free Memory**: " + runtime.freeMemory() / mb + " MB\n");
        builder.append("**Available Memory**: " + runtime.totalMemory() / mb + " MB\n");
        builder.append("**Max Memory**: " + runtime.maxMemory() / mb + " MB\n");
        builder.append("\n\n");
        eb.setDescription(builder.getStringBuilder());
        event.getChannel().sendMessage(eb.build()).queue();

        final MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append("__**Per-Server stats**__: \n");
        plugin.getBot().getJDA().getGuilds().forEach(g ->  {
            messageBuilder.append("**" + g.getName() + "** | M: **" + g.getMembers().size() + "**\n");
            if (messageBuilder.length()>= 1500) {
                event.getChannel().sendMessage(messageBuilder.build()).queue();
                messageBuilder.getStringBuilder().setLength(0);
            }
        });
        event.getChannel().sendMessage(messageBuilder.build()).queue();


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
        return "!botstats - Shows Global stats about the bot";
    }
}
