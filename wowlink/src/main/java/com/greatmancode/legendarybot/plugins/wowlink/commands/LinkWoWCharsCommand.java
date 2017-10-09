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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import com.greatmancode.legendarybot.plugins.wowlink.utils.OAuthBattleNetApi;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class LinkWoWCharsCommand implements PublicCommand, ZeroArgsCommand {

    private WoWLinkPlugin plugin;

    public LinkWoWCharsCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    //TODO Support both regions at same time.
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String region  = plugin.getBot().getWowGuildManager(event.getGuild()).getDefaultGuild().getRegion();
        if (region == null) {
            event.getChannel().sendMessage("The Region is not configured. Please ask a server admin to configure it with !setserversetting WOW_REGION_NAME US/EU").queue();
            return;
        }
        OAuth20Service service = new ServiceBuilder(plugin.getProps().getProperty("battlenetoauth.key"))
                .scope("wow.profile")
                .callback("https://legendarybot.greatmancode.com/auth/battlenetcallback")
                .state(region + ":" + event.getAuthor().getId())
                .build(new OAuthBattleNetApi(region));
        event.getAuthor().openPrivateChannel().queue((privateChannel -> privateChannel.sendMessage("Please follow this link to connect your WoW account to this bot: " + service.getAuthorizationUrl()).queue()));

    }

    @Override
    public String help() {
        return "linkwowchars - Link your WoW characters to your Discord account.";
    }
}
