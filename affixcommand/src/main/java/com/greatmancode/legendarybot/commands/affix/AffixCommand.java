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
package com.greatmancode.legendarybot.commands.affix;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Weeks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

public class AffixCommand extends LegendaryBotPlugin implements PublicCommand, ZeroArgsCommand {

    private final static DateTime startDateMythicPlus = new DateTime(2017,3,28,0,0, DateTimeZone.forID("America/Montreal"));
    private final static String[][] mythicPlusAffixes = {
            {"Raging","Volcanic","Tyrannical"},
            {"Teeming","Explosive","Fortified"},
            {"Bolstering","Grievous","Tyrannical"},
            {"Sanguine", "Volcanic","Fortified"},
            {"Bursting", "Skittish", "Tyrannical"},
            {"Teeming","Quaking","Fortified"},
            {"Raging", "Necrotic","Tyrannical"},
            {"Bolstering", "Skittish", "Fortified"},
            {"Teeming", "Necrotic", "Tyrannical"},
            {"Sanguine","Grievous", "Fortified"},
            {"Bolstering", "Explosive", "Tyrannical"},
            {"Unknown", "Unknown", "Unknown"}
    };

    private static final Logger log = LoggerFactory.getLogger(AffixCommand.class);

    public AffixCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        DateTime current = new DateTime(DateTimeZone.forID("America/Montreal"));
        while (current.getDayOfWeek() != DateTimeConstants.TUESDAY) {
            current = current.minusDays(1);
        }
        int weeks = Weeks.weeksBetween(startDateMythicPlus, current).getWeeks();
        String[] weekAffixes = mythicPlusAffixes[weeks % 12];
        event.getChannel().sendMessage("This week affixes: " + weekAffixes[0] + ", " + weekAffixes[1] + ", " + weekAffixes[2]).queue();
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("affix", this);
        log.info("Command !affix loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("affix");
        log.info("Command !affix unloaded");
    }

    @Override
    public String help() {
        return "!affix - Return this week's affixes.";
    }
}
