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
import com.greatmancode.legendarybot.api.utils.HeroClass;
import com.greatmancode.legendarybot.api.utils.WoWSlotType;
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import com.greatmancode.legendarybot.api.utils.WowArmorType;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.bson.Document;
import org.json.JSONObject;
import org.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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
    private Properties props;

    public LegendaryCheckPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Starting OldLegendaryCheck plugin.");
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        final int[] i = {0};
        new Thread(() -> getBot().getJDA().forEach(jda -> jda.getGuilds().forEach(guild -> {
            if (startLegendaryCheck(guild, i[0])) {
                i[0]++;
            }
        }))).start();

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

    public String getItem(String region, long id) {
        String result = null;
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(props.getProperty("api.host"))
                .addPathSegments("api/item/"+region+"/"+id)
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                result = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Check if an item is a legendary. Checks in the ES cache if we have the item. If not, we retrieve the information from Battle.Net API and cache it.
     * @return True if the item is a legendary. Else false.
     */
    public boolean isItemLegendary(String json) {
        return (json != null) && new JSONObject(json).getInt("quality") == 5;
    }

    public MessageEmbed buildEmbed(String character, HeroClass heroClass, String json) {
        JSONObject jsonObject = new JSONObject(json);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(jsonObject.getString("name"),"http://www.wowhead.com/item=" + jsonObject.getInt("id"));
        System.out.println("https://wow.zamimg.com/images/wow/icons/large/"+jsonObject.getString("icon") + ".jpg");
        builder.setThumbnail("https://wow.zamimg.com/images/wow/icons/large/"+jsonObject.getString("icon") + ".jpg");
        builder.setColor(WoWUtils.getClassColor(heroClass.name()));
        builder.setAuthor(character + " just looted the following legendary!", "http://www.wowhead.com/item=" + jsonObject.getInt("id"), WoWUtils.getClassIcon(heroClass.name()));
        StringBuilder stringBuilder = new StringBuilder();
        String slot = WoWSlotType.values()[jsonObject.getInt("inventoryType")].name().toLowerCase();
        slot = slot.substring(0,1).toUpperCase() + slot.substring(1);
        stringBuilder.append("**");
        stringBuilder.append(slot);
        if (jsonObject.getInt("itemSubClass") != 0) {
            String armorType = WowArmorType.values()[jsonObject.getInt("itemSubClass")].name().toLowerCase();
            armorType = armorType.substring(0,1).toUpperCase() + armorType.substring(1);
            stringBuilder.append(" ");
            stringBuilder.append(armorType);
        }
        stringBuilder.append("**\n\n");

        if (jsonObject.has("itemSpells")) {
            if (jsonObject.getJSONObject("itemSpells").has("spell") && !jsonObject.getJSONObject("itemSpells").getJSONObject("spell").getString("description").equals("") ) {
                stringBuilder.append("**Equip:** ");
                stringBuilder.append(jsonObject.getJSONObject("itemSpells").getJSONObject("spell").getString("description"));
                stringBuilder.append("\n\n");
            }
        }

        //Hardcoded stuff because blizz does crap in his API
        switch (jsonObject.getInt("id")) {
            case 151801:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Finishing moves extend the duration of Tiger's Fury by 0.5 sec per combo point spent.");
                stringBuilder.append("\n\n");
                break;
            case 151823:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Ravager increases your movement speed by 10% and your damage done by 2% for 6 sec, increasing periodically and stacking up to 6 times\n" +
                        "\n" +
                        "Bladestorm increases your movement speed by 10% and your damage done by 2% for 6 sec, increasing periodically and stacking up to 6 times.");
                stringBuilder.append("\n\n");
                break;
            case 151787:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Prayer of Mending has a 15% chance to grant you Apotheosis for 8 sec.");
                stringBuilder.append("\n\n");
                break;
            case 151809:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Casting 30 Fireballs or Pyroblasts calls down a Meteor at your target.");
                stringBuilder.append("\n\n");
                break;
            case 151782:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Light of Dawn has a 10% chance to grant you Avenging Wrath for 8 sec.");
                stringBuilder.append("\n\n");
                break;
            case 151813:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Every 2 sec, gain 6% increased damage to your next Divine Storm, stacking up to 30 times.");
                stringBuilder.append("\n\n");
                break;
            case 137227:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Dire Beast reduces the remaining cooldown on Kill Command by 3 sec.");
                stringBuilder.append("\n\n");
                break;
            case 151644:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Gain one of the following talents based on your specialization:\n" +
                        "\n" +
                        "Holy: Divine Purpose\n" +
                        "Protection: Holy Shield\n" +
                        "Retribution: Divine Purpose");
                stringBuilder.append("\n\n");
                break;
            case 150936:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Gain the Vigor talent.");
                stringBuilder.append("\n\n");
                break;
            case 151819:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("You have a \n" +
                        "\n" +
                        "Enhancement\n" +
                        "0.12%\n" +
                        "\n" +
                        "Elemental\n" +
                        "0.10%\n" +
                        "\n" +
                        "chance per Maelstrom spent to gain Ascendance for 10 sec.");
                stringBuilder.append("\n\n");
                break;
            case 151812:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Eye of Tyr deals 300% increased damage and has 25% reduced cooldown.");
                stringBuilder.append("\n\n");
                break;
            case 151822:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Shield Block and Spell Reflection gain 1 additional charge.");
                stringBuilder.append("\n\n");
                break;
            case 151807:
                stringBuilder.append("**Equip:** ");
                stringBuilder.append("Gain 10% increased critical strike chance against enemies burning from your Explosive Trap.");
                stringBuilder.append("\n\n");
                break;
        }
        stringBuilder.append(jsonObject.getString("description"));
        builder.setDescription(stringBuilder);
        return builder.build();
    }
    public String getItemName(String json) {
        JSONObject object = new JSONObject(json);
        return object.getString("name");
    }
}
