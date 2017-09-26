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

public class PlayMusicCommand extends AdminCommand {

    private MusicPlugin plugin;

    public PlayMusicCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String channel = null;
        VoiceChannel voiceChannel = null;
        if (args.length == 1 && event.getMember().getVoiceState().inVoiceChannel()) {
            voiceChannel = event.getMember().getVoiceState().getChannel();
        }

        if (args.length >= 3) {
            String[] argsend = new String[args.length - 1];
            System.arraycopy(args,1,argsend,0,args.length - 1);
            StringBuilder builder = new StringBuilder();
            for(String s : argsend) {
                builder.append(" ").append(s);
            }
            channel = builder.toString().trim();

        } else if (args.length == 2) {
            channel = args[1];
        }
        if (channel == null && voiceChannel == null) {
            event.getChannel().sendMessage("If you don't put a voice channel in the command, you need to be yourself in a channel!").queue();
            return;
        }
        if (voiceChannel == null) {
            List<VoiceChannel> voiceChannels = event.getGuild().getVoiceChannelsByName(channel, true);
            if (voiceChannels.size() == 0) {
                event.getChannel().sendMessage("Voice channel not found!").queue();
                return;
            } else if (voiceChannels.size() == 2) {
                event.getChannel().sendMessage("More than 1 channel found. Please be more specific").queue();
                return;
            }
            plugin.getMusicManager().loadAndPlay(event.getTextChannel(), args[0], voiceChannels.get(0));
        } else {
            plugin.getMusicManager().loadAndPlay(event.getTextChannel(), args[0], voiceChannel);
        }


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
        return "playmusic [Link] <Channel> - Play music in a channel.";
    }
}
