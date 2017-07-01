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
package com.greatmancode.legendarybot.api.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CommandHandler {

    private LegendaryBot bot;

    Map<String, Command> commandMap = new LinkedHashMap<>();
    private UnknownCommandHandler unknownCommandHandler = null;

    public CommandHandler(LegendaryBot bot) {
        this.bot = bot;
    }

    public void addCommand(String name, Command command) {
        commandMap.put(name, command);
    }

    public void removeCommand(String name) {
        commandMap.remove(name);
    }

    public void handle(MessageReceivedEvent event) {
        String text = event.getMessage().getContent();
        if (text.startsWith("!")) {
            String[] commandArray = text.split(" ");
            String command = commandArray[0].substring(1).toLowerCase();
            if (commandMap.containsKey(command)) {
                Command commandClass = commandMap.get(command);
                if (commandClass.canExecute(event.getMember())) {
                    String[] args = new String[commandArray.length - 1];
                    if (args.length >= commandClass.minArgs() && args.length <= commandClass.maxArgs()) {
                        System.arraycopy(commandArray, 1, args,0,commandArray.length - 1);
                        commandClass.execute(event, args);
                    } else {
                        sendMessage(event,commandClass.help());
                    }
                }
            } else if (unknownCommandHandler != null) {
                unknownCommandHandler.handle(event);
            }

        }
    }

    private void sendMessage(MessageReceivedEvent event, String message) {
        event.getAuthor().openPrivateChannel().complete().sendMessage(message).queue();
    }

    public Map<String, Command> getCommandList() {
        return Collections.unmodifiableMap(commandMap);
    }

    public void setUnknownCommandHandler(UnknownCommandHandler handler) {
        this.unknownCommandHandler = handler;
    }

}
