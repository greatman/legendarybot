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
import com.greatmancode.legendarybot.plugins.wowlink.commands.*;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * The WowLink plugin main class.
 */
public class WoWLinkPlugin extends LegendaryBotPlugin {

    public static final String SETTING_RANKSET_ENABLED = "wowlink_rankset";
    public static final String SETTING_SCHEDULER = "wowlink_scheduler";
    public static final String SETTING_RANKS = "wowranks";
    /**
     * The HttpClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient.Builder().build();

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

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
        getBot().getCommandHandler().addCommand("getwowranks", new GetWowRanksCommand(this), "WoW Admin Commands");

        WoWLinkPlugin plugin = this;
        //We load the scheduler
        new Thread(() -> getBot().getJDA().forEach((jda -> jda.getGuilds().forEach(guild -> {
            if (getBot().getGuildSettings(guild).getSetting(SETTING_SCHEDULER) != null && getBot().getGuildSettings(guild).getSetting(SETTING_RANKSET_ENABLED) != null) {
                scheduler.put(guild.getId(), new SyncRankScheduler(plugin,guild));
            }
        })))).start();

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
        getBot().getCommandHandler().removeCommand("getwowranks");
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
        Request request = new Request.Builder().url(url).addHeader("x-api-key", getBot().getBotSettings().getProperty("api.key")).post(RequestBody.create(null, new byte[]{})).build();
        try {
            Response response = client.newCall(request).execute();
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
        JSONObject character = new JSONObject();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getBot().getBotSettings().getProperty("api.host"))
                .addPathSegments("api/user/" + user.getId() + "/character/"+guild.getId())
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println(response);
            JSONObject jsonObject = new JSONObject(response.body().string());
            System.out.println(jsonObject);
            character = jsonObject.length() > 1 ? jsonObject : new JSONObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return character;
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

    public void doGuildRankUpdate(User initiator, Guild guild) {
        doGuildRankUpdate(initiator, guild, guild.getMembers());
    }
    public void doGuildRankUpdate(User initiator, Guild guild, List<Member> members) {
        JSONObject jsonObject = new JSONObject();
        JSONObject guildObject = new JSONObject();
        JSONArray guildRanks = new JSONArray();
        List<Role> guildRoles = guild.getRoles();
        guildRoles.forEach(role -> {
            JSONObject roleJSON = new JSONObject();
            roleJSON.put("name", role.getName());
            roleJSON.put("position", role.getPosition());
            roleJSON.put("managerole", role.getPermissions().contains(Permission.MANAGE_ROLES));
            guildRanks.put(roleJSON);
        });
        guildObject.put("ranks", guildRanks);

        JSONArray botRoles = new JSONArray();
        guild.getSelfMember().getRoles().forEach(role -> botRoles.put(role.getName()));
        guildObject.put("botranks", botRoles);
        jsonObject.put("guild", guildObject);

        JSONArray users = new JSONArray();
        members.forEach(member -> {
            JSONObject userJSON = new JSONObject();
            userJSON.put("discordId", member.getUser().getIdLong());
            JSONArray userRanks = new JSONArray();
            member.getRoles().forEach(role -> userRanks.put(role.getName()));
            userJSON.put("ranks", userRanks);
            users.put(userJSON);
        });

        jsonObject.put("users", users);
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(getBot().getBotSettings().getProperty("api.host"))
                .addPathSegments("api/guild/" + guild.getId() + "/rankUpdate")
                .build();
        Request request = new Request.Builder().url(url).addHeader("x-api-key", getBot().getBotSettings().getProperty("api.key")).post(RequestBody.create(JSON_MEDIA_TYPE, jsonObject.toString())).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject responseJSON = new JSONObject(response.body().string());
            if (responseJSON.has("roleChange")) {
                MessageBuilder builder = new MessageBuilder();
                responseJSON.getJSONArray("roleChange").forEach(roleChangeEntry -> {
                    JSONObject roleChange = (JSONObject) roleChangeEntry;
                    Member member = guild.getMemberById(roleChange.getLong("discordId"));
                    List<Role> roleToAdd = new ArrayList<>();
                    List<Role> rolesToRemove = new ArrayList<>();
                    if (roleChange.has("rankToAdd")) {
                        roleToAdd.add(guildRoles.stream().filter(r -> r.getName().equals(roleChange.getString("rankToAdd"))).findFirst().orElse(null));
                    }
                    if (roleChange.has("ranksToRemove")) {
                        JSONArray array = roleChange.getJSONArray("ranksToRemove");
                        array.forEach(rankToRemoveEntry -> {
                            String rankToRemove = (String) rankToRemoveEntry;
                            rolesToRemove.add(guildRoles.stream().filter(r -> r.getName().equals(rankToRemove)).findFirst().orElse(null));
                        });
                    }

                    if (roleToAdd.size() > 0 || rolesToRemove.size() > 0) {
                        if (getBot().getGuildSettings(guild).getSetting(SETTING_RANKSET_ENABLED) != null) {
                            guild.getController().modifyMemberRoles(member, roleToAdd, rolesToRemove).reason("LegendaryBot WoW rank sync").queue();
                        } else {
                            builder.append("Changing user " + member.getNickname() + " Adding:" + Arrays.toString(roleToAdd.toArray()) + " Removing:" + Arrays.toString(rolesToRemove.toArray()));
                        }
                    }
                });
                if (!builder.isEmpty()) {
                    initiator.openPrivateChannel().queue(channel -> channel.sendMessage(builder.build()).queue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
