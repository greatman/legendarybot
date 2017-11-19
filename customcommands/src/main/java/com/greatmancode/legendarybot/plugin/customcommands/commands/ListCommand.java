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
package com.greatmancode.legendarybot.plugin.customcommands.commands;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.plugin.customcommands.CustomCommandsPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Map;

public class ListCommand implements PublicCommand, ZeroArgsCommand {

    private CustomCommandsPlugin plugin;

    public ListCommand(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        Map<String,String> commands = plugin.getServerCommands(event.getGuild());

        StringBuilder builder = new StringBuilder();
        builder.append("Customs commands set on this server:\n");
        String prefix = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("PREFIX");
        if (prefix == null) {
            prefix = "!";
        }
        String finalPrefix = prefix;
        commands.forEach((k,v) -> {
            builder.append(finalPrefix);
            builder.append(k);
            builder.append("\n");
        });
        event.getAuthor().openPrivateChannel().complete().sendMessage(builder.toString()).queue();
    }

    @Override
    public String help() {
        return "List all the custom commands of the server.";
    }

    @Override
    public String shortDescription() {
        return help();
    }
}
