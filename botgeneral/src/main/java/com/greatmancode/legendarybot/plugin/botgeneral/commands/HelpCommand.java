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
import com.greatmancode.legendarybot.api.commands.Command;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Command to show all the command's help available to the executor.
 */
public class HelpCommand implements PublicCommand {

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
        if (args.length == 1 && bot.getCommandHandler().getCommandList().containsKey(args[0])) {
            event.getAuthor().openPrivateChannel().complete().sendMessage(bot.getCommandHandler().getCommandList().get(args[0]).help(event.getGuild())).queue();
            return;
        }
        final MessageBuilder[] builder = {new MessageBuilder()};
        String prefix = bot.getGuildSettings(event.getGuild()).getSetting("PREFIX");
        if (prefix == null) {
            prefix = "!";
        }
        String finalPrefix = prefix;
        builder[0].append(bot.getTranslateManager().translate(event.getGuild(),"command.help.firstline") + "\n");
        builder[0].append(bot.getTranslateManager().translate(event.getGuild(), "command.help.secondline", prefix));
        bot.getCommandHandler().getCommandGroup().forEach((k,v) -> {
            if (builder[0].length() >= 1700) {
                event.getAuthor().openPrivateChannel().complete().sendMessage(builder[0].build()).queue();
                builder[0] = new MessageBuilder();
            }
            builder[0].append("\n__");
            builder[0].append(k);
            builder[0].append("__\n");
            v.forEach((commandName) -> {
                if (builder[0].length() >= 1700) {
                    event.getAuthor().openPrivateChannel().complete().sendMessage(builder[0].build()).queue();
                    builder[0] = new MessageBuilder();
                }
                Command command = bot.getCommandHandler().getCommandList().get(commandName);
                if (command.canExecute(event.getMember())) {
                    builder[0].append("**");
                    builder[0].append(finalPrefix);
                    builder[0].append(commandName);
                    builder[0].append("**: ");
                    builder[0].append(command.shortDescription(event.getGuild()));
                    builder[0].append("\n");
                }
            });
        });
        event.getAuthor().openPrivateChannel().complete().sendMessage(builder[0].build()).queue();
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
    public String help(Guild guild) {
        if (bot.getGuildSettings(guild).getSetting("PREFIX") != null) {
            return bot.getTranslateManager().translate(guild, "command.help.help", bot.getGuildSettings(guild).getSetting("PREFIX"));
        } else {
            return bot.getTranslateManager().translate(guild, "command.help.help", "!");
        }
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
