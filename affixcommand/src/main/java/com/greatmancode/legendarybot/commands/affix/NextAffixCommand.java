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

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Weeks;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides next week's affix.
 */
public class NextAffixCommand implements PublicCommand, ZeroArgsCommand {

    private LegendaryBot bot;

    public NextAffixCommand(LegendaryBot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        DateTime current = new DateTime(DateTimeZone.forID("America/Montreal"));
        if (current.getDayOfWeek() == DateTimeConstants.TUESDAY) {
            current = current.plusDays(1);
        }
        while (current.getDayOfWeek() != DateTimeConstants.TUESDAY) {
            current = current.plusDays(1);
        }
        int weeks = Weeks.weeksBetween(Utils.startDateMythicPlus, current).getWeeks();
        String[] weekAffixes = Utils.mythicPlusAffixes[weeks % 12];
        Map<Long, String> map = new HashMap<>();
        map.put((long) 4, weekAffixes[0]);
        map.put((long) 7, weekAffixes[1]);
        map.put((long) 10, weekAffixes[2]);
        event.getChannel().sendMessage(Utils.createMythicEmbed(bot, event.getGuild(), map).build()).queue();
    }

    @Override
    public String help(Guild guild) {
        return bot.getTranslateManager().translate(guild, "command.nextaffix.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return bot.getTranslateManager().translate(guild, "command.nextaffix.help");
    }
}
