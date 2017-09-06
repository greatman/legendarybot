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
import com.greatmancode.legendarybot.api.utils.HeroClass;
import com.greatmancode.legendarybot.plugins.wowlink.commands.LinkWoWCharsCommand;
import com.greatmancode.legendarybot.plugins.wowlink.utils.OAuthBattleNetApi;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WoWCharacter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;
import spark.Spark;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static spark.Spark.get;
import static spark.Spark.path;

public class WoWLinkPlugin extends LegendaryBotPlugin {


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
                    "  PRIMARY KEY (`user_id`, `characterName`, `realmName`, `region`));");
            statement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        path("/auth", () ->  {
            //get("/:discordid", (req,res) -> {
            //    OAuth20Service service = new ServiceBuilder(props.getProperty("battlenet.key"))
            //            .apiSecret(props.getProperty("battlenet.secret"))
            //            .scope("wow.profile")
            //            .callback("https://legendarybot.greatmancode.com/auth/battlenetcallback")
            //            .state(req.params(":discordid"))
            //            .build(new OAuthBattleNetApi(getbo));
            //    res.redirect(service.getAuthorizationUrl());
            //    return "";
            //});
            get("/battlenetcallback", (req,res) -> {
                String state = req.queryParams("state");
                String region = state.split(":")[0];
                OAuth20Service service = new ServiceBuilder(props.getProperty("battlenet.key"))
                        .apiSecret(props.getProperty("battlenet.secret"))
                        .scope("wow.profile")
                        .callback("https://legendarybot.greatmancode.com/auth/battlenetcallback")
                        .build(new OAuthBattleNetApi(region));
                String oAuthCode = req.queryParams("code");
                OAuth2AccessToken token = service.getAccessToken(oAuthCode);
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
                return "Done";
            });
        });


        getBot().getCommandHandler().addCommand("linkwowchars", new LinkWoWCharsCommand(this));
    }

    @Override
    public void stop() throws PluginException {
        Spark.stop();
        getBot().getCommandHandler().removeCommand("linkwowchars");
    }
}
