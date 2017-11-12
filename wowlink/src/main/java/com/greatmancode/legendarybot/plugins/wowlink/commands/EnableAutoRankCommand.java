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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class EnableAutoRankCommand extends AdminCommand implements ZeroArgsCommand {

    private WoWLinkPlugin plugin;

    public EnableAutoRankCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (plugin.getBot().getGuildSettings(event.getGuild()).getGuildName() == null || plugin.getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage("You can't run this command. A server administrator needs to configure the bot first. Ask him to use !setup.").queue();
            return;
        }

        if (!plugin.getBot().getJDA().getGuildById(event.getGuild().getId()).getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_ROLES)) {
            event.getChannel().sendMessage("The bot need the \"**Manage Roles**\" permission to be able to set roles to the users.").queue();
            return;
        }

        plugin.getBot().getGuildSettings(event.getGuild()).setSetting(WoWLinkPlugin.SETTING_RANKSET_ENABLED, "true");
        event.getChannel().sendMessage("AutoRank enabled!").queue();
    }

    @Override
    public String help() {
        return "Enable the automatic set of the rank according to the WoW guild rank.";
    }

    @Override
    public String shortDescription() {
        return help();
    }
}
