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

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SyncGuildCommand extends AdminCommand implements ZeroArgsCommand {

    private WoWLinkPlugin plugin;

    public SyncGuildCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!plugin.getBot().getJDA().getGuildById(event.getGuild().getId()).getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_ROLES)) {
            event.getChannel().sendMessage("The bot need the \"**Manage Roles**\" permission to be able to set roles to the users.").queue();
            return;
        }

        if (plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKSET_ENABLED) == null) {
            event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage("Account sync is not enabled in this Discord server.").queue());
            return;
        }

        Map<User,String> userCharacterMap = new HashMap<>();
        event.getGuild().getMembers().forEach((m) -> {
            try {
                userCharacterMap.put(m.getUser(),plugin.getMainCharacterForUserInGuild(m.getUser(),event.getGuild()));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        userCharacterMap.forEach((u,l) -> {
            if (l != null) {
                plugin.setDiscordRank(u,event.getGuild(),plugin.getWoWRank(event.getGuild(),l));
            }
        });

        event.getChannel().sendMessage("All ranks are synced!").queue();
    }

    @Override
    public String help() {
        return "syncguild - Sync the rank of all the users with a main WoW character.";
    }
}
