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

package com.greatmancode.legendarybot.commands.affix;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Plugin that provides the current and next's week affix.
 */
public class AffixPlugin extends LegendaryBotPlugin {


    public AffixPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("affix", new AffixCommand(this), "World of Warcraft");
        getBot().getCommandHandler().addCommand("nextaffix", new NextAffixCommand(getBot()), "World of Warcraft");
        log.info("Command !affix & !nextaffix loaded");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("affix");
        getBot().getCommandHandler().removeCommand("nextaffix");
        log.info("Command !affix & !nextaffix unloaded");
    }

    public Map<Long,String> getWeekAffixes(String region) throws IOException, ParseException {
        Map<Long, String> affixes = new HashMap<>();
        OkHttpClient clientBattleNet = new OkHttpClient.Builder()
                .addInterceptor(new BattleNetAPIInterceptor(getBot()))
                .build();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(region + ".api.battle.net")
                .addPathSegments("/data/wow/mythic-challenge-mode/")
                .addQueryParameter("namespace", "dynamic-"+region)
                .build();
        Request request = new Request.Builder().url(url).build();
        String result = clientBattleNet.newCall(request).execute().body().string();
        JSONParser parser = new JSONParser();
        JSONObject mythicPlusDocument = (JSONObject) parser.parse(result);
        if (mythicPlusDocument.containsKey("current_keystone_affixes")) {
            JSONArray array = (JSONArray) mythicPlusDocument.get("current_keystone_affixes");
            for (Object keystoneAffixObject : array) {
                JSONObject keystoneAffixJson = (JSONObject) keystoneAffixObject;
                JSONObject keystoneAffixNameObject = (JSONObject) keystoneAffixJson.get("keystone_affix");
                affixes.put((long)keystoneAffixJson.get("starting_level"), (String) keystoneAffixNameObject.get("name"));
            }
        }
        return affixes;
    }
}
