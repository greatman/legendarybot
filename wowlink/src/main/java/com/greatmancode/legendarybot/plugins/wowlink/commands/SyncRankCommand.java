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
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;

/**
 * The !syncrank command
 */
public class SyncRankCommand implements ZeroArgsCommand, PublicCommand {

    /**
     * The WowLink plugin instance.
     */
    private WoWLinkPlugin plugin;

    public SyncRankCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!plugin.getBot().getJDA(event.getGuild()).getGuildById(event.getGuild().getId()).getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_ROLES)) {
            event.getChannel().sendMessage("The bot need the \"**Manage Roles**\" permission to be able to set roles to the users.").queue();
            return;
        }
        if (plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKSET_ENABLED) == null) {
            event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage("Account sync is not enabled in this Discord server.").queue());
            return;
        }

        String character = plugin.getMainCharacterForUserInGuild(event.getAuthor(), event.getGuild());
        if (character == null) {
            event.getAuthor().openPrivateChannel().queue((c)-> c.sendMessage("You didn't set a main character yet. Please use !setguildcharacter first.").queue());
            return;
        }
        plugin.setDiscordRank(event.getAuthor(),event.getGuild(),plugin.getWoWRank(event.getGuild(), character));
        event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage("Rank synced!").queue());

    }

    @Override
    public String help() {
        return "Sync your Guild rank with your Discord account";
    }

    @Override
    public String shortDescription() {
        return help();
    }
}
