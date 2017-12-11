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

package com.greatmancode.legendarybot.plugin.botgeneral;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.plugin.botgeneral.commands.HelpCommand;
import com.greatmancode.legendarybot.plugin.botgeneral.commands.InfoCommand;
import com.greatmancode.legendarybot.plugin.botgeneral.commands.InviteCommand;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.pf4j.PluginWrapper;

/**
 * Plugin containing some general commands for the bot.
 */
public class BotGeneralPlugin extends LegendaryBotPlugin {

    /**
     * A Event Listener to log to the bot's discord log channel.
     */
    private MessageListener listener;


    public BotGeneralPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("invite", new InviteCommand(), "General Commands");
        log.info("Command !invite loaded!");
        getBot().getCommandHandler().addCommand("help", new HelpCommand(getBot()), "General Commands");
        log.info("Command !help loaded!");
        getBot().getCommandHandler().addCommand("info", new InfoCommand(), "General Commands");

        //We find our main guild
        Guild guild = null;
        for (JDA jda : getBot().getJDA()) {
            Guild guildEntry = jda.getGuildById("330748360673722381");
            if (guildEntry != null) {
                guild = guildEntry;
            }
        }
        listener = new MessageListener(guild);
        getBot().getJDA().forEach(jda -> jda.addEventListener(listener));
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("invite");
        log.info("Command !invite unloaded.");
        getBot().getCommandHandler().removeCommand("help");
        log.info("Command !help unloaded.");
        getBot().getCommandHandler().removeCommand("info");
        getBot().getJDA().forEach(jda -> jda.removeEventListener(listener));
    }
}
