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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Event listener for general bot things. Currently announce how to configure on join and log join/leave in the bot's discord server.
 */
public class MessageListener extends ListenerAdapter {


    private Guild guild;
    public MessageListener(Guild guild) {
        this.guild = guild;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

        //We log the event
        guild.getTextChannelsByName("logs-guild",true).get(0).sendMessage(":robot: Joined guild " + event.getGuild().getName() + ". Members: " + event.getGuild().getMembers().size()).queue();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        //We log the event
        guild.getTextChannelsByName("logs-guild",true).get(0).sendMessage(":broken_heart: Left guild " + event.getGuild().getName() + ".").queue();
    }

}
