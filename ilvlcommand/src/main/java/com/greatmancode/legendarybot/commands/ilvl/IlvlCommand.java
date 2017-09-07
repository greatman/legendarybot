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
package com.greatmancode.legendarybot.commands.ilvl;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.api.utils.Hero;
import com.greatmancode.legendarybot.api.utils.HeroClass;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WowCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;

public class IlvlCommand extends LegendaryBotPlugin implements WowCommand, PublicCommand {

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();

    public IlvlCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("ilvl", this);
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("ilvl");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = args[0];
        Hero hero = null;
        try {


            if (args.length == 1) {
                serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
                hero = getiLvl(getBot().getGuildSettings(event.getGuild()).getRegionName(),serverName, args[0]);
            } else {
                hero = getiLvl(getBot().getGuildSettings(event.getGuild()).getRegionName(),serverName, args[1]);
            }

            if (hero != null) {
                event.getChannel().sendMessage(hero.getName() + " ("+hero.getHeroClass()+" "+hero.getLevel()+") ilvl is " + hero.getEquipilvl() + "/" + hero.getIlvl()).queue();
            } else {
                event.getChannel().sendMessage("WowCharacter not found!").queue();
            }
        } catch (IOException e) {
            getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName, "hero:" + hero);
            event.getChannel().sendMessage("An error occured. Try again later!").queue();
        }

    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 2;
    }

    public String help() {
        return  "ilvl [Character Name] <Server Name> - Retrieve a character iLvl";
    }

    /**
     * Retrieve the iLvl of a World of Warcraft character.
     * @param region The Region the server is hosted in.
     * @param serverName The server name the character belongs to.
     * @param character The character name
     * @return A {@link Hero} containing the Name, the {@link HeroClass}, the level, the equipped iLvl and the unequipped (bag) iLvl.
     */
    public Hero getiLvl(String region, String serverName, String character) throws IOException {
        JSONParser parser = new JSONParser();
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(region + ".api.battle.net")
                .addPathSegments("/wow/character/" + serverName + "/" + character)
                .addQueryParameter("fields", "items")
                .addQueryParameter("locale", "en_US")
                .build();
        Request request = new Request.Builder().url(url).build();
        String result = client.newCall(request).execute().body().string();
        if (result == null) {
            //We received a empty result. Is he part of a connected realm?
            url = new HttpUrl.Builder().scheme("https")
                    .host(region + ".api.battle.net")
                    .addPathSegments("/wow/realm/status")
                    .addQueryParameter("locale", "en_US")
                    .addQueryParameter("realms", serverName)
                    .build();
            request = new Request.Builder().url(url).build();
            result = client.newCall(request).execute().body().string();
            if (result != null) {
                try {
                    JSONObject json = (JSONObject) parser.parse(result);
                    JSONArray array = (JSONArray) json.get("realms");
                    JSONObject realm = (JSONObject) array.get(0);
                    JSONArray realms = (JSONArray)realm.get("connected_realms");
                    for (Object realmEntry: realms) {
                        url = new HttpUrl.Builder().scheme("https")
                                .host(region + ".api.battle.net")
                                .addPathSegments("/wow/character/" + realmEntry + "/" + character)
                                .addQueryParameter("fields", "items")
                                .build();
                        request = new Request.Builder().url(url).build();
                        result = client.newCall(request).execute().body().string();
                        if (result != null) {
                            break;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName, "character:" + character);
                }

            }
            if (result == null) {
                return null;
            }
        }
        try {
            JSONObject object = (JSONObject) parser.parse(result);
            if (!object.containsKey("name") || !object.containsKey("class") || !object.containsKey("level")) {
                return null;
            }
            return new Hero((String)object.get("name"), HeroClass.values()[((Long) object.get("class")).intValue()], (Long)object.get("level"), (Long)((JSONObject)object.get("items")).get("averageItemLevel"), (Long)((JSONObject)object.get("items")).get("averageItemLevelEquipped"));
        } catch (ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "region:" + region, "serverName:" + serverName, "character:" + character);
        }
        return null;
    }
}
