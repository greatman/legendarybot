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
package com.greatmancode.legendarybot.plugins.wowlink;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.api.utils.HeroClass;
import com.greatmancode.legendarybot.plugins.wowlink.commands.*;
import com.greatmancode.legendarybot.plugins.wowlink.utils.OAuthBattleNetApi;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WoWCharacter;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.path;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

/**
 * The WowLink plugin main class.
 */
public class WoWLinkPlugin extends LegendaryBotPlugin {

    public static final String SETTING_RANKSET_ENABLED = "wowlink_rankset";
    public static final String SETTING_SCHEDULER = "wowlink_scheduler";
    public static final String SETTING_RANK_PREFIX = "wowlink_rank_";

    private final static String MONGO_WOW_CHARACTERS_COLLECTION = "wowCharacters";
    /**
     * The HttpClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();

    /**
     * The settings file
     */
    private Properties props;

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, SyncRankScheduler> scheduler = new HashMap<>();

    public WoWLinkPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }


    /**
     * Get the properties file.
     * @return The Properties file.
     */
    public Properties getProps() {
        return props;
    }

    @Override
    public void start() {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }

        path("/auth", () -> get("/battlenetcallback", (req, res) -> {
            String state = req.queryParams("state");
            String region = state.split(":")[0];
            OAuth20Service service = new ServiceBuilder(props.getProperty("battlenetoauth.key"))
                    .apiSecret(props.getProperty("battlenetoauth.secret"))
                    .scope("wow.profile")
                    .callback("https://legendarybot.greatmancode.com/auth/battlenetcallback")
                    .build(new OAuthBattleNetApi(region));
            String oAuthCode = req.queryParams("code");
            OAuth2AccessToken token = service.getAccessToken(oAuthCode); //TODO: Save oauth code to do a character refresh.
            OAuthRequest request = new OAuthRequest(Verb.GET,"https://"+region+".api.battle.net/wow/user/characters");
            service.signRequest(token, request);
            Response response = service.execute(request);
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response.getBody());
            JSONArray charactersArray = (JSONArray) obj.get("characters");
            List<WoWCharacter> characterList = new ArrayList<>();
            charactersArray.forEach((c) -> {
                JSONObject jsonObject = (JSONObject) c;
                if (jsonObject.containsKey("guild")) {
                    characterList.add(new WoWCharacter((String)jsonObject.get("name"),((String)jsonObject.get("realm")).toLowerCase(), (String)jsonObject.get("guild"), region, HeroClass.values()[((Long) jsonObject.get("class")).intValue()]));
                    log.info("User " + state.split(":")[1] + " user have the character " + jsonObject.get("name") + " in guild " + jsonObject.get("guild"));
                }
            });
            if (characterList.size() > 0) {
                MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
                characterList.forEach((c) -> collection.updateOne(and(eq("region",c.getRegion()), eq("realm", c.getRealm()), eq("name",c.getCharacterName())),and(set("guild",c.getGuild()),set("owner", state.split(":")[1])),new UpdateOptions().upsert(true)));
            }
            return "Your WoW characters are now synced to LegendaryBot!";
        }));


        getBot().getCommandHandler().addCommand("linkwowchars", new LinkWoWCharsCommand(this), "World of Warcraft Character");
        getBot().getCommandHandler().addCommand("guildchars", new GuildCharsCommand(this), "World of Warcraft Character");
        getBot().getCommandHandler().addCommand("setmainchar", new SetMainCharacterCommand(this),"World of Warcraft Character");
        getBot().getCommandHandler().addCommand("enableautorank", new EnableAutoRankCommand(this),"WoW Admin Commands");
        getBot().getCommandHandler().addCommand("disableautorank", new DisableAutoRankCommand(this), "WoW Admin Commands");
        getBot().getCommandHandler().addCommand("setwowrank", new SetWoWRankCommand(this), "WoW Admin Commands");
        getBot().getCommandHandler().addCommand("syncrank", new SyncRankCommand(this), "World of Warcraft Character");
        getBot().getCommandHandler().addCommand("syncguild", new SyncGuildCommand(this), "WoW Admin Commands");
        getBot().getCommandHandler().addCommand("enableautorankupdate", new EnableAutoRankUpdateCommand(this), "WoW Admin Commands");
        getBot().getCommandHandler().addCommand("disableautorankupdate", new DisableAutoRankUpdateCommand(this), "WoW Admin Commands");


        //We load the scheduler
        getBot().getJDA().forEach((jda -> {
            jda.getGuilds().forEach(guild -> {
                if (getBot().getGuildSettings(guild).getSetting(SETTING_SCHEDULER) != null && getBot().getGuildSettings(guild).getSetting(SETTING_RANKSET_ENABLED) != null) {
                    //scheduler.put(guild.getId(), new SyncRankScheduler(this,guild));
                }
            });
        }));
    }

    @Override
    public void stop() {

        Spark.stop();
        getBot().getCommandHandler().removeCommand("linkwowchars");
        getBot().getCommandHandler().removeCommand("guildchars");
        getBot().getCommandHandler().removeCommand("setmainchar");
        getBot().getCommandHandler().removeCommand("enableautorank");
        getBot().getCommandHandler().removeCommand("disableautorank");
        getBot().getCommandHandler().removeCommand("setwowrank");
        getBot().getCommandHandler().removeCommand("syncrank");
        getBot().getCommandHandler().removeCommand("syncguild");
        getBot().getCommandHandler().removeCommand("enableautorankupdate");
        getBot().getCommandHandler().removeCommand("disableautorankupdate");

        scheduler.forEach((k,v) -> v.stop());
        scheduler.clear();
    }

    /**
     * Retrieve all characters of a player in a specific guild
     * @param user The user to get the characters from
     * @param guild The guild to retrieve the characters from.
     * @return A List containing all characters of a player that belong to a specific guild.
     */
    public List<String> getUserCharactersInGuild(User user, Guild guild) {
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        List<String> charactersList = new ArrayList<>();
        collection.find(and(eq("owner",user.getId()), eq("guild",getBot().getGuildSettings(guild).getGuildName()), eq("region", getBot().getGuildSettings(guild).getRegionName()))).forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                charactersList.add(document.getString("name"));
            }
        });
        return charactersList;
    }

    /**
     * Set the main character of a character in a guild.
     * @param user The user to set the main character to
     * @param guild The guild to set the main character of the user in.
     * @param character The character name that is the main character of the player.
     */
    public void setMainCharacterForGuild(User user, Guild guild, String character) {
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        //We first search for an existing main character and unset it.
        Document document = collection.find(and(eq("owner", user.getId()), eq("guild_id", guild.getId()))).first();
        if (document != null) {
            collection.updateOne(eq("_id", document.getObjectId("_id")), unset("guild_id"));
        }
        collection.updateOne(and(eq("owner",user.getId()), eq("guild",getBot().getGuildSettings(guild).getGuildName()), eq("region", getBot().getGuildSettings(guild).getRegionName()), eq("name", character)), set("guild_id", guild.getId()));
    }

    /**
     * Retrieve the main character of a user in a guild.
     * @param user The user to retrieve the main character from.
     * @param guild The guild to retrieve the main character from.
     * @return The name of the main character of a user. Returns null if not found.
     */
    public String getMainCharacterForUserInGuild(User user, Guild guild) {
        MongoCollection<Document> collection = getBot().getMongoDatabase().getCollection(MONGO_WOW_CHARACTERS_COLLECTION);
        //We first search for an existing main character and unset it.
        Document document = collection.find(and(eq("owner", user.getId()), eq("guild_id", guild.getId()))).first();

        return document != null ? document.getString("name") : null;
    }

    /**
     * Get the Discord rank linked to a WoW guild rank.
     * @param guild The guild to get the setting from.
     * @param character The character to get the rank from.
     * @return The Discord rank of a character.
     */
    public String getWoWRank(Guild guild, String character) {
        int[] rank = new int[1];
        rank[0] = -1;
        String rankDiscord = null;
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(getBot().getGuildSettings(guild).getRegionName() + ".api.battle.net")
                .addPathSegments("/wow/guild/" + getBot().getGuildSettings(guild).getWowServerName()+"/" + getBot().getGuildSettings(guild).getGuildName())
                .addQueryParameter("fields", "members")
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            String result = client.newCall(request).execute().body().string();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result);
            if (!jsonObject.containsKey("status")) {
                JSONArray membersArray = (JSONArray) jsonObject.get("members");
                for (Object e: membersArray) {
                    JSONObject entry = (JSONObject) e;
                    JSONObject characterEntry = (JSONObject) entry.get("character");
                    if (characterEntry.get("name").equals(character)) {
                        rank[0] = Long.valueOf((long) entry.get("rank")).intValue();
                        break;
                    }
                }
            }
            //We found a rank. Translate it.
            if (rank[0] != -1) {
                rankDiscord = getBot().getGuildSettings(guild).getSetting(SETTING_RANK_PREFIX + rank[0]);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        return rankDiscord;
    }

    /**
     * Set the Discord rank of a player. If the bot doesn't find the rank in the Discord Guild or can't set the rank, nothing happens.
     * @param user The user to set the rank to.
     * @param guild The guild we want to set the rank in
     * @param rank The rank we want to set.
     */
    public void setDiscordRank(User user, Guild guild, String rank) {
        if (rank == null) {
            return;
        }
        List<Role> botRole = guild.getMember(getBot().getJDA(guild).getSelfUser()).getRoles();
        final int[] botRoleRank = {-999};
        botRole.forEach(r -> {
            if (r.getPosition() > botRoleRank[0]) {
                botRoleRank[0] = r.getPosition();
            }
        });

        List<Role> rolesToAdd = guild.getRolesByName(rank, true);
        if (rolesToAdd.isEmpty()) {
            return;
        }

        if (rolesToAdd.get(0).getPosition() > botRoleRank[0]) {
            return; //Can't set a rank higher than us.
        }


        //We try to load all the other ranks so we do a cleanup at the same time.
        Set<String> allRanks = new HashSet<>();
        GuildSettings settings = getBot().getGuildSettings(guild);
        for(int i = 0; i <= 9; i++) {
            String entry = settings.getSetting(SETTING_RANK_PREFIX + i);
            if (entry != null) {
                allRanks.add(entry);
            }
        }
        if (allRanks.contains(rank)) {
            allRanks.remove(rank);
        }

        List<Role> rolesToRemove = new ArrayList<>();
        List<Role> memberRoles = guild.getMember(user).getRoles();
        for(String rankEntry: allRanks) {
            List<Role> roles = guild.getRolesByName(rankEntry, false);
            if (!roles.isEmpty()){
                if (memberRoles.contains(roles.get(0))) {
                    if (roles.get(0).getPosition() < botRoleRank[0]) {
                        rolesToRemove.add(roles.get(0));
                    } else {
                        log.info("The bot can't remove rank " + rankEntry + " on user " + user.getName());
                    }
                } else {
                    log.info("Role " + rankEntry + " not on user" + user.getName());
                }

            } else {
                log.info("Role " + rankEntry + " not found!");
            }
        }



        try {
            guild.getController().modifyMemberRoles(guild.getMember(user), rolesToAdd, rolesToRemove).reason("LegendaryBot - Rank Sync with WoW Guild.").queue();
            log.info("User " + user.getName());
            log.info("Adding ranks:");
            rolesToAdd.forEach(v -> log.info(v.getName()));
            log.info("Removing ranks:");
            rolesToRemove.forEach(v -> log.info(v.getName()));
        } catch (PermissionException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }

    }

    public void enableAutoRankUpdate(Guild guild) {
        getBot().getGuildSettings(guild).setSetting(SETTING_SCHEDULER, "true");
        scheduler.put(guild.getId(), new SyncRankScheduler(this, guild));
    }

    public void disableAutoRankUpdate(Guild guild) {
        getBot().getGuildSettings(guild).unsetSetting(SETTING_SCHEDULER);
        scheduler.get(guild.getId()).stop();
        scheduler.remove(guild.getId());
    }
}
