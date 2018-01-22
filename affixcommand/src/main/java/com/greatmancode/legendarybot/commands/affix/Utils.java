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
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Several utils to calculate what affixes are current online
 */
public class Utils {

    /**
     * The start date of Mythic+ dungeons, helps to know which week we are.
     */
    public static final DateTime startDateMythicPlus = new DateTime(2017,3,28,0,0, DateTimeZone.forID("America/Montreal"));

    /**
     * Contains all the prefix descriptions/difficulty.
     */
    public static final Map<String,AffixDescription> affixDescription = new HashMap<>();

    static {
        affixDescription.put("Overflowing", new AffixDescription(1, 1));
        affixDescription.put("Skittish", new AffixDescription(2, 2));
        affixDescription.put("Volcanic",new AffixDescription(3, 0));
        affixDescription.put("Necrotic", new AffixDescription(4, 2));
        affixDescription.put("Teeming", new AffixDescription(5, 1));
        affixDescription.put("Raging", new AffixDescription(6, 1));
        affixDescription.put("Bolstering", new AffixDescription(7,1));
        affixDescription.put("Sanguine", new AffixDescription(8, 0));
        affixDescription.put("Tyrannical", new AffixDescription(9, 2));
        affixDescription.put("Fortified", new AffixDescription(10, 2));
        affixDescription.put("Bursting", new AffixDescription(11, 1));
        affixDescription.put("Grievous", new AffixDescription(12, 1));
        affixDescription.put("Explosive", new AffixDescription(13, 1));
        affixDescription.put("Quaking", new AffixDescription(14, 1));
    }

    /**
     * All the Mythic+ affixes groups.
     */
    public static final String[][] mythicPlusAffixes = {
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
            {"Bursting", "Quaking", "Fortified"}
    };

    /**
     * Create an embed of a Mythic week
     * @param weekAffixes The affixes to add to the embed.
     * @return A {@link EmbedBuilder} to send to the user.
     */
    public static EmbedBuilder createMythicEmbed(LegendaryBot bot, Guild guild, String[] weekAffixes) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setThumbnail("http://wow.zamimg.com/images/wow/icons/large/inv_relics_hourglass.jpg");

        eb.setFooter(bot.getTranslateManager().translate(guild,"mythicplus.affixes"), "http://wow.zamimg.com/images/wow/icons/large/inv_relics_hourglass.jpg");
        AffixDescription affix4 = Utils.affixDescription.get(weekAffixes[0]);
        AffixDescription affix7 = Utils.affixDescription.get(weekAffixes[1]);
        AffixDescription affix10 = Utils.affixDescription.get(weekAffixes[2]);
        int difficulty = affix4.getDifficulty() + affix7.getDifficulty() + affix10.getDifficulty();

        if (difficulty <= 3) {
            eb.setColor(Color.GREEN);
        } else if (difficulty == 4) {
            eb.setColor(Color.YELLOW);
        } else {
            eb.setColor(Color.RED);
        }
        //TODO link the proper language wowhead site for the language of the guild
        eb.addField("(4) " + bot.getTranslateManager().translate(guild,"affix."+weekAffixes[0].toLowerCase()+".name"),bot.getTranslateManager().translate(guild, "affix."+weekAffixes[0].toLowerCase() + ".description") + "\n[" + bot.getTranslateManager().translate(guild, "more.info")+"](http://www.wowhead.com/affix="+affix4.getId()+")",false);
        eb.addField("(7) " +bot.getTranslateManager().translate(guild,"affix."+weekAffixes[1].toLowerCase()+".name"),bot.getTranslateManager().translate(guild, "affix."+weekAffixes[1].toLowerCase() + ".description") + "\n[" + bot.getTranslateManager().translate(guild, "more.info")+"](http://www.wowhead.com/affix="+affix7.getId()+")",false);
        eb.addField("(10) " + bot.getTranslateManager().translate(guild,"affix."+weekAffixes[2].toLowerCase()+".name"),bot.getTranslateManager().translate(guild, "affix."+weekAffixes[2].toLowerCase() + ".description") + "\n[" + bot.getTranslateManager().translate(guild, "more.info")+"](http://www.wowhead.com/affix="+affix10.getId()+")",false);
        return eb;
    }
}
