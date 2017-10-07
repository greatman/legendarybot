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
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;
import spark.Spark;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static spark.Spark.get;
import static spark.Spark.path;

public class WoWLinkPlugin extends LegendaryBotPlugin {

    public static final String SETTING_RANKSET_ENABLED = "wowlink_rankset";
    public static final String SETTING_RANK_PREFIX = "wowlink_rank_";
    /**
     * The HttpClient to do web requests.
     */
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();
    private Properties props;
    public WoWLinkPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }


    public Properties getProps() {
        return props;
    }

    @Override
    public void start() throws PluginException {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }

        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `user_characters`(" +
                    "  `user_id` VARCHAR(64) NOT NULL," +
                    "  `characterName` VARCHAR(45) NOT NULL," +
                    "  `realmName` VARCHAR(45) NOT NULL," +
                    "  `region` VARCHAR(45) NOT NULL," +
                    "  `guildName` VARCHAR(45) NOT NULL," +
                    "  PRIMARY KEY (`user_id`, `characterName`, `realmName`, `region`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;");
            statement.executeUpdate();
            statement.close();
            statement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS `user_characters_guild` (" +
                    "  `user_id` VARCHAR(64) NOT NULL," +
                    "  `guild_id` VARCHAR(64) NOT NULL," +
                    "  `characterName` VARCHAR(45) NULL," +
                    "  PRIMARY KEY (`user_id`, `guild_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;");
            statement.executeUpdate();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
            System.out.println(token);
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
                    characterList.add(new WoWCharacter((String)jsonObject.get("name"),(String)jsonObject.get("realm"), (String)jsonObject.get("guild"), region, HeroClass.values()[((Long) jsonObject.get("class")).intValue()]));
                }
            });
            if (characterList.size() > 0) {
                try {
                    Connection conn = getBot().getDatabase().getConnection();
                    final String[] statement = {"INSERT INTO user_characters(user_id,characterName,realmName,region,guildName) VALUES"};
                    characterList.forEach((c) -> statement[0] += "(?,?,?,?,?),");
                    statement[0] = statement[0].substring(0,statement[0].length() - 1);
                    statement[0] += " ON DUPLICATE KEY UPDATE guildName=VALUES(guildName)";
                    System.out.println(statement[0]);
                    PreparedStatement preparedStatement = conn.prepareStatement(statement[0]);
                    final int[] i = {1};
                    characterList.forEach((c) -> {
                        try {
                            preparedStatement.setString(i[0]++,state.split(":")[1]);
                            preparedStatement.setString(i[0]++,c.getCharacterName());
                            preparedStatement.setString(i[0]++,c.getRealm());
                            preparedStatement.setString(i[0]++,c.getRegion());
                            preparedStatement.setString(i[0]++,c.getGuild());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    preparedStatement.executeUpdate();
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return "Your WoW characters are now synced to LegendaryBot!";
        }));


        getBot().getCommandHandler().addCommand("linkwowchars", new LinkWoWCharsCommand(this));
        getBot().getCommandHandler().addCommand("guildchars", new GuildCharsCommand(this));
        getBot().getCommandHandler().addCommand("setmainchar", new SetMainCharacterCommand(this));
        getBot().getCommandHandler().addCommand("enableautorank", new EnableAutoRankCommand(this));
        getBot().getCommandHandler().addCommand("disableautorank", new DisableAutoRankCommand(this));
        getBot().getCommandHandler().addCommand("setwowrank", new SetWoWRankCommand(this));
        getBot().getCommandHandler().addCommand("syncrank", new SyncRankCommand(this));
        getBot().getCommandHandler().addCommand("syncguild", new SyncGuildCommand(this));
    }

    @Override
    public void stop() throws PluginException {
        Spark.stop();
        getBot().getCommandHandler().removeCommand("linkwowchars");
        getBot().getCommandHandler().removeCommand("guildchars");
        getBot().getCommandHandler().removeCommand("setmainchar");
        getBot().getCommandHandler().removeCommand("enableautorank");
        getBot().getCommandHandler().removeCommand("disableautorank");
        getBot().getCommandHandler().removeCommand("setwowrank");
        getBot().getCommandHandler().removeCommand("syncrank");
        getBot().getCommandHandler().removeCommand("syncguild");
    }

    public List<String> getUserCharactersInGuild(User user, Guild guild) {
        List<String> charactersList = new ArrayList<>();
        try {
            Connection conn = getBot().getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT * FROM user_characters WHERE user_id=? AND guildName=? AND region=?");
            statement.setString(1,user.getId());
            statement.setString(2, getBot().getGuildSettings(guild).getGuildName());
            statement.setString(3,getBot().getGuildSettings(guild).getRegionName());
            ResultSet set = statement.executeQuery();
            while(set.next()) {
                charactersList.add(set.getString("characterName"));
            }
            set.close();
            statement.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        return charactersList;
    }

    public void setMainCharacterForGuild(User user, Guild guild, String character) throws SQLException {
        Connection conn = getBot().getDatabase().getConnection();
        PreparedStatement statement = conn.prepareStatement("INSERT INTO user_characters_guild VALUES(?,?,?) ON DUPLICATE KEY UPDATE characterName=VALUES(characteRName)");
        statement.setString(1,user.getId());
        statement.setString(2,guild.getId());
        statement.setString(3, character);
        statement.executeUpdate();
        statement.close();
        conn.close();
    }

    public String getMainCharacterForUserInGuild(User user, Guild guild) throws SQLException {
        String character = null;
        Connection connection = getBot().getDatabase().getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT characterName FROM user_characters_guild WHERE user_id=? AND guild_id=?");
        statement.setString(1, user.getId());
        statement.setString(2, guild.getId());
        ResultSet set = statement.executeQuery();
        if (set.next()) {
            character = set.getString("characterName");
        }
        set.close();
        statement.close();
        connection.close();
        return character;
    }

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

    public void setDiscordRank(User user, Guild guild, String rank) {
        if (rank == null) {
            return;
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
        for(String rankEntry: allRanks) {
            rolesToRemove.add(guild.getRolesByName(rankEntry, true).get(0));
        }
        List<Role> rolesToAdd = new ArrayList<>();
        rolesToAdd.add(guild.getRolesByName(rank, true).get(0));
        try {
            System.out.println(guild.getRolesByName(rank, true));
            guild.getController().modifyMemberRoles(guild.getMember(user), rolesToAdd, rolesToRemove).reason("LegendaryBot - Rank Sync with WoW Guild.").queue();
        } catch (PermissionException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }

    }
}
