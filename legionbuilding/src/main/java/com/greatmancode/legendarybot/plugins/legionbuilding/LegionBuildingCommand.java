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
package com.greatmancode.legendarybot.plugins.legionbuilding;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The !legionbuilding command
 */
public class LegionBuildingCommand extends LegendaryBotPlugin implements PublicCommand, ZeroArgsCommand {

    public LegionBuildingCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "server.region.must.be.set")).queue();
            return;
        }
        List<String> buildingStatus = new ArrayList<>();
        List<String> buildingStatusString = new ArrayList<>();
        try {
            Document document = Jsoup.connect("http://www.wowhead.com/").get();
            String region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            Element element;
            if (region.equals("eu")) {
                element = document.getElementsByClass("tiw-region tiw-region-EU").first();
            } else {
                element = document.getElementsByClass("tiw-region tiw-region-US tiw-show").first();
            }

            element.getElementsByClass("tiw-group tiw-bs-building").stream().forEach(building -> {
                buildingStatusString.add(building.getElementsByClass("imitation-heading heading-size-5").first().ownText());
                buildingStatus.add(building.getElementsByClass("tiw-bs-status-progress").first().getElementsByTag("span").first().ownText());
            });
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(),"command.legionbuilding.message",
                    getBot().getTranslateManager().translate(event.getGuild(),buildingStatusString.get(0).replaceAll(" ",".").toLowerCase()),
                    buildingStatus.get(0),
                    getBot().getTranslateManager().translate(event.getGuild(),buildingStatusString.get(1).replaceAll(" ",".").toLowerCase()),
                    buildingStatus.get(1),
                    getBot().getTranslateManager().translate(event.getGuild(),buildingStatusString.get(2).replaceAll(" ",".").toLowerCase()),
                    buildingStatus.get(2))).queue();
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + getBot().getGuildSettings(event.getGuild()).getRegionName(), "guildId:" + event.getGuild().getId());
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "error.occurred.try.again.later")).queue();
        } catch (NullPointerException e) {
            e.printStackTrace();
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(),"error.occurred.try.again.later")).queue();
        }

    }

    @Override
    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.legionbuilding.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("legionbuilding", this, "World of Warcraft");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("legionbuilding");
    }
}
