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
package com.greatmancode.legendarybot.plugin.setupmodule.commands;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugin.setupmodule.SetupHandler;
import com.greatmancode.legendarybot.plugin.setupmodule.SetupPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SetupCommand extends AdminCommand {

    private SetupPlugin plugin;

    public SetupCommand(SetupPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        plugin.addSetupHandler(event.getGuild(), new SetupHandler(event.getAuthor(), event.getChannel()));
        StringBuilder builder = new StringBuilder();
        builder.append("Thanks for installing Legendarybot! This wizard will help you with the basic configuration of the bot.\n");
        builder.append("It will only take a couple of minutes. If you wish to stop the setup wizard, just say ``cancel``\n");
        builder.append("You don't need to type any command to answer the following questions.\n");
        builder.append("========================================================================\n");
        event.getChannel().sendMessage(builder.toString()).queue();

        builder = new StringBuilder();
        builder.append("A **prefix** is the start of a message that the bot will recognize as a command.\n");
        builder.append("As an example, ``!help`` ``!`` is the prefix and ``help`` is the command.\n");
        builder.append("If you're running more than one Discord bot, you likely don't want two bots answering to the same command.\n\n");
        builder.append("**Default Value**: ``!``\n");

        String prefix = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("PREFIX");
        if (prefix == null) {
            prefix = "!";
        }
        builder.append("**Current Setting**: ``" + prefix + "``\n");
        builder.append("So, what **prefix** would you like to use?");
        event.getChannel().sendMessage(builder.toString()).queue();

    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 0;
    }

    @Override
    public String help() {
        return "Setup LegendaryBot for your server.";
    }

    @Override
    public String shortDescription() {
        return "Setup LegendaryBot for your server.";
    }
}
