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
package com.greatmancode.legendarybot.api.utils;

import com.greatmancode.legendarybot.api.LegendaryBot;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class BattleNet {

    public static Hero getiLvl(String serverName, String character) {
        String urlString = "https://us.api.battle.net/wow/character/"+serverName+"/"+character+"?fields=items&locale=en_US&apikey="+LegendaryBot.getBattlenetKey();
        String result = Utils.doRequest(urlString);
        if (result == null) {
            return null;
        }
        try {
            JSONObject object = (JSONObject) Utils.jsonParser.parse(result);
            if (!object.containsKey("name") || !object.containsKey("class") || !object.containsKey("lebel")) {
                return null;
            }
            Hero hero = new Hero((String)object.get("name"), HeroClass.values()[((Long) object.get("class")).intValue()], (Long)object.get("level"));
            hero.setIlvl((Long)((JSONObject)object.get("items")).get("averageItemLevel"));
            hero.setEquipilvl((Long)((JSONObject)object.get("items")).get("averageItemLevelEquipped"));
            return hero;
        } catch (ParseException e) {
            e.printStackTrace();
            LegendaryBot.getRaygunClient().Send(e);
        }
        return null;
    }

    public static boolean guildExist(String serverName, String guildName) {
        String urlString = "https://us.api.battle.net/wow/guild/"+serverName+"/"+guildName+"?locale=en_US&apikey=" + LegendaryBot.getBattlenetKey();
        String result = Utils.doRequest(urlString);
        return result != null;
    }

    public static Map<String, String> getServerStatus(String serverName) {
        Map<String,String> map = new HashMap<>();
        String url = "https://us.api.battle.net/wow/realm/status?locale=en_US&realms="+serverName+"&apikey="+LegendaryBot.getBattlenetKey();
        String result = Utils.doRequest(url);
        try {
            JSONObject object = (JSONObject) Utils.jsonParser.parse(result);
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
            LegendaryBot.getRaygunClient().Send(e);
        }
        return map;

    }
}
