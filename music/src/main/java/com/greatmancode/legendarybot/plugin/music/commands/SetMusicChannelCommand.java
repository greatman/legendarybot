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
package com.greatmancode.legendarybot.plugin.music.commands;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugin.music.MusicPlugin;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * The !setmusicchannel command
 */
public class SetMusicChannelCommand extends AdminCommand {

    /**
     * An instance of the Music Plugin.
     */
    private MusicPlugin plugin;

    public SetMusicChannelCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        StringBuilder builder = new StringBuilder();
        for(String s : args) {
            builder.append(" ").append(s);
        }

        List<VoiceChannel> channels = event.getGuild().getVoiceChannelsByName(builder.toString().trim(), false);

        if (channels.isEmpty()) {
            event.getChannel().sendMessage("Channel " + builder.toString().trim() + " not found!").queue();
            return;
        }

        plugin.getBot().getGuildSettings(event.getGuild()).setSetting(MusicPlugin.MUSIC_CHANNEL_SETTING, builder.toString().trim());
        event.getChannel().sendMessage("Channel set! LegendaryBot will only play music in this channel.").queue();
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help() {
        return "Set the channel the bot will be only be able to play music in.\n" +
                "__Parameters__\n" +
                "**Channel name**: The voice channel name.";
    }

    @Override
    public String shortDescription() {
        return "Set the channel the bot will play music in.";
    }
}
