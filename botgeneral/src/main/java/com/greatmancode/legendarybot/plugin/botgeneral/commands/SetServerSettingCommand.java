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

package com.greatmancode.legendarybot.plugin.botgeneral.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.api.utils.BattleNet;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Collections;

/**
 * Command to set guild's specific settings to the bot.
 */
public class SetServerSettingCommand extends AdminCommand {

    /**
     * A instance of the Bot
     */
    private LegendaryBot bot;

    /**
     * Instantiate a class of the setserversetting command
     * @param legendaryBot A {@link LegendaryBot} instance of the bot.
     */
    public SetServerSettingCommand(LegendaryBot legendaryBot) {
        this.bot = legendaryBot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String[] argsend = new String[args.length - 1];
        System.arraycopy(args,1,argsend,0,args.length - 1);
        StringBuilder builder = new StringBuilder();
        for(String s : argsend) {
            builder.append(" ").append(s);
        }
        String setting = builder.toString().trim();

        if (args[0].equalsIgnoreCase("WOW_REGION_NAME") && !setting.equalsIgnoreCase("EU") && !setting.equalsIgnoreCase("US")) {
            event.getChannel().sendMessage("The only valid setting for WOW_REGION_NAME are US or EU.").queue();
            return;
        }

        if (args[0].equalsIgnoreCase("WOW_SERVER_NAME")) {
            if (bot.getGuildSettings(event.getGuild()).getRegionName() == null) {
                event.getChannel().sendMessage("Please set WOW_REGION_NAME first.").queue();
                return;
            }
            try {
                HttpEntity entity = new NStringEntity("{ \"query\": { \"match\" : { \"name\" : \""+setting+"\" } } }", ContentType.APPLICATION_JSON);
                Response response = bot.getElasticSearch().performRequest("POST", "/wow/realm_"+bot.getGuildSettings(event.getGuild()).getRegionName().toLowerCase()+"/_search", Collections.emptyMap(), entity);
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JSONParser jsonParser = new JSONParser();
                JSONObject obj = (JSONObject) jsonParser.parse(jsonResponse);
                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                if (hit.size() == 0) {
                    event.getChannel().sendMessage("No server found!").queue();
                    return;
                }
                JSONObject firstItem = (JSONObject) hit.get(0);
                JSONObject source = (JSONObject)  firstItem.get("_source");
                String serverSlug = (String) source.get("slug");
                bot.getGuildSettings(event.getGuild()).setSetting("WOW_SERVER_NAME", serverSlug);
                event.getChannel().sendMessage("Setting WOW_SERVER_NAME set!").queue();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("No server found!").queue();
                return;
            }
        }

        if (args[0].equalsIgnoreCase("GUILD_NAME")) {
            if (bot.getGuildSettings(event.getGuild()).getRegionName() != null &&
                    bot.getGuildSettings(event.getGuild()).getWowServerName() != null) {
                try {
                    if (!BattleNet.guildExist(bot.getGuildSettings(event.getGuild()).getRegionName(),bot.getGuildSettings(event.getGuild()).getWowServerName(), setting)) {
                        event.getChannel().sendMessage("Guild not found! Check if your region and/or your server name is properly configured!").queue();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    bot.getStacktraceHandler().sendStacktrace(e);
                    event.getChannel().sendMessage("An error occured. Try again later!");
                }
            } else {
                event.getChannel().sendMessage("Please fill out the WOW_SERVER_NAME and the WOW_REGION_NAME first.").queue();
                return;
            }
        }


        bot.getGuildSettings(event.getGuild()).setSetting(args[0], setting);
        event.getChannel().sendMessage("Setting " + args[0] + " set!").queue();
    }

    @Override
    public int minArgs() {
        return 2;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help() {
        return "setserversetting [Setting Name] [Setting Value] - Set a server setting";
    }
}
