package com.greatmancode.legendarybot;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.core.entities.Guild;
import okhttp3.*;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class SettingsConverter {
    private static final String MONGO_COLLECTION_NAME = "guild";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();
    private static final MediaType TEXT = MediaType.parse("text/plain");
    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());
    public SettingsConverter(LegendaryBot bot, Guild guild) {
        log.info("Converting settings for guild " + guild.getName() + ":" + guild.getId());
        //We convert the original settings
        MongoCollection<Document> collection = bot.getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        JSONObject guildJSON = new JSONObject();
        guildJSON.put("settings", new JSONObject());
        collection.find(eq("guild_id",guild.getId())).forEach((Block<Document>) document -> {
            if (document.get("settings") != null) {
                log.info("Converting the settings of the guild.");
                JSONObject wowlinkRanks = new JSONObject();
                //If it's wowlink, we create a json of the values instead.
                ((Document)document.get("settings")).forEach((k, v) -> {
                    if (k.contains("wowlink_rank_")) {
                        wowlinkRanks.put(k.split("_")[2], v);
                    } else if (k.equalsIgnoreCase("wowlink_rankset") || k.equalsIgnoreCase("wowlink_scheduler") || k.equalsIgnoreCase("WOW_GUILD_NAME") || k.equalsIgnoreCase("WOW_REALM_NAME")) {
                        //We force disable the wow rank autoupdate system because of the massive overhaul. We also ignore bad values

                    } else {
                        guildJSON.getJSONObject("settings").put(k,v);
                    }

                });
                guildJSON.getJSONObject("settings").put("wowranks", wowlinkRanks);
            }
            Document streamers = ((Document)document.get("streamers"));
            if (streamers != null) {
                log.info("Converting the streamers of the guild.");
                JSONObject streamersJSON = new JSONObject();
                JSONArray twitchStreamers = new JSONArray();
                JSONArray mixerStreamers = new JSONArray();
                streamers.forEach((k,v) -> {
                    if (k.equalsIgnoreCase("twitch")) {
                        ((ArrayList<String>) v).forEach(twitchStreamers::put);
                    } else if (k.equalsIgnoreCase("mixer")) {
                        ((ArrayList<String>) v).forEach(mixerStreamers::put);
                    }
                });
                streamersJSON.put("TWITCH", twitchStreamers);
                streamersJSON.put("MIXER", mixerStreamers);
                guildJSON.getJSONObject("settings").put("streamers", streamersJSON);
            }
        });

        //We convert the customCommands

        collection.find(eq("guild_id",guild.getId())).forEach((Block<Document>) document -> {
            if (document.containsKey("customCommands")) {
                log.info("Converting the customCommands of the guild.");
                JSONObject customCommands = new JSONObject();
                ((Document)document.get("customCommands")).forEach((k, v) -> {
                    JSONObject customCommand = new JSONObject();
                    customCommand.put("value", v);
                    customCommand.put("type", "text");
                    customCommands.put(k, customCommand);
                });
                guildJSON.getJSONObject("settings").put("customCommands", customCommands);
            }
        });



        //We save it to the backend
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(bot.getBotSettings().getProperty("api.host"))
                .addPathSegments("api/guild/"+guild.getId()+"/settingRaw")
                .build();
        Request request = new Request.Builder().url(url).addHeader("x-api-key", bot.getBotSettings().getProperty("api.key")).post(RequestBody.create(TEXT, guildJSON.toString())).build();
        try {
            System.out.println(client.newCall(request).execute());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(5000);
                client.newCall(request).execute();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        //TODO add some kind of character converter for characters with people linked to them.
    }
}
