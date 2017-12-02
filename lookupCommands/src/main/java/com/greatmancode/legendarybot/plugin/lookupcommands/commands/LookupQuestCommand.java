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
 * The !lookupquest command
 */
public class LookupQuestCommand implements PublicCommand {

    /**
     * An instance of the Lookup Commands plugin.
     */
    private LookupCommandsPlugin plugin;

    public LookupQuestCommand(LookupCommandsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String query = String.join(" ", args);
        try {
            HttpEntity entity = new NStringEntity("{ \"query\": { \"match\" : { \"title\" : \""+query+"\" } } }", ContentType.APPLICATION_JSON);
            Response response = plugin.getBot().getElasticSearch().performRequest("POST", "/wow/quest/_search", Collections.emptyMap(), entity);

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject obj = (JSONObject) jsonParser.parse(EntityUtils.toString(response.getEntity()));
                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                JSONObject firstItem = (JSONObject) hit.get(0);
                event.getChannel().sendMessage("http://www.wowhead.com/quest=" + firstItem.get("_id")).queue();
            } catch (ParseException e) {
                e.printStackTrace();
                event.getChannel().sendMessage("No Quest found!").queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getBot().getStacktraceHandler().sendStacktrace(e,"query:" + query);
            event.getChannel().sendMessage("An error occured. Please try again later.").queue();
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
    public String help() {
        return "Lookup a quest in the WoW database. Supports partial queries.\n\n" +
                "__Parameters__\n" +
                "**Quest** (Required): A quest name\n\n" +
                "**Example**: ``!lookupquest boar``";
    }

    @Override
    public String shortDescription() {
        return "Lookup a Quest in WoW database. Supports partial queries.";
    }
}
