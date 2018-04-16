package com.greatmancode.legendarybot;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.core.entities.Guild;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class SettingsConverter {
    private static final String MONGO_COLLECTION_NAME = "guild";
    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());
    public SettingsConverter(LegendaryBot bot, Guild guild) {
        log.info("Converting settings for guild " + guild.getName() + ":" + guild.getId());
        //We convert the original settings
        MongoCollection<Document> collection = bot.getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        collection.find(eq("guild_id",guild.getId())).forEach((Block<Document>) document -> {
            if (document.get("settings") != null) {
                log.info("Converting the settings of the guild.");
                ((Document)document.get("settings")).forEach((k, v) -> bot.getGuildSettings(guild).setSetting(k, (String) v));
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
                bot.getGuildSettings(guild).setSetting("customCommands", customCommands.toString());
            }
        });


        //We convert the streamers
        Document document = collection.find(and(eq("guild_id", guild.getId()))).first();
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
            bot.getGuildSettings(guild).setSetting("streamers", streamersJSON.toString());
        }

        //TODO add some kind of character converter for characters with people linked to them.
    }
}
