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
package com.greatmancode.legendarybot.commands.log;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.Utils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.Calendar;

//TODO Support EU
public class LogCommand extends LegendaryBotPlugin implements ZeroArgsCommand, PublicCommand {

    private static final Logger log = LoggerFactory.getLogger(LogCommand.class);

    public LogCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String request = Utils.doRequest("https://www.warcraftlogs.com:443/v1/reports/guild/"+ getBot().getGuildSettings(event.getGuild()).getSetting("guildName")+"/"+ getBot().getGuildSettings(event.getGuild()).getWowServerName()+"/"+getBot().getGuildSettings(event.getGuild()).getRegionName()+"?api_key=c57da16709187c207c10a92a29db78fb");

        try {
            JSONArray jsonArray = (JSONArray) Utils.jsonParser.parse(request);
            JSONObject jsonObject = (JSONObject) jsonArray.stream()
                    .toArray()[jsonArray.size() - 1];
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((Long) jsonObject.get("start"));
            event.getChannel().sendMessage("Last Log: " + jsonObject.get("title") + " by " + jsonObject.get("owner") + " at " + calendar.get(Calendar.DAY_OF_MONTH)+"/"+ (calendar.get(Calendar.MONTH) + 1)+ "/"+ calendar.get(Calendar.YEAR)+". https://www.warcraftlogs.com/reports/" + jsonObject.get("id")).queue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "!log - Retrieve the last log of the guild";
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("log", this);
        log.info("Command !log loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("log");
        log.info("Command !log unloaded");
    }
}
