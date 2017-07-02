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
package com.greatmancode.legendarybot.plugin.botgeneral;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        MessageBuilder builder = new MessageBuilder();
        builder.append("Hello! I am LegendaryBot, a World of Warcraft Bot! To see all my commands. Type !help. The bot need some basic configuration to work properly. Please use the following commands:\n\n");
        builder.append("Your Battle.net region: `!setserversetting WOW_REGION_NAME US/EU`\n\n");
        builder.append("Your Server Name (In Slug format, so remove all special characters and spaces are replaced with \"-\" (hyphen)) `!setserversetting WOW_SERVER_NAME Arthas`\n\n");
        builder.append("Your Guild Name, spaces are supported without \"\": `!setserversetting GUILD_NAME YourGuildName`\n\n");
        builder.append("That is all you need for the bot to run properly! If you have any questions, feel free to see the documentation here: https://github.com/greatman/legendarybot");
        event.getGuild().getPublicChannel().sendMessage(builder.build()).queue();

    }

}
