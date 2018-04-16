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

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.plugins.wowlink.commands.*;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

/**
 * The WowLink plugin main class.
 */
public class WoWLinkPlugin extends LegendaryBotPlugin {

    public static final String SETTING_RANKSET_ENABLED = "wowlink_rankset";
    public static final String SETTING_SCHEDULER = "wowlink_scheduler";
    public static final String SETTING_RANK_PREFIX = "wowlink_rank_";

    /**
     * The HttpClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient.Builder().build();

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, SyncRankScheduler> scheduler = new HashMap<>();

    public WoWLinkPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {

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
        getBot().getJDA().forEach((jda -> jda.getGuilds().forEach(guild -> {
            if (getBot().getGuildSettings(guild).getSetting(SETTING_SCHEDULER) != null && getBot().getGuildSettings(guild).getSetting(SETTING_RANKSET_ENABLED) != null) {
                scheduler.put(guild.getId(), new SyncRankScheduler(this,guild));
            }
        })));
    }

    @Override
    public void stop() {

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
        List<String> charactersList = new ArrayList<>();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getBot().getBotSettings().getProperty("api.host"))
                .addPathSegments("api/user/" + user.getId() + "/character/all/"+ getBot().getGuildSettings(guild).getGuildName())
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            JSONArray array = new JSONArray(result);
            array.forEach(entry -> charactersList.add((String) entry));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return charactersList;
    }

    /**
     * Set the main character of a character in a guild.
     * @param user The user to set the main character to
     * @param guild The guild to set the main character of the user in.
     * @param character The character name that is the main character of the player.
     */
    public void setMainCharacterForGuild(User user, Guild guild, String character) { //TODO change void for boolean
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getBot().getBotSettings().getProperty("api.host"))
                .addPathSegments("api/user/"+ user.getId() + "/character/" + guild.getId() + "/"+getBot().getGuildSettings(guild).getRegionName() + "/" + getBot().getGuildSettings(guild).getWowServerName() + "/" + character)

                .build();
        Request request = new Request.Builder().url(url).addHeader("x-api-key", getBot().getBotSettings().getProperty("api.key")).build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieve the main character of a user in a guild.
     * @param user The user to retrieve the main character from.
     * @param guild The guild to retrieve the main character from.
     * @return The region/realm/name of the main character of a user. Returns null if not found.
     */
    public JSONObject getMainCharacterForUserInGuild(User user, Guild guild) {
        JSONObject character = null;
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getBot().getBotSettings().getProperty("api.host"))
                .addPathSegments("api/user/" + user.getId() + "/character/"+guild.getId())
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject jsonObject = new JSONObject(response.body().source());
            character = jsonObject.length() > 0 ? jsonObject : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return character;
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
        /*try {
            String result = clientBattleNet.newCall(request).execute().body().string();
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
        }*/
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
