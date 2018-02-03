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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.simple.parser.ParseException;

import java.io.IOException;

/**
 * Command that provides this week's Mythic+ Affixes
 */
public class AffixCommand implements PublicCommand, ZeroArgsCommand {

    private AffixPlugin plugin;

    public AffixCommand(AffixPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (plugin.getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(),"server.region.must.be.set")).queue();
        }
        try {
            event.getChannel().sendMessage(Utils.createMythicEmbed(plugin.getBot(), event.getGuild(), plugin.getWeekAffixes(plugin.getBot().getGuildSettings(event.getGuild()).getRegionName())).build()).queue();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            event.getChannel().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(), "error.occurred.try.again.later")).queue();
        }
    }

    @Override
    public String help(Guild guild) {
        return plugin.getBot().getTranslateManager().translate(guild, "command.affix.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return plugin.getBot().getTranslateManager().translate(guild, "command.affix.help");
    }
}
