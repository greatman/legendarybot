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

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * !addstreamers command - Add a streamer to the Guild
 */
public class AddStreamerCommand extends AdminCommand {

    /**
     * The {@link StreamersPlugin} instance.
     */
    private StreamersPlugin plugin;

    public AddStreamerCommand(StreamersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        try {
            StreamPlatform platform = StreamPlatform.valueOf(args[1].toUpperCase());
            if (plugin.streamerExist(args[0], platform)) {
                plugin.addStreamer(event.getGuild(), args[0], platform);
                event.getChannel().sendMessage("Streamer " + args[0] + " added!").queue();
            } else {
                event.getChannel().sendMessage("Streamer not found!").queue();
            }
        } catch (IllegalArgumentException e) {
            event.getChannel().sendMessage("Invalid platform!").queue();
        }
    }

    @Override
    public int minArgs() {
        return 2;
    }

    @Override
    public int maxArgs() {
        return 2;
    }

    @Override
    public String help() {
        return "[Streamer username] [Platform (Twitch/Mixer)] - Add a streamer to the streamer list.\n\n" +
                "__Parameters__\n" +
                "**Streamer username** (Required): The name of the streamer.\n" +
                "**Platform** (Required). Either Twitch or Mixer.\n\n" +
                "**Example**: ``!addstreamer greamtan mixer``";
    }

    @Override
    public String shortDescription() {
        return "Add a streamer to the streamer list.";
    }
}
