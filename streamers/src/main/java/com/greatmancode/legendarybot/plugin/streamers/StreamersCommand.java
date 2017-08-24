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

package com.greatmancode.legendarybot.plugin.streamers;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Map;

/**
 * The !streamers command. Lists all the streamers and their status.
 */
public class StreamersCommand implements PublicCommand, ZeroArgsCommand {

    /**
     * The {@link StreamersPlugin} instance.
     */
    private StreamersPlugin plugin;

    public StreamersCommand(StreamersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        MessageBuilder builder = new MessageBuilder();
        String streamersConfig = plugin.getBot().getGuildSettings(event.getGuild()).getSetting(StreamersPlugin.CONFIG_KEY);
        if (streamersConfig != null) {
            Arrays.stream(streamersConfig.split(";")).forEach(s -> {
                String[] streamer = s.split(",");
                StreamPlatform platform = StreamPlatform.valueOf(streamer[1]);
                Map<String, String> result = plugin.isStreaming(streamer[0], platform);
                if (result.size() != 0) {
                    String url = "";
                    if (platform == StreamPlatform.TWITCH) {
                        url = "https://twitch.tv/" + streamer[0];
                    } else if (platform == StreamPlatform.MIXER) {
                        url = "https://mixer.com/" + streamer[0];
                    }
                    builder.append(streamer[0] + " is streaming " + result.get(StreamersPlugin.STATUS_KEY) + " in " + result.get(StreamersPlugin.GAME_KEY) + "! " + url);
                } else {
                    builder.append(streamer[0] + " is not streaming!");
                }
                builder.append("\n");
            });
            event.getChannel().sendMessage(builder.build()).queue();
        } else {
            event.getChannel().sendMessage("No streamers on this server!").queue();
        }

    }

    @Override
    public String help() {
        return "streamers - Return the list of streamers of this Discord server and their status";
    }
}
