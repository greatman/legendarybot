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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.awt.*;
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
        String streamersConfig = plugin.getBot().getGuildSettings(event.getGuild()).getSetting(StreamersPlugin.CONFIG_KEY);
        if (streamersConfig != null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Server Streamers");
            eb.setColor(new Color(100,65,164));
            eb.setThumbnail("https://www-cdn.jtvnw.net/images/twitch_logo3.jpg");
            Arrays.stream(streamersConfig.split(";")).forEach(s -> {
                String[] streamer = s.split(",");
                StreamPlatform platform = StreamPlatform.valueOf(streamer[1]);
                Map<String, String> result = plugin.isStreaming(streamer[0], platform);
                String output = "";
                if (result.size() != 0) {
                    String url = "";
                    if (platform == StreamPlatform.TWITCH) {
                        url = "https://twitch.tv/" + streamer[0];
                    } else if (platform == StreamPlatform.MIXER) {
                        url = "https://mixer.com/" + streamer[0];
                    }
                    output = "[" + result.get(StreamersPlugin.STATUS_KEY) + " in " + result.get(StreamersPlugin.GAME_KEY) + "!](" + url + ")";
                } else {
                    output = streamer[0] + " is not streaming!";
                }
                eb.addField(streamer[0],output, false);
            });
            event.getChannel().sendMessage(eb.build()).queue();
        } else {
            event.getChannel().sendMessage("No streamers on this server!").queue();
        }

    }

    @Override
    public String help() {
        return "Return the list of streamers of this Discord server and their status.";
    }

    @Override
    public String shortDescription() {
        return "Return the list of streamers of this Discord server and their status.";
    }
}
