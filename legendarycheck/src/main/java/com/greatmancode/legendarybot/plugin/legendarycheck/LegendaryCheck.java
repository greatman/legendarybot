package com.greatmancode.legendarybot.plugin.legendarycheck;

import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

import static com.mongodb.client.model.Filters.*;

public class LegendaryCheck {

    /**
     * The scheduler for the checks.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * ItemIDs to ignore, those are not Legendaries that we want to announce.
     */
    private final long[] itemIDIgnore = {147451,151462, 152626, 154880};

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    public LegendaryCheck(LegendaryCheckPlugin plugin, Guild guild, int initialDelay) {
        log.info("Preparing Legendary check for server " + guild.getName() + ":" + guild.getId() + ". Doing pre-flight checks.");
        String serverName = plugin.getBot().getGuildSettings(guild).getWowServerName();
        String regionName = plugin.getBot().getGuildSettings(guild).getRegionName();
        String guildName = plugin.getBot().getGuildSettings(guild).getGuildName();
        String channelName = plugin.getBot().getGuildSettings(guild).getSetting(LegendaryCheckPlugin.SETTING_NAME);
        if (regionName == null || serverName == null || guildName == null || channelName == null) {
            log.warn("Failed Pre-flight config for " + guild.getName() + ":" + guild.getId());
            plugin.destroyLegendaryCheck(guild);
            return;
        }
        if (guild.getTextChannelsByName(channelName, false).size() == 0) {
            log.warn("Failed pre-flight check for guild " + guild.getName() + ":" + guild.getId() + ". Channel not found.");
            plugin.destroyLegendaryCheck(guild);
            return;
        }
        log.info("Legendary check pre-flight for " + guild.getName() + ":" + guild.getId() + " done.");
        final Runnable checkLegendary = () -> {
            try {
                log.info("Starting Legendary check for server " + guild.getName() + ":" + guild.getId() + ".");
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new BattleNetAPIInterceptor(plugin.getBot()))
                        .build();

                HttpUrl url = new HttpUrl.Builder()
                        .scheme("https")
                        .host(regionName.toLowerCase() + ".api.battle.net")
                        .addPathSegments("/wow/guild/" + serverName + "/" + guildName)
                        .addQueryParameter("fields", "members,news")
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                Map<String, String> characterRealmMap = new HashMap<>();
                try {
                    Response guildResponse = client.newCall(request).execute();
                    String result = guildResponse.body().string();
                    guildResponse.close();
                    JSONParser parser = new JSONParser();
                    JSONObject guildJSON = (JSONObject) parser.parse(result);
                    if (guildJSON.containsKey("status")) {
                        plugin.stopLegendaryCheck(guild);
                        log.info("Failed status for guild " + guildName + ":" + guild.getId());
                        return;
                    }

                    //We load the guild character list
                    JSONArray members = (JSONArray) guildJSON.get("members");
                    for (Object memberRaw : members) {
                        JSONObject member = (JSONObject) memberRaw;
                        JSONObject character = (JSONObject) member.get("character");
                        long level = (Long) character.get("level");
                        if (level != 110) {
                            continue;
                        }
                        String realmInfo = WoWUtils.getRealmInformation(plugin.getBot(),regionName,(String)character.get("realm"));
                        if (realmInfo != null) {
                            JSONObject realmInfoObject = (JSONObject) parser.parse(realmInfo);
                            characterRealmMap.put((String)character.get("name"), (String) realmInfoObject.get("slug"));
                        }

                    }
                    
                    //We do the check to see if a character is active so we query it's last actions.
                    JSONArray news = (JSONArray) guildJSON.get("news");
                    List<String> doneCharacter = new ArrayList<>();
                    for (Object newsEntryRaw : news) {
                        JSONObject newsEntry = (JSONObject) newsEntryRaw;
                        String character = (String) newsEntry.get("character");
                        long newsTimestamp = (long) newsEntry.get("timestamp");

                        if (doneCharacter.contains(character)) {
                            continue;
                        }

                        long currentNewsTimestamp = plugin.getPlayerNewsDate(regionName,serverName,character);
                        if (newsTimestamp > currentNewsTimestamp) {
                            plugin.setPlayerNewsDate(regionName,serverName,character,newsTimestamp);
                        }
                        doneCharacter.add(character);
                    }


                    MongoCollection<Document> collection = plugin.getBot().getMongoDatabase().getCollection(LegendaryCheckPlugin.MONGO_WOW_CHARACTERS_COLLECTION);
                    LocalDateTime date = LocalDateTime.now().minusDays(7);
                    long timeMinus7Days = date.toInstant(ZoneOffset.UTC).toEpochMilli();
                    characterRealmMap.forEach((character,realm) -> {
                        if (guild.getId().equals("1518684006000")) {
                            log.info("Checking for user " + character + ": "+realm);

                        }
                        realm = realm.toLowerCase();
                        Document document = collection.find(and(eq("region", regionName),eq("realm",realm), eq("name", character),gt("newsDate", timeMinus7Days))).first();
                        if (document != null) {
                            //The character is active, let's do the legendary check.
                            HttpUrl characterUrl = new HttpUrl.Builder()
                                    .scheme("https")
                                    .host(regionName.toLowerCase() + ".api.battle.net")
                                    .addPathSegments("/wow/character/" + realm + "/" + character)
                                    .addQueryParameter("fields", "feed")
                                    .build();
                            Request characterRequest = new Request.Builder().url(characterUrl).build();
                            boolean notOk = true;
                            Response characterResponse = null;
                            while (notOk) {
                                try {
                                    characterResponse = client.newCall(characterRequest).execute();
                                    notOk = false;
                                } catch (SocketTimeoutException e) {
                                    log.warn("Timeout when trying to call" + url.toString() + " for guild " + guild.getName() + " : " + guild.getId());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    notOk = false;
                                }
                            }
                            if (characterResponse != null) {
                                try {
                                    String characterJSONRaw = characterResponse.body().string();
                                    characterResponse.close();
                                    JSONObject characterJSON = (JSONObject) parser.parse(characterJSONRaw);
                                    if (!characterJSON.containsKey("status")) {
                                        long memberLastModified = (Long) characterJSON.get("lastModified");
                                        long dbLastModified = plugin.getPlayerInventoryDate(regionName, realm, character);
                                        if (memberLastModified > dbLastModified) {
                                            plugin.setPlayerInventoryDate(regionName, realm, character, memberLastModified);
                                            //We check the items
                                            JSONArray feedArray = (JSONArray) characterJSON.get("feed");
                                            for (Object feedObject : feedArray) {
                                                JSONObject feed = (JSONObject) feedObject;
                                                if (feed.get("type").equals("LOOT")) {
                                                    long itemID = (Long) feed.get("itemId");
                                                    long timestamp = (Long) feed.get("timestamp");
                                                    if (timestamp <= dbLastModified) {
                                                        continue;
                                                    }
                                                    if (LongStream.of(itemIDIgnore).anyMatch(x -> x == itemID)) {
                                                        continue;
                                                    }
                                                    if (plugin.isItemLegendary(regionName, itemID)) {
                                                        log.info(character + " just looted a legendary");
                                                        //We got a legendary!
                                                        List<TextChannel> channelList = guild.getTextChannelsByName(channelName, true);
                                                        if (channelList.isEmpty()) {
                                                            plugin.destroyLegendaryCheck(guild);
                                                            log.warn("Guild " + guild + "("+guild.getId()+") have a invalid channel name " + channelName + ". Removing legendary check.");
                                                        } else {
                                                            channelList.get(0).sendMessage(plugin.getBot().getTranslateManager().translate(guild,"legendarycheck.looted",character,plugin.getItemName(regionName, itemID)) + " http://www.wowhead.com/item=" + itemID).queue();
                                                        }

                                                    }
                                                }
                                            }
                                        }

                                    } else {
                                        log.warn("Guild " + guild.getName() + "("+guild.getId()+") Member " + character + " with realm " + realm + " not found for WoW guild " + guildName + "-" + regionName);
                                    }

                                } catch (IOException | ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                    log.info("Went through Legendary check for server " + guild.getName() + ":" + guild.getId() + ".");
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        };
        log.info("Scheduling LC for guild " + guild.getName() + ":" + guild.getId() + " with a " + initialDelay + " seconds delay.");
        scheduler.scheduleAtFixedRate(checkLegendary, initialDelay,1200, TimeUnit.SECONDS);
    }

    /**
     * Stop the legendary checker. Stops the scheduler.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

}
