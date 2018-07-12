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
package com.greatmancode.legendarybot.plugin.lookupcommands.commands;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.plugin.lookupcommands.LookupCommandsPlugin;
import net.dv8tion.jda.core.entities.Guild;
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
 * The !lookupachivement command
 */
public class LookupAchievementCommand implements PublicCommand {

    /**
     * An instance of the Lookup Commands plugin.
     */
    private LookupCommandsPlugin plugin;

    public LookupAchievementCommand(LookupCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String query = String.join(" ", args);
        try {
            HttpEntity entity = new NStringEntity("{ \"query\": { \"match\" : { \"title\" : \""+query+"\" } } }", ContentType.APPLICATION_JSON);
            Response response = plugin.getBot().getElasticSearch().performRequest("POST", "/wow/achievement/_search", Collections.emptyMap(), entity);

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject obj = (JSONObject) jsonParser.parse(EntityUtils.toString(response.getEntity()));
                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                JSONObject firstItem = (JSONObject) hit.get(0);
                
                String whLocatedDomain = "www";
                switch(plugin.getBot().getTranslateManager().getLanguages())
                {
                    case "fr":
                        whLocatedDomain = "fr";
                        break;
                        
                    case "ru":
                        whLocatedDomain = "ru";
                        break;
                        
                    case "it":
                        whLocatedDomain = "it";
                        break;
                        
                    case "de":
                        whLocatedDomain = "de";
                        break;
                        
                    case "es":
                        whLocatedDomain = "es";
                        break;
                        
                    case "pt":
                        whLocatedDomain = "pt";
                        break;
                        
                    case "ko":
                        whLocatedDomain = "ko";
                        break;
                        
                    case "cn":
                        whLocatedDomain = "cn";
                        break;
                        
                    default:
                        whLocatedDomain = "www";
                        break;
                }
                
                event.getChannel().sendMessage("http://" + whLocatedDomain + ".wowhead.com/achievement=" + firstItem.get("_id")).queue();
            } catch (ParseException e) {
                e.printStackTrace();
                event.getChannel().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(), "command.lookupachievement.notfound")).queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getBot().getStacktraceHandler().sendStacktrace(e,"query:" + query);
            event.getChannel().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(), "error.occurred.try.again.later")).queue();
        }
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help(Guild guild) {
        return plugin.getBot().getTranslateManager().translate(guild, "command.lookupachievement.longhelp");
    }

    @Override
    public String shortDescription(Guild guild) {
        return plugin.getBot().getTranslateManager().translate(guild, "command.lookupachievement.shorthelp");
    }
}
