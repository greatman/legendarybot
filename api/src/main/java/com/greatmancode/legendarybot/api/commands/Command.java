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
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * Represents a command
 */
public interface Command {

    /**
     * Execute the command
     * @param event The Discord event that triggered this command
     * @param args The arguments that followed the command
     */
    void execute(MessageReceivedEvent event, String[] args);

    /**
     * Verify if the executor of the command can run it
     * @param member The executor of the command
     * @return True if the Discord user can execute the command, else false
     */
    boolean canExecute(Member member);

    /**
     * The minimum number of arguments this command take
     * @return a integer containing the number of arguments needed
     */
    int minArgs();

    /**
     * The maximum number of arguments this command take
     * @return a integer containing the number of arguments maximum
     */
    int maxArgs();

    /**
     * Returns the help for the command
     * @return A String containing the help information about the command.
     */
    String help();

    /**
     * Returns a short description of the command
     * @return A String containing the short description.
     */
    String shortDescription();

    /**
     * Execute code before the actual command being run
     * @param event The {@link MessageReceivedEvent} event.
     * @param bot The instance of the bot
     * @param args The arguments for the command
     * @return A String array of arguments.
     */
    default String[] preFlight(MessageReceivedEvent event, LegendaryBot bot, String[] args) {
        return args;
    }
}

