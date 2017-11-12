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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.joda.time.Weeks;

import java.awt.*;

/**
 * Command that provides this week's Mythic+ Affixes
 */
public class AffixCommand implements PublicCommand, ZeroArgsCommand {

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        DateTime current = new DateTime(DateTimeZone.forID("America/Montreal"));
        while (current.getDayOfWeek() != DateTimeConstants.TUESDAY) {
            current = current.minusDays(1);
        }
        int weeks = Weeks.weeksBetween(Utils.startDateMythicPlus, current).getWeeks();
        String[] weekAffixes = Utils.mythicPlusAffixes[weeks % 12];

        event.getChannel().sendMessage(Utils.createMythicEmbed(weekAffixes).build()).queue();
    }

    @Override
    public String help() {
        return "Return this week's affixes.";
    }

    @Override
    public String shortDescription() {
        return "Return this week's affixes.";
    }
}
