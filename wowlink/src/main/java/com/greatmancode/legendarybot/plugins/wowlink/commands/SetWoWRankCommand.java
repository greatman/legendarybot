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
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.util.List;

/**
 * The !setwowrank command.
 */
public class SetWoWRankCommand extends AdminCommand {

    /**
     * The WowLink plugin instance.
     */
    private WoWLinkPlugin plugin;

    public SetWoWRankCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String[] messages = new String[args.length - 1];
        System.arraycopy(args, 1, messages,0,args.length - 1);
        String rank = String.join(" ", messages);

        List<Role> roles = event.getGuild().getRolesByName(rank, true);
        if (roles.isEmpty()) {
            event.getChannel().sendMessage("Rank not found! Does the rank exists on the Discord server?").queue();
            return;
        }

        String setting = plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKS);
        JSONObject rankSettings = new JSONObject();
        if (setting != null) {
            rankSettings = new JSONObject(setting);
        }
        rankSettings.put(args[0], rank);
        plugin.getBot().getGuildSettings(event.getGuild()).setSetting(WoWLinkPlugin.SETTING_RANKS, rankSettings.toString());
        event.getChannel().sendMessage("Rank set!").queue();
    }

    @Override
    public int minArgs() {
        return 2;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help() {
        return "Link a WoW Guild Rank ID to a Discord Rank.\n" +
                "To find the Rank ID, go in the **Guild Control** panel in WoW. Substract 1 to the rank when typing the command (Example, Guild Master is Rank 0 but in WoW is shown 1)\n\n" +
                "__Parameters__\n" +
                "**Rank ID** (Required): The Guild Rank ID.\n" +
                "**Discord Rank Name** (Required): The name of the Discord Rank\n\n" +
                "**Example**: ``!setwowrank 1 Raider``";
    }

    @Override
    public String shortDescription() {
        return "Link a WoW Guild Rank ID to a Discord Rank.";
    }
}
