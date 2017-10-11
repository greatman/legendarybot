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
import com.greatmancode.legendarybot.api.server.WoWGuild;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.OkHttpClient;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.Collections;

public class AddGuildCommand extends AdminCommand {

    private OkHttpClient client;

    private LegendaryBot bot;

    public AddGuildCommand(LegendaryBot bot) {
        this.bot = bot;
        client = new OkHttpClient.Builder()
                .addInterceptor(new BattleNetAPIInterceptor(bot))
                .build();
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String region = args[0];
        String[] allArgs = new String[args.length - 1];
        System.arraycopy(args,1,allArgs,0,args.length - 1);
        //We merge everything together
        StringBuilder builder = new StringBuilder();
        for (String s : allArgs) {
            builder.append(s).append(" ");
        }
        String result = builder.toString();
        String[] serverGuild = result.split("\"");
        String server = serverGuild[1].trim();
        String guild = serverGuild[3].trim();
        boolean isDefault = false;
        if (serverGuild.length == 5) {
            isDefault = Boolean.parseBoolean(serverGuild[4].trim());
        }

        try {
            HttpEntity entity = new NStringEntity("{ \"query\": { \"match\" : { \"name\" : \""+server+"\" } } }", ContentType.APPLICATION_JSON);
            Response response = bot.getElasticSearch().performRequest("POST", "/wow/realm_"+region+"/_search", Collections.emptyMap(), entity);
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

            //We got the server, let's save everything
            WoWGuild wowGuild = new WoWGuild(region,serverSlug,guild,isDefault);
            bot.getWowGuildManager(event.getGuild()).addServerGuild(wowGuild);
            event.getChannel().sendMessage("Guild added to the server.").queue();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(), "region:"+region, "server:" + server, "guild:" + guild, "default:" + isDefault);
        } catch (ParseException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(), "region:"+region, "server:" + server, "guild:" + guild, "default:" + isDefault);
            event.getChannel().sendMessage("No server found!").queue();
            return;
        } catch (org.json.simple.parser.ParseException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(), "region:"+region, "server:" + server, "guild:" + guild, "default:" + isDefault);
        }
    }

    @Override
    public int minArgs() {
        return 3;
    }

    @Override
    public int maxArgs() {
        return 99;
    }

    @Override
    public String help() {
        return "addserverguild [Region] \"[Server]\" \"[Guild]\" <Default(true/false)> - Add a World of Warcraft Guild to the bot. First guild added is by default the \"default\" one.";
    }
}
