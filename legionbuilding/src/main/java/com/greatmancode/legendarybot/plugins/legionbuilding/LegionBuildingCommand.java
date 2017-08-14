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
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LegionBuildingCommand extends LegendaryBotPlugin implements PublicCommand, ZeroArgsCommand {

    public LegionBuildingCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        List<String> buildingStatus = new ArrayList<>();
        try {
            Document document = Jsoup.connect("http://www.wowhead.com/").get();
            int skip = (!getBot().getGuildSettings(event.getGuild()).getRegionName().equalsIgnoreCase("US")) ? 3 : 0;
            document.getElementsByClass("tiw-bs-status-progress").stream().skip(skip).forEach(element ->
                    element.getElementsByTag("span").forEach( value -> buildingStatus.add(value.ownText())));
            event.getChannel().sendMessage("Broken Shore building status: Mage Tower : **" + buildingStatus.get(0) + "** | Command Center: **" + buildingStatus.get(1) + "** | Nether Disruptor : **" + buildingStatus.get(2) + "**").queue();
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + getBot().getGuildSettings(event.getGuild()).getRegionName(), "guildId:" + event.getGuild().getId());
            event.getChannel().sendMessage("An error occured. Try again later");
        }

    }

    @Override
    public String help() {
        return "legionbuilding - Give the current status of the Broken Shore buildings!";
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("legionbuilding", this);
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("legionbuilding");
    }
}