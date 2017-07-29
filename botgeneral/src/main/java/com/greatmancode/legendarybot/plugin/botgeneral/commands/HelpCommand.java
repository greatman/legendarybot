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

package com.greatmancode.legendarybot.plugin.botgeneral.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Command to show all the command's help available to the executor.
 */
public class HelpCommand implements PublicCommand,ZeroArgsCommand {

    /**
     * A instance of the bot
     */
    private LegendaryBot bot;

    /**
     * Instantiate a version of the help command
     * @param bot A {@link LegendaryBot} instance of the bot.
     */
    public HelpCommand(LegendaryBot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        MessageBuilder builder = new MessageBuilder();
        String prefix = bot.getGuildSettings(event.getGuild()).getSetting("PREFIX");
        if (prefix == null) {
            prefix = "!";
        }
        builder.append("Available commands ([] - Required, <> - Optional):\n");
        String finalPrefix = prefix;
        bot.getCommandHandler().getCommandList().forEach((k, v) -> {
            if (v.canExecute(event.getMember())) {
                builder.append(finalPrefix + v.help());
                builder.append("\n");
            }
        });
        event.getAuthor().openPrivateChannel().complete().sendMessage(builder.build()).queue();
    }

    @Override
    public String help() {
        return "help - Return this help";
    }
}
