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

package com.greatmancode.legendarybot.plugin.debuguild;

import com.greatmancode.legendarybot.api.commands.Command;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.pf4j.PluginWrapper;

public class DebugGuildCommand extends LegendaryBotPlugin implements Command {

    public DebugGuildCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        final Guild[] guildEntry = {null};
        getBot().getJDA().forEach((jda -> {
            if (guildEntry[0] == null) {
                guildEntry[0] = jda.getGuildById(args[0]);
            }

        }));

        if (guildEntry[0] != null) {
            Guild guild = guildEntry[0];
            PrivateChannel channel = event.getAuthor().openPrivateChannel().complete();
            channel.sendMessage("Guild " + guild.getName()).queue();
            channel.sendMessage("Roles:").queue();
            guild.getRoles().forEach((role -> channel.sendMessage(role.getName()).queue()));
            channel.sendMessage("Config:").queue();
            channel.sendMessage("Guild Name: " + getBot().getGuildSettings(guild).getGuildName()).queue();
            channel.sendMessage("Region Name: "  + getBot().getGuildSettings(guild).getRegionName()).queue();
            channel.sendMessage("Realm name: " + getBot().getGuildSettings(guild).getWowServerName()).queue();
            channel.sendMessage("WoW Rank config:").queue();
            channel.sendMessage("Rank 0: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_0")).queue();
            channel.sendMessage("Rank 1: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_1")).queue();
            channel.sendMessage("Rank 2: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_2")).queue();
            channel.sendMessage("Rank 3: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_3")).queue();
            channel.sendMessage("Rank 4: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_4")).queue();
            channel.sendMessage("Rank 5: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_5")).queue();
            channel.sendMessage("Rank 6: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_6")).queue();
            channel.sendMessage("Rank 7: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_7")).queue();
            channel.sendMessage("Rank 8: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_8")).queue();
            channel.sendMessage("Rank 9: " + getBot().getGuildSettings(guild).getSetting("wowlink_rank_9")).queue();
            channel.sendMessage("Rank Enabled: " + getBot().getGuildSettings(guild).getSetting("wowlink_rankset")).queue();


        }
    }

    @Override
    public boolean canExecute(Member member) {
        return member.getUser().getId().equals("95709957629939712");
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "Show information about a guild";
    }

    @Override
    public String shortDescription() {
        return help();
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("debuguild",this,"Admin Commands");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("debugguild");
    }
}
