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
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.*;

/**
 * Handle commands sent by the user
 */
public class CommandHandler {

    /**
     * The Bot instance
     */
    private LegendaryBot bot;

    /**
     * The list of all the commands registered to the bot
     */
    private Map<String, Command> commandMap = new LinkedHashMap<>();

    /**
     * The alias list
     */
    private Map<String, String> aliasMap = new LinkedHashMap<>();

    private Map<String, List<String>> commandGroup = new LinkedHashMap<>();

    /**
     * An instance of a {@link UnknownCommandHandler} to handle commands that are unknown
     */
    private UnknownCommandHandler unknownCommandHandler = null;

    /**
     * Build a CommandHandler
     * @param bot An instance of a Bot.
     */
    public CommandHandler(LegendaryBot bot) {
        this.bot = bot;
    }


    public void addAlias(String name, String command) {
        aliasMap.put(name, command);
    }

    public void removeAlias(String name) {
        aliasMap.remove(name);
    }

    /**
     * Add a command to the commands registered
     * @param name The trigger of the command
     * @param command The {@link Command} instance related to the trigger
     */
    public void addCommand(String name, Command command, String group) {
        commandMap.put(name, command);
        if (!commandGroup.containsKey(group)) {
            commandGroup.put(group, new ArrayList<>());
        }
        commandGroup.get(group).add(name);
    }

    /**
     * Remove a command from the bot
     * @param name The trigger of the command
     */
    public void removeCommand(String name) {
        commandMap.remove(name);

        commandGroup.forEach((k,v) -> v.remove(name));
    }

    /**
     * Handle a message from Discord
     * @param event The {@link MessageReceivedEvent} from JDA.
     */
    public void handle(MessageReceivedEvent event) {
        String text = event.getMessage().getContent();
        String prefix = bot.getGuildSettings(event.getGuild()).getSetting("PREFIX");
        if (prefix == null) {
            prefix = "!";
        }
        if (text.startsWith(prefix) || event.getMessage().isMentioned(event.getJDA().getSelfUser())) {
            String command = null;
            String[] commandArray = text.split(" ");
            if (text.startsWith(prefix)) {
                command = commandArray[0].substring(prefix.length()).toLowerCase();
            } else {
                command = commandArray[1].toLowerCase();
            }

            if (commandMap.containsKey(command) || aliasMap.containsKey(command)) {
                Command commandClass = null;
                if (commandMap.containsKey(command)) {
                    commandClass = commandMap.get(command);
                } else if (aliasMap.containsKey(command) && commandMap.containsKey(aliasMap.get(command))) {
                    commandClass = commandMap.get(aliasMap.get(command));
                }

                if (commandClass == null) {
                    return;
                }

                if (commandClass.canExecute(event.getMember())) {
                    String[] args = null;
                    if (text.startsWith(prefix)) {
                        args = new String[commandArray.length - 1];
                    } else {
                        args = new String[commandArray.length - 2];
                    }
                    try {
                        args = commandClass.preFlight(event, bot, args);
                        if (args.length >= commandClass.minArgs() && args.length <= commandClass.maxArgs()) {
                            if (text.startsWith(prefix)) {
                                System.arraycopy(commandArray, 1, args,0,commandArray.length - 1);
                            } else {
                                System.arraycopy(commandArray, 2, args,0,commandArray.length - 2);
                            }


                            bot.getStatsClient().incrementCounter("legendarybot.commands","command:"+command);
                            commandClass.execute(event, args);
                        } else {
                            sendMessage(event,prefix + commandClass.help());
                        }
                    } catch (PermissionException e) {
                        sendMessage(event, "Unfortunately, I can't send a message to the channel you did " + commandArray[0] + ".");
                    }

                }
            } else if (unknownCommandHandler != null) {
                unknownCommandHandler.handle(event);
            }

        }
    }

    /**
     * Send a private message to the author of the Message event
     * @param event The {@link MessageReceivedEvent} from JDA
     * @param message The message to send to the author.
     */
    private void sendMessage(MessageReceivedEvent event, String message) {
        event.getAuthor().openPrivateChannel().complete().sendMessage(message).queue();
    }

    /**
     * Get a unmodifiable map of the registered commands.
     * @return a {@link Map} containing as a key the command trigger and the value the Command handler.
     */
    public Map<String, Command> getCommandList() {
        return Collections.unmodifiableMap(commandMap);
    }

    /**
     * Set the {@link UnknownCommandHandler}.
     * @param handler The {@link UnknownCommandHandler} to set to.
     */
    public void setUnknownCommandHandler(UnknownCommandHandler handler) {
        this.unknownCommandHandler = handler;
    }

    public Map<String, List<String>> getCommandGroup() {
        return Collections.unmodifiableMap(commandGroup);
    }

}
