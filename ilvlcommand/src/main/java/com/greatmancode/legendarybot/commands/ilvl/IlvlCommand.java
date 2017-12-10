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
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WowCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.ConnectionPool;
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
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * The !lookup command
 */
public class IlvlCommand extends LegendaryBotPlugin implements WowCommand, PublicCommand {


    /**
     * The OKHttp client
     */
    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    OkHttpClient clientBattleNet = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(getBot()))
            .connectionPool(new ConnectionPool(300, 1, TimeUnit.SECONDS))
            .build();

    public IlvlCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("lookup", this, "World of Warcraft");
        getBot().getCommandHandler().addAlias("ilvl", "lookup");
        getBot().getCommandHandler().addAlias("mplusrank", "lookup");
        getBot().getCommandHandler().addAlias("raidrank", "lookup");
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("lookup");
        getBot().getCommandHandler().removeAlias("ilvl");
        getBot().getCommandHandler().removeAlias("mplusrank");
        getBot().getCommandHandler().removeAlias("raidrank");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = null;
        String region = null;
        try {
            if (args.length == 1) {
                serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
                region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            } else if (args.length == 2){
                serverName = args[1];
                region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            } else {
                //We got a long server name potentially
                if (args[args.length - 1].equalsIgnoreCase("US") || args[args.length - 1].equalsIgnoreCase("EU")) {
                    //Last argument is the region, taking the rest for the realm info
                    String[] argsend = new String[args.length - 2];
                    System.arraycopy(args,1,argsend,0,args.length - 2);
                    StringBuilder builder = new StringBuilder();
                    for(String s : argsend) {
                        builder.append(" ").append(s);
                    }
                    serverName = builder.toString().trim();
                    region = args[args.length - 1];
                } else {
                    String[] argsend = new String[args.length - 1];
                    System.arraycopy(args,1,argsend,0,args.length - 1);
                    StringBuilder builder = new StringBuilder();
                    for(String s : argsend) {
                        builder.append(" ").append(s);
                    }
                    serverName = builder.toString().trim();
                    region = getBot().getGuildSettings(event.getGuild()).getRegionName();
                }
            }

            String realmData = WoWUtils.getRealmInformation(getBot(),region,serverName);
            if (realmData == null) {
                event.getChannel().sendMessage("Realm not found! Did you make a typo?").queue();
                return;
            }
            JSONParser parser = new JSONParser();
            JSONObject realmInformation = (JSONObject) parser.parse(realmData);
            String serverSlug = (String) realmInformation.get("slug");

            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host("raider.io")
                    .addPathSegments("api/v1/characters/profile")
                    .addQueryParameter("region", region)
                    .addQueryParameter("realm", serverSlug)
                    .addQueryParameter("name", args[0])
                    .addQueryParameter("fields", "gear,raid_progression,mythic_plus_scores,previous_mythic_plus_scores,mythic_plus_best_runs")
                    .build();
            Request request = new Request.Builder().url(url).build();
            String result = client.newCall(request).execute().body().string();
            if (result != null) {
                JSONObject jsonObject = (JSONObject) parser.parse(result);
                if (!jsonObject.containsKey("error")) {



                    EmbedBuilder eb = new EmbedBuilder();
                    if (jsonObject.get("name").equals("Pepyte") && jsonObject.get("realm").equals("Arthas")) {
                        eb.setThumbnail("https://lumiere-a.akamaihd.net/v1/images/b5e11dc889c5696799a6bd3ec5d819c1f7dfe8b4.jpeg");
                    } else if (jsonObject.get("name").equals("Xdntgivitoya") && jsonObject.get("realm").equals("Arthas")) {
                        eb.setThumbnail("https://cdn.discordapp.com/attachments/239729214004133889/389452254823841792/20171210_112359.jpg");
                    } else {
                        eb.setThumbnail(jsonObject.get("thumbnail_url").toString());
                    }

                    String className = jsonObject.get("class").toString().toLowerCase();
                    eb.setColor(WoWUtils.getClassColor(className));
                    
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
                    String wowLink = null;
                    if (((String) jsonObject.get("region")).equalsIgnoreCase("us")) {
                        wowLink = "https://worldofwarcraft.com/en-us/character/" + serverSlug + "/" + jsonObject.get("name");
                    } else {
                        wowLink = "https://worldofwarcraft.com/en-gb/character/" + serverSlug + "/" + jsonObject.get("name");
                    }
                    eb.setTitle(titleBuilder.toString(), wowLink);

                    StringBuilder progressionBuilder = new StringBuilder();
                    JSONObject raidProgression = (JSONObject) jsonObject.get("raid_progression");
                    JSONObject emeraldNightmare = (JSONObject) raidProgression.get("the-emerald-nightmare");
                    JSONObject trialOfValor = (JSONObject) raidProgression.get("trial-of-valor");
                    JSONObject theNighthold = (JSONObject) raidProgression.get("the-nighthold");
                    JSONObject tombOfSargeras = (JSONObject) raidProgression.get("tomb-of-sargeras");
                    JSONObject antorus = (JSONObject) raidProgression.get("antorus-the-burning-throne");
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
                    progressionBuilder.append("\n");
                    progressionBuilder.append("**Antorus**: ");
                    progressionBuilder.append(antorus.get("summary"));
                    eb.addField("Progression", progressionBuilder.toString(), false);


                    //We fetch the Battle.net achivement record
                    HttpUrl battleneturl = new HttpUrl.Builder().scheme("https")
                            .host(region + ".api.battle.net")
                            .addPathSegments("wow/character/"+serverSlug+"/" +args[0])
                            .addQueryParameter("fields", "achievements")
                            .build();
                    Request battlenetRequest = new Request.Builder().url(battleneturl).build();
                    String battlenetResult = clientBattleNet.newCall(battlenetRequest).execute().body().string();
                    long apAmount = -1;
                    if (battlenetResult != null) {
                        JSONObject battleNetCharacter = (JSONObject) parser.parse(battlenetResult);
                        JSONObject achivements = (JSONObject) battleNetCharacter.get("achievements");
                        JSONArray criteriaObject = (JSONArray) achivements.get("criteria");
                        int criteriaNumber = -1;
                        for (int i = 0; i < criteriaObject.size(); i++) {
                            if ((long)criteriaObject.get(i) == 30103) {
                                criteriaNumber = i;
                            }
                        }

                        if (criteriaNumber != -1) {
                            apAmount = (long) ((JSONArray)achivements.get("criteriaQuantity")).get(criteriaNumber);
                        }
                    }
                    JSONObject gear = (JSONObject) jsonObject.get("gear");
                    eb.addField("iLVL", gear.get("item_level_equipped") + "/" + gear.get("item_level_total"), true);

                    if (apAmount != -1) {
                        eb.addField("Artifact Power", gear.get("artifact_traits").toString() + " / " + format(apAmount) + " AP Gathered", true);
                    } else {
                        eb.addField("Artifact Power", gear.get("artifact_traits").toString(), true);
                    }




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
                    eb.addField("WoWProgress", "[Click Here](https://www.wowprogress.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.addField("Raider.IO", "[Click Here](https://raider.io/characters/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.addField("WarcraftLogs","[Click Here](https://www.warcraftlogs.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.setFooter("Information taken from https://raider.io",null);

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
        return 99;
    }

    public String help() {
        return "Lookup a World of Warcraft character statistics.\n" +
                "**Format**: ``!lookup <Character Name> <Server Name> <Region>``\n\n" +
                "__Parameters__\n" +
                "**Character Name** (Required/Optional): A World of Warcraft character Name. This parameter is required if you don't have a main character set in this Discord server. Optional if you have one.\n" +
                "**Server Name** (Optional): The World of Warcraft realm you want to search on. If omitted, will take this Discord's default server.\n" +
                "**Region** (Optional): The Region you want to do the search in. If omitted, will take this Discord's default server.\n\n" +
                "**Example**: ``!lookup Kugruon Arthas US``";
    }

    @Override
    public String shortDescription() {
        return "Lookup a character information (iLVL/Raid Progression/Mythic+)";
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, " Million(s)");
        suffixes.put(1_000_000_000L, " Billion(s)");
        suffixes.put(1_000_000_000_000L, " Trillion(s)");
        suffixes.put(1_000_000_000_000_000L, " Quadrillion(s)");
        suffixes.put(1_000_000_000_000_000_000L, " Quintillion(s)");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
