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
package com.greatmancode.legendarybot.commands.ilvl;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNet;
import com.greatmancode.legendarybot.api.utils.Hero;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

public class IlvlCommand extends LegendaryBotPlugin implements PublicCommand {


    private static final Logger log = LoggerFactory.getLogger(IlvlCommand.class);

    public IlvlCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("ilvl", this);
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("ilvl");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = args[0];
        Hero hero;
        if (args.length == 1) {
            serverName = getBot().getServerSettings(event.getGuild()).getWowServerName();
            hero = BattleNet.getiLvl(serverName, args[0]);
        } else {
            hero = BattleNet.getiLvl(serverName, args[1]);
        }

        if (hero != null) {
            event.getChannel().sendMessage(hero.getName() + " ("+hero.getHeroClass()+" "+hero.getLevel()+") ilvl is " + hero.getEquipilvl() + "/" + hero.getIlvl()).queue();
        } else {
            event.getChannel().sendMessage("WowCharacter not found!").queue();
        }
    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 2;
    }

    public String help() {
        return  "!ilvl <Server Name> [Character Name] - Retrieve a character iLvl";
    }
}
