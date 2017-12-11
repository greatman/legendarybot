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
import org.pf4j.PluginWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * The Setup Plugin main class.
 */
public class SetupPlugin extends LegendaryBotPlugin {


    /**
     * A Map containing all running setup wizards.
     */
    private Map<Guild,SetupHandler> setupHandlerMap = new HashMap<>();

    /**
     * The MessageListener that handle all the setup steps.
     */
    private SetupMessageListener setupMessageListener = new SetupMessageListener(this);

    public SetupPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getJDA().forEach(jda ->jda.addEventListener(setupMessageListener));
        getBot().getCommandHandler().addCommand("setup", new SetupCommand(this), "Admin Commands");
    }

    @Override
    public void stop() {
        getBot().getJDA().forEach(jda -> jda.removeEventListener(setupMessageListener));
        getBot().getCommandHandler().removeCommand("setup");
    }

    /**
     * Retrieve the setup handler of a guild.
     * @param guild The guild to retrieve the setup handler from.
     * @return The {@link SetupHandler} instance of the guild. If none found returns null.
     */
    public SetupHandler getSetupHandler(Guild guild) {
        return setupHandlerMap.get(guild);
    }

    /**
     * Remove the {@link SetupHandler} of a guild.
     * @param guild The guild to remove the {@link SetupHandler} from
     */
    public void setupDone(Guild guild) {
        setupHandlerMap.remove(guild);
    }

    /**
     * Add a {@link SetupHandler} for a guild
     * @param guild The guild to add the {@link SetupHandler} to.
     * @param handler The {@link SetupHandler} instance.
     */
    public void addSetupHandler(Guild guild, SetupHandler handler) {
        setupHandlerMap.put(guild, handler);
    }
}
