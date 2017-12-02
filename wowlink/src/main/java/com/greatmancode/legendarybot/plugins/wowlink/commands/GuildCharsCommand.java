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
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * The !guildchars command
 */
public class GuildCharsCommand implements PublicCommand {

    /**
     * The WowLink plugin instance.
     */
    private WoWLinkPlugin plugin;

    public GuildCharsCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (plugin.getBot().getGuildSettings(event.getGuild()).getGuildName() == null || plugin.getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage("You can't run this command. A server administrator needs to configure the bot first. Ask him to use !setup.").queue();
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




        builder.append(plugin.getBot().getGuildSettings(event.getGuild()).getGuildName());
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

    @Override
    public String shortDescription() {
        return "Retrive the characters of a player";
    }
}
