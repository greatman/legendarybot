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
package com.greatmancode.legendarybot.plugin.lookupcommands;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.plugin.lookupcommands.commands.LookupAchievementCommand;
import com.greatmancode.legendarybot.plugin.lookupcommands.commands.LookupItemCommand;
import com.greatmancode.legendarybot.plugin.lookupcommands.commands.LookupQuestCommand;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * The Lookup Commands Plugin main class.
 */
public class LookupCommandsPlugin extends LegendaryBotPlugin {

    public LookupCommandsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("lookupitem", new LookupItemCommand(this), "World of Warcraft");
        log.info("Command !lookupitem loaded!");
        getBot().getCommandHandler().addCommand("lookupachievement", new LookupAchievementCommand(this), "World of Warcraft");
        log.info("Command !lookupachievement loaded!");
        getBot().getCommandHandler().addCommand("lookupquest", new LookupQuestCommand(this), "World of Warcraft");
        log.info("Command !lookupquest loaded!");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("lookupitem");
        log.info("Command !lookupitem unloaded!");
        getBot().getCommandHandler().removeCommand("lookupachievement");
        log.info("Command !lookupachievement unloaded!");
        getBot().getCommandHandler().removeCommand("lookupquest");
        log.info("Command !lookupquest unloaded!");
    }
}
