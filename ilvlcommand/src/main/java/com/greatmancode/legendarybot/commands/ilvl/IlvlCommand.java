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
import com.greatmancode.legendarybot.api.utils.HeroClass;
import com.greatmancode.legendarybot.api.utils.HeroRace;
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WowCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.text.DecimalFormat;
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

    public static final String SETTING_PRIVATE_LOOKUP = "lookupCommandPrivate";

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
        getBot().getCommandHandler().addCommand("privatelookup", new PrivateLookupCommand(getBot()), "WoW Admin Commands");
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("lookup");
        getBot().getCommandHandler().removeAlias("ilvl");
        getBot().getCommandHandler().removeAlias("mplusrank");
        getBot().getCommandHandler().removeAlias("raidrank");
        getBot().getCommandHandler().removeCommand("privatelookup");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = null;
        String region = null;
        try {
            //If we only received one name, split by dash to look for realm
            if (args.length == 1) {
                args = args[0].split("-");
            }
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
                    eb.setAuthor(titleBuilder.toString(), wowLink, WoWUtils.getClassIcon(className));


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
                    progressionBuilder.append(" - ");
                    progressionBuilder.append("**ABT**: ");
                    progressionBuilder.append(antorus.get("summary"));
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"progression"), progressionBuilder.toString(), false);


                    //We fetch the Battle.net achivement record
                    HttpUrl battleneturl = new HttpUrl.Builder().scheme("https")
                            .host(region + ".api.battle.net")
                            .addPathSegments("wow/character/"+serverSlug+"/" +args[0])
                            .addQueryParameter("fields", "achievements,stats")
                            .build();
                    Request battlenetRequest = new Request.Builder().url(battleneturl).build();
                    String battlenetResult = clientBattleNet.newCall(battlenetRequest).execute().body().string();
                    String apAmount = getAP(event.getGuild(), battlenetResult);

                    JSONObject gear = (JSONObject) jsonObject.get("gear");
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"ilvl"), gear.get("item_level_equipped") + "/" + gear.get("item_level_total"), true);

                    if (apAmount != null) {
                        eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"artifact.power"), gear.get("artifact_traits").toString() + " / " + apAmount + " " + getBot().getTranslateManager().translate(event.getGuild(),"ap.gathered"), true);
                    } else {
                        eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"artifact.power"), gear.get("artifact_traits").toString(), true);
                    }




                    JSONObject mplusRank = (JSONObject) jsonObject.get("mythic_plus_scores");
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"mythicplus.score"), mplusRank.get("all").toString(), true);
                    JSONObject lastMplusRank = (JSONObject) jsonObject.get("previous_mythic_plus_scores");
                    float lastSeasonScore = Float.parseFloat(lastMplusRank.get("all").toString());
                    float currentSeasonScore = Float.parseFloat(mplusRank.get("all").toString());
                    if (lastSeasonScore > currentSeasonScore || lastSeasonScore == 0) {
                        eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"last.season.mythicplus.score"), lastMplusRank.get("all").toString(), true);
                    } else {
                        //Field Unused. Let's put something else.
                        JSONObject battleNetJSON = (JSONObject) parser.parse(battlenetResult);
                        JSONObject statsJSON = (JSONObject) battleNetJSON.get("stats");
                        //TODO Translate this
                        StringBuilder statsBuilder = new StringBuilder();
                        long str = (long) statsJSON.get("str");
                        long agi = (long) statsJSON.get("agi");
                        long intel = (long) statsJSON.get("int");
                        if (str > agi && str > intel) {
                            statsBuilder.append("**STR**: ");
                            statsBuilder.append(str);
                        } else if (agi > str && agi > intel) {
                            statsBuilder.append("**AGI**: ");
                            statsBuilder.append(agi);
                        } else {
                            statsBuilder.append("**INT**: ");
                            statsBuilder.append(intel);
                        }
                        DecimalFormat decimalFormat = new DecimalFormat("00.##");
                        statsBuilder.append(" - ");
                        statsBuilder.append("**Crit**: ");
                        statsBuilder.append(decimalFormat.format(statsJSON.get("crit")));
                        statsBuilder.append("%");
                        statsBuilder.append(" - ");
                        statsBuilder.append("**Haste**: ");
                        statsBuilder.append(decimalFormat.format(statsJSON.get("haste")));
                        statsBuilder.append("%");
                        statsBuilder.append("\n");
                        statsBuilder.append("**Mastery**: ");
                        statsBuilder.append(decimalFormat.format(statsJSON.get("mastery")));
                        statsBuilder.append("%");
                        statsBuilder.append(" - ");
                        statsBuilder.append("**Vers**: ");
                        statsBuilder.append(decimalFormat.format(statsJSON.get("versatilityDamageDoneBonus")));
                        statsBuilder.append("%");

                        eb.addField("Stats", statsBuilder.toString(), true);
                    }



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
                            runsBuilder.append(getBot().getTranslateManager().translate(event.getGuild(), "hour.minutes.seconds", hours + "", minutes + "", seconds + ""));
                        } else {
                            runsBuilder.append(getBot().getTranslateManager().translate(event.getGuild(), "minutes.seconds", minutes + "", seconds + ""));
                        }
                        runsBuilder.append(" | ");
                        runsBuilder.append(run.get("num_keystone_upgrades").toString() + " ");
                        runsBuilder.append(getBot().getTranslateManager().translate(event.getGuild(), "mythicplus.chests"));
                        runsBuilder.append("\n");
                    }
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"best.mythicplus.runs"), runsBuilder.toString(),    true);
                    long m5 = getM5(battlenetResult);
                    long m10 = getM10(battlenetResult);
                    long m15 = getM15(battlenetResult);
                    StringBuilder completedBuilder = new StringBuilder();
                    completedBuilder.append("**M+5**: " + m5 + "\n");
                    completedBuilder.append("**M+10**: " + m10 + "\n");
                    completedBuilder.append("**M+15**: " + m15 + "\n");
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(), "mythicplus.completed"), completedBuilder.toString(), true);
                    eb.addField("WoWProgress", "[Click Here](https://www.wowprogress.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.addField("Raider.IO", "[Click Here](https://raider.io/characters/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.addField("WarcraftLogs","[Click Here](https://www.warcraftlogs.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+jsonObject.get("name") + ")", true);
                    eb.setFooter(getBot().getTranslateManager().translate(event.getGuild(),"information.taken.raider.io"),null);

                    if (getBot().getGuildSettings(event.getGuild()).getSetting(SETTING_PRIVATE_LOOKUP) != null) {
                        event.getAuthor().openPrivateChannel().complete().sendMessage(eb.build()).queue();
                    } else {
                        event.getChannel().sendMessage(eb.build()).queue();
                    }
                } else {

                    //We got an error from raider.io, maybe he was never added to the site. Let's try through battle.net
                    HttpUrl battleneturl = new HttpUrl.Builder().scheme("https")
                            .host(region + ".api.battle.net")
                            .addPathSegments("wow/character/"+serverSlug+"/" +args[0])
                            .addQueryParameter("fields", "progression,items,achievements")
                            .build();
                    Request battlenetRequest = new Request.Builder().url(battleneturl).build();
                    String battlenetResult = clientBattleNet.newCall(battlenetRequest).execute().body().string();

                    JSONObject battleNetObject = (JSONObject) parser.parse(battlenetResult);
                    if (battleNetObject.containsKey("status")) {
                        //Error
                        event.getChannel().sendMessage("Character not found. Did you make an error?").queue();
                        return;

                    }

                    StringBuilder titleBuilder = new StringBuilder();
                    titleBuilder.append(battleNetObject.get("name"));
                    titleBuilder.append(" ");
                    titleBuilder.append(battleNetObject.get("realm"));
                    titleBuilder.append(" - ");
                    titleBuilder.append(region.toUpperCase());
                    titleBuilder.append(" | ");
                    titleBuilder.append(HeroRace.values()[((Long) battleNetObject.get("race")).intValue()]);
                    titleBuilder.append(" ");
                    titleBuilder.append(HeroClass.values()[((Long) battleNetObject.get("class")).intValue()]);
                    titleBuilder.append(" ");
                    //event.getChannel().sendMessage(jsonObject.get("message").toString()).queue();

                    String wowLink = null;

                    if (region.equalsIgnoreCase("us")) {
                        wowLink = "https://worldofwarcraft.com/en-us/character/" + serverSlug + "/" + jsonObject.get("name");
                    } else {
                        wowLink = "https://worldofwarcraft.com/en-gb/character/" + serverSlug + "/" + jsonObject.get("name");
                    }

                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle(titleBuilder.toString(), wowLink);
                    eb.setThumbnail("http://render-" + region.toLowerCase() + ".worldofwarcraft.com/character/" + battleNetObject.get("thumbnail"));
                    String apAmount = getAP(event.getGuild(), battlenetResult);
                    JSONObject gear = (JSONObject) battleNetObject.get("items");
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"ilvl"), gear.get("averageItemLevelEquipped") + "/" + gear.get("averageItemLevel"), true);

                    if (apAmount != null) {
                        eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"artifact.power"), apAmount + " " + getBot().getTranslateManager().translate(event.getGuild(),"ap.gathered"), true);
                    }
                    long m5 = getM5(battlenetResult);
                    long m10 = getM10(battlenetResult);
                    long m15 = getM15(battlenetResult);
                    StringBuilder completedBuilder = new StringBuilder();
                    completedBuilder.append("**M+5**: " + m5 + "\n");
                    completedBuilder.append("**M+10**: " + m10 + "\n");
                    completedBuilder.append("**M+15**: " + m15 + "\n");
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(), "mythicplus.completed"), completedBuilder.toString(), false);
                    eb.addField("WoWProgress", "[Click Here](https://www.wowprogress.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+battleNetObject.get("name") + ")", true);
                    eb.addField("Raider.IO", "[Click Here](https://raider.io/characters/"+region.toLowerCase()+"/"+serverSlug+"/"+battleNetObject.get("name") + ")", true);
                    eb.addField("WarcraftLogs","[Click Here](https://www.warcraftlogs.com/character/"+region.toLowerCase()+"/"+serverSlug+"/"+battleNetObject.get("name") + ")", true);
                    eb.addField(getBot().getTranslateManager().translate(event.getGuild(),"information"), getBot().getTranslateManager().translate(event.getGuild(),"command.lookup.charnotfound"), false);
                    eb.setFooter(getBot().getTranslateManager().translate(event.getGuild(),"information.taken.battle.net"),null);

                    if (getBot().getGuildSettings(event.getGuild()).getSetting(SETTING_PRIVATE_LOOKUP) != null) {
                        event.getAuthor().openPrivateChannel().complete().sendMessage(eb.build()).queue();
                    } else {
                        event.getChannel().sendMessage(eb.build()).queue();
                    }

                    return;
                }
            }
        } catch (IOException e) {
            getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "error.occurred.try.again.later")).queue();
        } catch (ParseException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "serverName:" + serverName);
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "error.occurred.try.again.later")).queue();
        }

    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 99;
    }

    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild,"command.lookup.longhelp");
    }

    @Override
    public String shortDescription(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.lookup.shorthelp");
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "millions");
        suffixes.put(1_000_000_000L, "billions");
        suffixes.put(1_000_000_000_000L, "trillions");
        suffixes.put(1_000_000_000_000_000L, "quadrillions");
        suffixes.put(1_000_000_000_000_000_000L, "quintillions");
    }

    public String format(Guild guild, long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(guild,Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(guild, -value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = getBot().getTranslateManager().translate(guild,e.getValue().toLowerCase());

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + " " +  suffix : (truncated / 10) + " " + suffix;
    }

    public String getAP(Guild guild, String json) throws ParseException {
        long apAmount = -1;
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject battleNetCharacter = (JSONObject) parser.parse(json);
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
        String result = null;
        if (apAmount != -1) {
            result = format(guild, apAmount);
        }
        return result;

    }

    public long getM5(String json) throws ParseException {
        long m5 = 0;
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject battleNetCharacter = (JSONObject) parser.parse(json);
            JSONObject achivements = (JSONObject) battleNetCharacter.get("achievements");
            JSONArray criteriaObject = (JSONArray) achivements.get("criteria");
            int criteriaNumber = -1;
            for (int i = 0; i < criteriaObject.size(); i++) {
                if ((long)criteriaObject.get(i) == 33097) {
                    criteriaNumber = i;
                }
            }

            if (criteriaNumber != -1) {
                m5 = (long) ((JSONArray)achivements.get("criteriaQuantity")).get(criteriaNumber);
                if (m5 >= 1) {
                    m5 += 1;
                }
            }
        }
        return m5;
    }

    public long getM10(String json) throws ParseException {
        long m10 = 0;
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject battleNetCharacter = (JSONObject) parser.parse(json);
            JSONObject achivements = (JSONObject) battleNetCharacter.get("achievements");
            JSONArray criteriaObject = (JSONArray) achivements.get("criteria");
            int criteriaNumber = -1;
            for (int i = 0; i < criteriaObject.size(); i++) {
                if ((long)criteriaObject.get(i) == 33098) {
                    criteriaNumber = i;
                }
            }

            if (criteriaNumber != -1) {
                m10 = (long) ((JSONArray)achivements.get("criteriaQuantity")).get(criteriaNumber);
                if (m10 >= 1) {
                    m10 += 1;
                }
            }
        }
        return m10;
    }

    public long getM15(String json) throws ParseException {
        long m15 = 0;
        if (json != null) {
            JSONParser parser = new JSONParser();
            JSONObject battleNetCharacter = (JSONObject) parser.parse(json);
            JSONObject achivements = (JSONObject) battleNetCharacter.get("achievements");
            JSONArray criteriaObject = (JSONArray) achivements.get("criteria");
            int criteriaNumber = -1;
            for (int i = 0; i < criteriaObject.size(); i++) {
                if ((long)criteriaObject.get(i) == 32028    ) {
                    criteriaNumber = i;
                }
            }

            if (criteriaNumber != -1) {
                m15 = (long) ((JSONArray)achivements.get("criteriaQuantity")).get(criteriaNumber);
                if (m15 >= 1) {
                    m15 += 1;
                }
            }
        }
        return m15;
    }
}
