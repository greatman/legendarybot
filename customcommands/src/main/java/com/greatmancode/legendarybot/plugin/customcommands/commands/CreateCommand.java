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

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugin.customcommands.CustomCommandsPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CreateCommand extends AdminCommand {

    private CustomCommandsPlugin plugin;

    public CreateCommand(CustomCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String[] messages = new String[args.length - 1];
        System.arraycopy(args, 1, messages,0,args.length - 1);
        plugin.createCommand(event.getGuild(),args[0].toLowerCase(), String.join(" ", messages));
        event.getChannel().sendMessage("Message "+args[0]+" set!").queue();
    }

    @Override
    public int minArgs() {
        return 2;
    }

    @Override
    public int maxArgs() {
        return 99999;
    }

    @Override
    public String help() {
        return "This command allows you to create custom commands with the bot.\n\n" +
                "**Example**: ``!createcmd cats I love cats!`` will make the bot answer to the command ``!cats`` with the sentence ``I love cats!``.\n" +
                "**You cannot override commands like !lookup with this command**\n" +
                "If you want to change a custom command, simply type ``!createcmd`` again.";
    }

    @Override
    public String shortDescription() {
        return "Create a command that the bot will return what you typed";
    }
}
