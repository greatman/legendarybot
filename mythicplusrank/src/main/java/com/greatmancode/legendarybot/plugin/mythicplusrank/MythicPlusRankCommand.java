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

package com.greatmancode.legendarybot.plugin.mythicplusrank;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.server.WoWGuild;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WowCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.Collections;

/**
 * !mplusrank - Get the Mythic Plus ranking of a player.
 */
public class MythicPlusRankCommand extends LegendaryBotPlugin implements WowCommand, PublicCommand{

    /**
     * The HTTPClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient();

    public MythicPlusRankCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

//TODO support multiple regions/servers
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        WoWGuild guild = getBot().getWowGuildManager(event.getGuild()).getDefaultGuild();
        if (guild == null) {
            event.getChannel().sendMessage("You need to configure a guild to be able to use this command.").queue();
            return;
        }
        String serverName = guild.getServer();

        try {
            if (args.length == 2) {
                HttpEntity entity = new NStringEntity("{ \"query\": { \"match\" : { \"name\" : \""+args[1]+"\" } } }", ContentType.APPLICATION_JSON);
                Response response = getBot().getElasticSearch().performRequest("POST", "/wow/realm_"+guild.getRegion().toLowerCase()+"/_search", Collections.emptyMap(), entity);
                String jsonResponse = null;
                jsonResponse = EntityUtils.toString(response.getEntity());
                JSONParser jsonParser = new JSONParser();
                JSONObject obj = (JSONObject) jsonParser.parse(jsonResponse);
                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                if (hit.size() == 0) {
                    event.getChannel().sendMessage("No server found with the name of "+args[1]+"!").queue();
                    return;
                }
                JSONObject firstItem = (JSONObject) hit.get(0);
                JSONObject source = (JSONObject)  firstItem.get("_source");
                serverName = (String) source.get("slug");
            }
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host("raider.io")
                    .addPathSegments("api/characters/"+guild.getRegion().toLowerCase()+"/"+ serverName +"/"+args[0]+"")
                    .addQueryParameter("season", "season-7.3.0")
                    .build();
            Request request = new Request.Builder().url(url).build();
            String result = client.newCall(request).execute().body().string();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result);
            if (jsonObject.containsKey("statusCode")) {
                event.getChannel().sendMessage("Character not found!").queue();
                return;
            }
            JSONObject characterDetails = (JSONObject)jsonObject.get("characterDetails");
            JSONObject character = (JSONObject) characterDetails.get("character");
            JSONObject mplusScores = (JSONObject) characterDetails.get("mythicPlusScores");
            String currentSeasonName = (String) characterDetails.get("seasonName");
            int currentSeasonScore = (int)Double.parseDouble(String.valueOf(((JSONObject)mplusScores.get("all")).get("score")));
            int previousSeasonScore = (int)Double.parseDouble(String.valueOf(characterDetails.get("previousSeasonScore")));
            String previousSeasonName = (String) characterDetails.get("previousSeasonName");
            MessageBuilder builder = new MessageBuilder();
            builder.append("**" + character.get("name") + "**");
            builder.append(" Mythic+ Score: ");
            builder.append("Global: **"+currentSeasonName+"**: **"+currentSeasonScore+"** | **"+previousSeasonName+"**: **" + previousSeasonScore +"** ");
            event.getChannel().sendMessage(builder.build()).queue();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e,"guildId:" + event.getGuild().getId(), "serverName:" + serverName, "character:" + args[0]);
        }
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 2;
    }

    @Override
    public String help() {
        return "mplusrank [Player Name] <Realm> - Retrieve the Mythic+ rank of a player (Based on Raider.io).";
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("mplusrank", this);
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("mplusrank");
    }
}
