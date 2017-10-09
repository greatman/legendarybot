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
package com.greatmancode.legendarybot.plugins.wowlink.commands;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.server.WoWGuild;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

public class GuildCharsCommand implements PublicCommand {

    private WoWLinkPlugin plugin;

    public GuildCharsCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {

        WoWGuild guild = plugin.getBot().getWowGuildManager(event.getGuild()).getDefaultGuild();
        if (guild == null) {
            event.getChannel().sendMessage("No default guild set for this discord server. Refer to documentation").queue();
            return;
        }

        List<User> mentionedUsers = event.getMessage().getMentionedUsers();
        List<String> charactersList;
        MessageBuilder builder = new MessageBuilder();
        if (!mentionedUsers.isEmpty()) {
            builder.append(mentionedUsers.get(0).getName() + " WoW characters in the Guild ");
            charactersList = plugin.getUserCharactersInGuild(mentionedUsers.get(0), event.getGuild());
        } else {
            builder.append("Your WoW characters in the Guild ");
            charactersList = plugin.getUserCharactersInGuild(event.getAuthor(),event.getGuild());
        }




        builder.append(guild.getGuild());
        builder.append("\n");
        charactersList.forEach((c) -> builder.append(c + "\n"));
        event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage(builder.build()).queue());
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "guildchars <User> - Show all the characters that belongs to you or the mentioned user related to a discord server's guild.";
    }
}
