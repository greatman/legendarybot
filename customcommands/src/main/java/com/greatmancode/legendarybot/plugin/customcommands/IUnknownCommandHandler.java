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
package com.greatmancode.legendarybot.plugin.customcommands;

import com.greatmancode.legendarybot.api.commands.UnknownCommandHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class IUnknownCommandHandler implements UnknownCommandHandler {

    private CustomCommandsPlugin plugin;

    public IUnknownCommandHandler(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        String[] split = event.getMessage().getContent().split(" ");
        String value = split[0].substring(1).toLowerCase();
        String result = plugin.getServerCommands(event.getGuild()).get(value);
        if (result != null) {
            if (result.contains(".png") || result.contains(".jpg") || result.contains(".gif")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setImage(result);
                eb.build();

                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage(result).queue();
            }
        }
    }
}
