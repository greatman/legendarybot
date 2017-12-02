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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * The !playmusic command
 */
public class PlayMusicCommand extends AdminCommand {

    /**
     * An instance of the Music Plugin.
     */
    private MusicPlugin plugin;

    public PlayMusicCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!event.getMember().getVoiceState().inVoiceChannel()) {
            event.getChannel().sendMessage("You need to be yourself in a voice channel!").queue();
            return;
        }
        String musicChannel = plugin.getBot().getGuildSettings(event.getGuild()).getSetting(MusicPlugin.MUSIC_CHANNEL_SETTING);
        VoiceChannel voiceChannel;
        if (musicChannel != null) {
            List<VoiceChannel> channels = event.getGuild().getVoiceChannelsByName(musicChannel, false);
            if (channels.isEmpty()) {
                event.getChannel().sendMessage("The channel the bot can play in doesn't exist anymore. Please ask an admin to fix it with the ``setmusicchannel`` command.").queue();
                return;
            }
            voiceChannel = channels.get(0);
        } else {
            voiceChannel = event.getMember().getVoiceState().getChannel();
        }

        plugin.getMusicManager().loadAndPlay(event.getTextChannel(), args[0], voiceChannel);
    }

    @Override
    public boolean canExecute(Member member) {
        return (super.canExecute(member) || plugin.getBot().getGuildSettings(member.getGuild()).getSetting(MusicPlugin.MEMBER_ALLOWED_SETTING) != null);
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "Play music in the voice channel you are currently in.\n\n" +
                "__Parameters__\n" +
                "**URL** (Required) : The URL to the song you want to add.\n\n" +
                "**Example**: ``!playmusic https://youtube.com/v/myawesomevideo``";
    }

    @Override
    public String shortDescription() {
        return "Play music in the voice channel you are currently in.";
    }
}
