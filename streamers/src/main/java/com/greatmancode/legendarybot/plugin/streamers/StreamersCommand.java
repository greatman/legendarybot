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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

import java.awt.*;
import java.util.Map;


/**
 * The !streamers command. Lists all the streamers and their status.
 */
public class StreamersCommand implements PublicCommand {

    /**
     * The {@link StreamersPlugin} instance.
     */
    private StreamersPlugin plugin;

    public StreamersCommand(StreamersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String streamersSetting = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("streamers");

        if (streamersSetting == null) {
            event.getChannel().sendMessage("No streamers on this server!").queue();
            return;
        }

        JSONObject streamersJSON = new JSONObject(streamersSetting);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Server Streamers");
        eb.setColor(new Color(100,65,164));
        eb.setThumbnail("https://www-cdn.jtvnw.net/images/twitch_logo3.jpg");

        if (streamersJSON.has(StreamPlatform.MIXER.name())) {
            streamersJSON.getJSONArray(StreamPlatform.MIXER.name()).forEach(username -> {
                Map<String, String> result = plugin.isStreaming((String) username, StreamPlatform.MIXER);
                String output = null;
                if (result.size() != 0) {
                    output = "[" + result.get(StreamersPlugin.STATUS_KEY) + " in " + result.get(StreamersPlugin.GAME_KEY) + "!](https://mixer.com/" + username + ")";

                } else {
                    if (args.length == 1) {
                        output = username + " is not streaming";
                    }
                }
                if (output != null) {
                    eb.addField((String) username,output, false);
                }
            });
        }
        if (streamersJSON.has(StreamPlatform.TWITCH.name())) {
            streamersJSON.getJSONArray(StreamPlatform.TWITCH.name()).forEach(username -> {
                Map<String, String> result = plugin.isStreaming((String) username, StreamPlatform.TWITCH);
                String output = null;
                if (result.size() != 0) {
                    output = "[" + result.get(StreamersPlugin.STATUS_KEY) + " in " + result.get(StreamersPlugin.GAME_KEY) + "!](https://twitch.com/" + username + ")";

                } else {
                    if (args.length == 1) {
                        output = username + " is not streaming";
                    }
                }
                if (output != null) {
                    eb.addField((String) username,output, false);
                }
            });
        }

        event.getChannel().sendMessage(eb.build()).queue();

    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "Return the list of streamers of this Discord server and their status. Add all at the end of the command to show them all even if they are offline.";
    }

    @Override
    public String shortDescription() {
        return "Return the list of streamers of this Discord server and their status.";
    }
}
