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

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import net.dv8tion.jda.core.EmbedBuilder;
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

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * !server command - Return the current status of the WoW server.
 */
public class ServerCommand extends LegendaryBotPlugin implements PublicCommand {

    /**
     * The HttpClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();

    public ServerCommand(PluginWrapper wrapper) {
        super(wrapper);

    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("server", this, "World of Warcraft");
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
            //Todo support correctly slugs
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
            EmbedBuilder eb = new EmbedBuilder();
            eb.setThumbnail("https://us.battle.net/forums/static/images/game-logos/game-logo-wow.png");
            if (map.size() == 5) {
                if (map.get("status").equalsIgnoreCase("Offline")) {
                    eb.setColor(Color.RED);
                } else {
                    eb.setColor(Color.GREEN);
                }
                eb.setTitle(map.get("name") + " - " + map.get("region").toUpperCase());
                eb.addField("Status", map.get("status"), true);
                eb.addField("Population", map.get("population"), true);
                eb.addField("Currently a Queue?", map.get("queue"), true);
            } else {
                eb.addField("Error","An error occured. Try again later.", false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
        } catch (IOException e) {
            if (args.length > 0) {
                getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + args[0]);
            } else {
                getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            }
            e.printStackTrace();
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
        return "Retrieve a Realm status.\n\n" +
                "__Parameters__\n" +
                "**Realm Name** (Optional/Required) : The Realm name that you want the status. If this parameter is omitted, the command will take this Discord's server configured realm.";
    }

    @Override
    public String shortDescription() {
        return "Retrieve a Realm status.";
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
                map.put("name", realm.get("name").toString());
                map.put("region", region);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName);
        }
        return map;

    }
}
