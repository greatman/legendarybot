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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The !linkwowchars command
 */
public class LinkWoWCharsCommand implements PublicCommand, ZeroArgsCommand {

    /**
     * The WowLink plugin instance.
     */
    private WoWLinkPlugin plugin;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public LinkWoWCharsCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String region  = plugin.getBot().getGuildSettings(event.getGuild()).getRegionName();
        if (region == null) {
            event.getChannel().sendMessage("The Region is not configured. Please ask a server admin to configure it with the setup command").queue();
            return;
        }
        event.getAuthor().openPrivateChannel().queue((privateChannel -> privateChannel.sendMessage("Please follow this link to connect your WoW account to this bot: https://" + plugin.getProps().getProperty("api.host") + "/api/oauth/login/" +region + "/" + event.getAuthor().getId())));
    }

    @Override
    public String help() {
        return "Link your WoW characters to your Discord account.";
    }

    @Override
    public String shortDescription() {
        return help();
    }
}
