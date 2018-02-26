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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;

public class WoWUtils {


    /**
     * Retrieve the informatoin of a realm
     * @param bot The bot instance.
     * @param region The region the realm is in
     * @param realm The realm name
     * @return A Json string containing information about the realm. Returns null if no realm is found.
     */
    public static String getRealmInformation(LegendaryBot bot, String region, String realm) {
        JSONObject search = new JSONObject();
        JSONObject query = new JSONObject();
        JSONArray fields = new JSONArray();
        fields.add("name");
        fields.add("slug");
        JSONObject multi_match = new JSONObject();
        multi_match.put("query", realm);
        multi_match.put("fields", fields);
        query.put("multi_match", multi_match);
        search.put("query", query);
        HttpEntity entity = new NStringEntity(search.toJSONString(), ContentType.APPLICATION_JSON);
        try {
            Response response = bot.getElasticSearch().performRequest("POST", "/wow/realm_"+region.toLowerCase()+"/_search", Collections.emptyMap(), entity);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JSONParser jsonParser = new JSONParser();
            JSONObject obj = (JSONObject) jsonParser.parse(jsonResponse);
            JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
            if (hit.size() == 0) {
                return null;
            }
            JSONObject firstItem = (JSONObject) hit.get(0);
            JSONObject source = (JSONObject)  firstItem.get("_source");
            return source.toJSONString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieve the {@link Color} of a World of Warcraft class.
     * @param className The class name to get the color from
     * @return A instance of {@link Color} representing the class color. Returns white if not found
     */
    public static Color getClassColor(String className) {
        Color color = null;
        String classNameLower = className.toLowerCase();
        switch (classNameLower) {
            case "death knight":
                color = new Color(196,30,59);
                break;
            case "demon hunter":
                color = new Color(163,48,201);
                break;
            case "druid":
                color = new Color(255,125,10);
                break;
            case "hunter":
                color = new Color(171,212,115);
                break;
            case "mage":
                color = new Color(105,204,240);
                break;
            case "monk":
                color = new Color(0,255,150);
                break;
            case "paladin":
                color = new Color(245,140,186);
                break;
            case "priest":
                color = new Color(255,255,255);
                break;
            case "rogue":
                color = new Color(255,245,105);
                break;
            case "shaman":
                color = new Color(0,112,222);
                break;
            case "warlock":
                color = new Color(148,130,201);
                break;
            case "warrior":
                color = new Color(199,156,110);
                break;
            default:
                color = Color.WHITE;
        }
        return color;
    }

    public static String getClassIcon(String className) {
        String url;
        String classNameLower = className.toLowerCase();
        switch (classNameLower) {
            case "death knight":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/e/e5/Ui-charactercreate-classes_deathknight.png";
                break;
            case "demon hunter":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/c/c9/Ui-charactercreate-classes_demonhunter.png";
                break;
            case "druid":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/6/6f/Ui-charactercreate-classes_druid.png";
                break;
            case "hunter":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/4/4e/Ui-charactercreate-classes_hunter.png";
                break;
            case "mage":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/5/56/Ui-charactercreate-classes_mage.png";
                break;
            case "monk":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/2/24/Ui-charactercreate-classes_monk.png";
                break;
            case "paladin":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/8/80/Ui-charactercreate-classes_paladin.png";
                break;
            case "priest":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/0/0f/Ui-charactercreate-classes_priest.png";
                break;
            case "rogue":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/b/b1/Ui-charactercreate-classes_rogue.png";
                break;
            case "shaman":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/3/3e/Ui-charactercreate-classes_shaman.png";
                break;
            case "warlock":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/c/cf/Ui-charactercreate-classes_warlock.png";
                break;
            case "warrior":
                url = "https://d1u5p3l4wpay3k.cloudfront.net/wowpedia/3/37/Ui-charactercreate-classes_warrior.png";
                break;
            default:
                url = null;
        }
        return url;
    }
}
