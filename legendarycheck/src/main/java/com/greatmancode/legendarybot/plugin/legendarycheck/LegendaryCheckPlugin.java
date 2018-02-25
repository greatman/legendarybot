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
package com.greatmancode.legendarybot.plugin.legendarycheck;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.core.entities.Guild;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.bson.Document;
import org.elasticsearch.client.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * The Legendary Check plugin
 */
public class LegendaryCheckPlugin extends LegendaryBotPlugin{

    /**
     * The setting name where we save the legendary check channel name.
     */
    public static final String SETTING_NAME = "legendary_check";

    /**
     * The Map of running legendary check.
     */
    private Map<String, LegendaryCheck> legendaryCheckMap = new HashMap<>();

    public final static String MONGO_WOW_CHARACTERS_COLLECTION = "wowCharacters";

    public LegendaryCheckPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Starting OldLegendaryCheck plugin.");
        final int[] i = {0};
        getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(guild -> {
            if (startLegendaryCheck(guild, i[0])) {
                i[0]++;
            }

        }));
        getBot().getCommandHandler().addCommand("enablelc", new EnableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        getBot().getCommandHandler().addCommand("disablelc", new DisableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        getBot().getCommandHandler().addCommand("mutelc", new MuteLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        log.info("Command !enablelc, !disablelc and !mutelc added!");
        log.info("Plugin OldLegendaryCheck started!");
    }

    @Override
    public void stop() {
        legendaryCheckMap.forEach((k,v) -> v.shutdown());
        legendaryCheckMap.clear();
        getBot().getCommandHandler().removeCommand("enablelc");
        getBot().getCommandHandler().removeCommand("disablelc");
        getBot().getCommandHandler().removeCommand("mutelc");
        log.info("Plugin OldLegendaryCheck unloaded! Command !enablelc, !disablelc  and !mutelc removed");
    }


    /**
     * Start the legendary check for a guild
     * @param guild The guild to start the Legendary check.
     */
    public void startLegendaryCheck(Guild guild) {
        startLegendaryCheck(guild, 0);
    }

    /**
     * Start a legendary check for a guild.
     * @param guild The guild to start the LC check in.
     * @param initialDelay the initial delay before starting the check.
     */
    public boolean startLegendaryCheck(Guild guild, int initialDelay) {
        if (getBot().getGuildSettings(guild).getSetting(SETTING_NAME) != null) {
            if (legendaryCheckMap.containsKey(guild.getId())) {
                legendaryCheckMap.get(guild.getId()).shutdown();
                legendaryCheckMap.remove(guild.getId());
            }

            legendaryCheckMap.put(guild.getId(), new LegendaryCheck(this, guild, initialDelay));
            return true;
        }
        return false;
    }

    /**
     * Stops and deletes the config of a legendary check for a guild.
     * @param guild The guild to disable the legendary check.
     */
    public void destroyLegendaryCheck(Guild guild) {
        getBot().getGuildSettings(guild).unsetSetting(SETTING_NAME);
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

    /**
     * Stops the legendary check for a guild.
     * @param guild The guild to stop the legendary check.
     */
    public void stopLegendaryCheck(Guild guild) {
        if (legendaryCheckMap.containsKey(guild.getId())) {
            legendaryCheckMap.get(guild.getId()).shutdown();
            legendaryCheckMap.remove(guild.getId());
        }
    }

    /**
     * Retrieve the last modified date of a player in the database.
     * @param region The region of the player
     * @param serverName The server name of the player.
     * @param playerName The player name.
     * @return a long containing the last modified date in UNIX timestamp format. If not found, returns -1.
     */
    public long getPlayerInventoryDate(String region, String serverName, String playerName) {
        long time = -1;
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        Document document = collection.find(and(eq("region", region), eq("realm", serverName), eq("name", playerName))).first();
        if (document != null && document.containsKey("lastUpdate")) {
            time = document.getLong("lastUpdate");
        }
        return time;
    }

    /**
     * Set the last modified date of a player in the database.
     * @param region The region of the player
     * @param serverName The server name of the player.
     * @param playerName The player name
     * @param time The time of the last modified in UNIX timestamp format.
     */
    public void setPlayerInventoryDate(String region, String serverName, String playerName, long time) {
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        collection.updateOne(and(eq("region", region), eq("realm", serverName), eq("name", playerName)),set("lastUpdate", time), new UpdateOptions().upsert(true));
    }

    public long getPlayerNewsDate(String region, String serverName, String playerName) {
        long time = -1;
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        Document document = collection.find(and(eq("region", region), eq("realm", serverName), eq("name", playerName))).first();
        if (document != null && document.containsKey("newsDate")) {
            time = document.getLong("newsDate");
        }
        return time;
    }

    public void setPlayerNewsDate(String region, String serverName, String playerName, long time) {
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        collection.updateOne(and(eq("region", region), eq("realm", serverName), eq("name", playerName)),set("newsDate", time), new UpdateOptions().upsert(true));
    }
    /**
     * Retrieve the count of enabled LC check.
     * @return The amount of enabled LC checks.
     */
    public int getLegendaryCheckEnabledCount() {
        return legendaryCheckMap.size();
    }

    /**
     * Check if an item is a legendary. Checks in the ES cache if we have the item. If not, we retrieve the information from Battle.Net API and cache it.
     * @param regionName The region to check the item in.
     * @param itemID The Item ID to check.
     * @return True if the item is a legendary. Else false.
     */
    public boolean isItemLegendary(String regionName, long itemID) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BattleNetAPIInterceptor(getBot()))
                .build();
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("q", "id:" + itemID);
            Response response = getBot().getElasticSearch().performRequest("GET", "/wow/item/_search", paramMap);

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject obj = (JSONObject) jsonParser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject hits = (JSONObject) obj.get("hits");
                if ((long)hits.get("total") == 0) {
                    HttpUrl url = new HttpUrl.Builder().scheme("https")
                            .host(regionName + ".api.battle.net")
                            .addPathSegments("/wow/item/" + itemID)
                            .build();
                    Request webRequest = new Request.Builder().url(url).build();
                    okhttp3.Response responseBattleNet = client.newCall(webRequest).execute();
                    String itemRequest = responseBattleNet.body().string();
                    responseBattleNet.close();
                    if (itemRequest == null) {
                        return false;
                    }
                    JSONObject itemObject;
                    try {
                        itemObject = (JSONObject) new JSONParser().parse(itemRequest);
                    } catch (ParseException e) {
                        getBot().getStacktraceHandler().sendStacktrace(e, "itemID:" + itemID, "regionName:" + regionName, "itemRequest:" + itemRequest);
                        return false;
                    }
                    if (itemObject.containsKey("reason")) {
                        return false;
                    }

                    HttpEntity entity = new NStringEntity(itemObject.toJSONString(), ContentType.APPLICATION_JSON);
                    Response indexResponse = getBot().getElasticSearch().performRequest("POST", "/wow/item/", Collections.emptyMap(), entity);
                    long quality = (Long) itemObject.get("quality");
                    return quality == 5;
                }

                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                JSONObject firstItem = (JSONObject) hit.get(0);
                JSONObject source = (JSONObject) firstItem.get("_source");
                return (long) source.get("quality") == 5;
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
            return false;
        }
    }

    /**
     * Retrieve the item name. Checks in the ES cache if we have the item. If not, we retrieve the information from Battle.Net API and cache it.
     * @param regionName The region to check in.
     * @param itemID The item ID.
     * @return The name of the item. Else null if not found.
     */
    public String getItemName(String regionName, long itemID) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BattleNetAPIInterceptor(getBot()))
                .build();
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("q", "id:" + itemID);
            Response response = getBot().getElasticSearch().performRequest("GET", "/wow/item/_search", paramMap);

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject obj = (JSONObject) jsonParser.parse(EntityUtils.toString(response.getEntity()));
                JSONObject hits = (JSONObject) obj.get("hits");
                if ((long)hits.get("total") == 0) {
                    HttpUrl url = new HttpUrl.Builder().scheme("https")
                            .host(regionName + ".api.battle.net")
                            .addPathSegments("/wow/item/" + itemID)
                            .build();
                    Request webRequest = new Request.Builder().url(url).build();
                    okhttp3.Response responseBattleNet = client.newCall(webRequest).execute();
                    String itemRequest = responseBattleNet.body().string();
                    responseBattleNet.close();
                    if (itemRequest == null) {
                        return null;
                    }
                    JSONObject itemObject;
                    try {
                        itemObject = (JSONObject) new JSONParser().parse(itemRequest);
                    } catch (ParseException e) {
                        getBot().getStacktraceHandler().sendStacktrace(e, "itemID:" + itemID, "regionName:" + regionName, "itemRequest:" + itemRequest);
                        return null;
                    }
                    return (String) itemObject.get("name");
                }
                JSONArray hit = (JSONArray) ((JSONObject)obj.get("hits")).get("hits");
                JSONObject firstItem = (JSONObject) hit.get(0);
                JSONObject source = (JSONObject) firstItem.get("_source");
                return (String) source.get("name");
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
            return null;
        }
    }
}
