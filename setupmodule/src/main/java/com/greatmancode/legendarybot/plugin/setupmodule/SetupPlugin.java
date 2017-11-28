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
package com.greatmancode.legendarybot.plugin.setupmodule;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.plugin.setupmodule.commands.SetupCommand;
import net.dv8tion.jda.core.entities.Guild;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.HashMap;
import java.util.Map;

public class SetupPlugin extends LegendaryBotPlugin {


    private Map<Guild,SetupHandler> setupHandlerMap = new HashMap<>();
    private SetupMessageListener setupMessageListener = new SetupMessageListener(this);

    public SetupPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        getBot().getJDA().forEach(jda ->jda.addEventListener(setupMessageListener));
        getBot().getCommandHandler().addCommand("setup", new SetupCommand(this), "Admin Commands");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getJDA().forEach(jda -> jda.removeEventListener(setupMessageListener));
        getBot().getCommandHandler().removeCommand("setup");
    }

    public SetupHandler getSetupHandler(Guild guild) {
        return setupHandlerMap.get(guild);
    }

    public void setupDone(Guild guild) {
        setupHandlerMap.remove(guild);
    }

    public void addSetupHandler(Guild guild, SetupHandler handler) {
        setupHandlerMap.put(guild, handler);
    }
}
