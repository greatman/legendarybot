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
package com.greatmancode.legendarybot.commands.server;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerCommand extends LegendaryBotPlugin implements PublicCommand {

    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();

    public ServerCommand(PluginWrapper wrapper) {
        super(wrapper);

    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("server", this);
        log.info("Command !server loaded.");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("server");
        log.info("Command !server disabled.");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = null;
        try {
            Map<String,String> map;
            if (args.length == 1) {
                map = getServerStatus(getBot().getGuildSettings(event.getGuild()).getRegionName(), args[0]);
            } else {
                serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
                if (serverName == null) {
                    event.getChannel().sendMessage("No server set. You are required to type a server.").queue();
                    return;
                }
                map = getServerStatus(getBot().getGuildSettings(event.getGuild()).getRegionName(), serverName);
            }
            MessageBuilder builder = new MessageBuilder();
            if (map.size() == 4) {
                builder.append("Server: ");
                builder.append(map.get("name"));
                builder.append(" | Status: ");
                builder.append(map.get("status"));
                builder.append(" | Population: ");
                builder.append(map.get("population"));
                builder.append(" | Currently a queue? : ");
                builder.append(map.get("queue"));
            } else {
                builder.append("An error occured. Try again later.");
            }
            event.getChannel().sendMessage(builder.build()).queue();
        } catch (IOException e) {
            if (args.length > 0) {
                getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + args[0]);
            } else {
                getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            }

            event.getChannel().sendMessage("An error occured. Try again later!").queue();
        }
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "server <Server Name> - Retrieve a server status.";
    }

    /**
     * Retrieve the server status of a World of Warcraft realm
     * The Map returned will have the following values:
     * name -> Realm Name
     * status -> Online/Offline
     * queue -> Yes/No
     * population -> The population of the Realm (Low/Medium/High/Full)
     * @param region The Region the server is hosted in.
     * @param serverName The server name
     * @return A {@link Map} containing the values above if it is found. Else an empty map.
     *
     */
    public Map<String, String> getServerStatus(String region, String serverName) throws IOException {
        Map<String,String> map = new HashMap<>();
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(region + ".api.battle.net")
                .addPathSegments("/wow/realm/status")
                .addQueryParameter("realms", serverName)
                .build();
        Request request = new Request.Builder().url(url).build();
        String result = client.newCall(request).execute().body().string();
        try {
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(result);
            JSONArray realms = (JSONArray) object.get("realms");
            for (Object realmObject : realms) {
                JSONObject realm = (JSONObject) realmObject;
                map.put("population", (String) realm.get("population"));
                map.put("queue", (Boolean)realm.get("queue") ? "Yes" : "No");
                map.put("status", (Boolean)realm.get("status") ? "Online" : "Offline");
                map.put("name", serverName);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName);
        }
        return map;

    }
}
