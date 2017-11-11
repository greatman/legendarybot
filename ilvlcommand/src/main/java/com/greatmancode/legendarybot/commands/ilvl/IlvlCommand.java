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
import net.dv8tion.jda.core.EmbedBuilder;
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

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class IlvlCommand extends LegendaryBotPlugin implements WowCommand, PublicCommand {

    private final OkHttpClient battleNetClient = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .build();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    public IlvlCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("lookup", this);
        getBot().getCommandHandler().addAlias("ilvl", "lookup");
        getBot().getCommandHandler().addAlias("mplusrank", "lookup");
        getBot().getCommandHandler().addAlias("raidrank", "lookup");
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("ilvl");
        getBot().getCommandHandler().removeCommand("info");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = null, region = null;
        try {
            if (args.length == 1) {
                serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
                region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            } else if (args.length == 2){
                serverName = args[1];
                region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            } else {
                serverName = args[1];
                region = args[2];
            }

            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host("raider.io")
                    .addPathSegments("api/v1/characters/profile")
                    .addQueryParameter("region", region)
                    .addQueryParameter("realm", serverName)
                    .addQueryParameter("name", args[0])
                    .addQueryParameter("fields", "gear,raid_progression,mythic_plus_scores,previous_mythic_plus_scores,mythic_plus_best_runs")
                    .build();
            Request request = new Request.Builder().url(url).build();
            String result = client.newCall(request).execute().body().string();
            if (result != null) {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(result);
                if (!jsonObject.containsKey("error")) {
                    EmbedBuilder eb = new EmbedBuilder();
                    if (jsonObject.get("name").equals("Pepyte") && jsonObject.get("realm").equals("Arthas")) {
                        eb.setThumbnail("https://lumiere-a.akamaihd.net/v1/images/b5e11dc889c5696799a6bd3ec5d819c1f7dfe8b4.jpeg");
                    } else {
                        eb.setThumbnail(jsonObject.get("thumbnail_url").toString());
                    }

                    String className = jsonObject.get("class").toString().toLowerCase();
                    switch (className) {
                        case "death knight":
                            eb.setColor(new Color(196,30,59));
                            break;
                        case "demon hunter":
                            eb.setColor(new Color(163,48,201));
                            break;
                        case "druid":
                            eb.setColor(new Color(255,125,10));
                            break;
                        case "hunter":
                            eb.setColor(new Color(171,212,115));
                            break;
                        case "mage":
                            eb.setColor(new Color(105,204,240));
                            break;
                        case "monk":
                            eb.setColor(new Color(0,255,150));
                            break;
                        case "paladin":
                            eb.setColor(new Color(245,140,186));
                            break;
                        case "priest":
                            eb.setColor(new Color(255,255,255));
                            break;
                        case "rogue":
                            eb.setColor(new Color(255,245,105));
                            break;
                        case "shaman":
                            eb.setColor(new Color(0,112,222));
                            break;
                        case "warlock":
                            eb.setColor(new Color(148,130,201));
                            break;
                        case "warrior":
                            eb.setColor(new Color(199,156,110));
                    }
                    StringBuilder titleBuilder = new StringBuilder();
                    titleBuilder.append(jsonObject.get("name"));
                    titleBuilder.append(" ");
                    titleBuilder.append(jsonObject.get("realm"));
                    titleBuilder.append(" - ");
                    titleBuilder.append(((String) jsonObject.get("region")).toUpperCase());
                    titleBuilder.append(" | ");
                    titleBuilder.append(jsonObject.get("race"));
                    titleBuilder.append(" ");
                    titleBuilder.append(jsonObject.get("active_spec_name"));
                    titleBuilder.append(" ");
                    titleBuilder.append(jsonObject.get("class"));
                    eb.setTitle(titleBuilder.toString());

                    StringBuilder progressionBuilder = new StringBuilder();
                    JSONObject raidProgression = (JSONObject) jsonObject.get("raid_progression");
                    JSONObject emeraldNightmare = (JSONObject) raidProgression.get("the-emerald-nightmare");
                    JSONObject trialOfValor = (JSONObject) raidProgression.get("trial-of-valor");
                    JSONObject theNighthold = (JSONObject) raidProgression.get("the-nighthold");
                    JSONObject tombOfSargeras = (JSONObject) raidProgression.get("tomb-of-sargeras");
                    progressionBuilder.append("**EN**: ");
                    progressionBuilder.append(emeraldNightmare.get("summary"));
                    progressionBuilder.append(" - ");
                    progressionBuilder.append("**ToV**:");
                    progressionBuilder.append(trialOfValor.get("summary"));
                    progressionBuilder.append(" - ");
                    progressionBuilder.append("**NH**: ");
                    progressionBuilder.append(theNighthold.get("summary"));
                    progressionBuilder.append(" - ");
                    progressionBuilder.append("**ToS**: ");
                    progressionBuilder.append(tombOfSargeras.get("summary"));
                    eb.addField("Progression", progressionBuilder.toString(), false);

                    JSONObject gear = (JSONObject) jsonObject.get("gear");
                    eb.addField("iLVL", gear.get("item_level_equipped") + "/" + gear.get("item_level_total"), true);
                    eb.addField("Artifact Power", gear.get("artifact_traits").toString(), true);



                    JSONObject mplusRank = (JSONObject) jsonObject.get("mythic_plus_scores");
                    eb.addField("Mythic+ Score", mplusRank.get("all").toString(), true);
                    JSONObject lastMplusRank = (JSONObject) jsonObject.get("previous_mythic_plus_scores");
                    eb.addField("Last Season Mythic+ Score", lastMplusRank.get("all").toString(), true);


                    StringBuilder runsBuilder = new StringBuilder();
                    JSONArray bestRuns = (JSONArray) jsonObject.get("mythic_plus_best_runs");
                    for (Object runObject : bestRuns) {
                        JSONObject run = (JSONObject) runObject;
                        runsBuilder.append("[");
                        runsBuilder.append(run.get("dungeon"));
                        runsBuilder.append(" **+");
                        runsBuilder.append(run.get("mythic_level"));
                        runsBuilder.append("**](");
                        runsBuilder.append(run.get("url"));
                        runsBuilder.append(")\n");
                        long time = (long) run.get("clear_time_ms");
                        long hours = TimeUnit.MILLISECONDS.toHours(time);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));
                        runsBuilder.append("    ");
                        if (hours >= 1) {
                            runsBuilder.append(String.format("%d Hour(s) %d Minute(s), %d seconds", hours, minutes, seconds));
                        } else {
                            runsBuilder.append(String.format("%d Minute(s), %d seconds", minutes, seconds));
                        }
                        runsBuilder.append(" | ");
                        runsBuilder.append(run.get("num_keystone_upgrades").toString());
                        runsBuilder.append(" Chest(s)\n\n");
                    }
                    eb.addField("Best Mythic+ Runs", runsBuilder.toString(), false);

                    event.getChannel().sendMessage(eb.build()).queue();
                } else {
                    event.getChannel().sendMessage(jsonObject.get("message").toString()).queue();
                    return;
                }
            }
        } catch (IOException e) {
            getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            event.getChannel().sendMessage("An error occured. Try again later!").queue();
        } catch (ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            event.getChannel().sendMessage("An error occured. Try again later!").queue();
        }

    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 3;
    }

    public String help() {
        return  "lookup [Character Name] <Server Name> <region> - Lookup a character information (iLVL/Raid Progression/Mythic+)";
    }
}
