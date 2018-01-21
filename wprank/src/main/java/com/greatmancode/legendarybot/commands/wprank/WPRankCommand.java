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

package com.greatmancode.legendarybot.commands.wprank;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.IOException;

/**
 * !wprank Command - Gives the WoW Progress rank of the Guild.
 */
public class WPRankCommand extends LegendaryBotPlugin implements PublicCommand {

    /**
     * The HTTP Client
     */
    private OkHttpClient client = new OkHttpClient();

    public WPRankCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
        String region = getBot().getGuildSettings(event.getGuild()).getRegionName();
        String guild = getBot().getGuildSettings(event.getGuild()).getGuildName();
        if (serverName == null || region == null || guild == null) {
            event.getChannel().sendMessage("The server name, the region and the guild must be configured for this command to work!").queue();
            return;
        }

        //https://raider.io/api/v1/guilds/profile
        HttpUrl url = new HttpUrl.Builder()
                .host("raider.io")
                .scheme("https")
                .addPathSegments("api/v1/guilds/profile")
                .addQueryParameter("region", region)
                .addQueryParameter("realm", serverName)
                .addQueryParameter("name", guild)
                .addQueryParameter("fields", "raid_rankings")
                .build();
        Request request = new Request.Builder().url(url).build();
        String result;
        try {
            result = client.newCall(request).execute().body().string();
            if ("null".equals(result)) {
                event.getChannel().sendMessage("Guild not found on WowProgress!").queue();
                return;
            }

            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(result);

                if (obj.containsKey("error")) {
                    event.getChannel().sendMessage("Guild not found on Raider.IO!").queue();
                    return;
                }
                String realm = (String) obj.get("realm");
                JSONObject raidRankings = (JSONObject) obj.get("raid_rankings");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(guild + "-" + realm + " Raid Rankings");
                JSONObject theNighthold = (JSONObject) raidRankings.get("the-nighthold");
                JSONObject theEmeraldNightmare = (JSONObject) raidRankings.get("the-emerald-nightmare");
                JSONObject trialOfValor = (JSONObject) raidRankings.get("trial-of-valor");
                JSONObject tombOfSargeras = (JSONObject) raidRankings.get("tomb-of-sargeras");
                JSONObject antorusTheBurningThrone = (JSONObject) raidRankings.get("antorus-the-burning-throne");
                eb.addField("Antorus The Burning Throne", formatRanking(antorusTheBurningThrone), true);
                eb.addField("Tomb of Sargeras", formatRanking(tombOfSargeras), true);
                eb.addField("The Nighthold", formatRanking(theNighthold), true);
                eb.addField("Trial of Valor", formatRanking(trialOfValor), true);
                eb.addField("The Emerald Nightmare", formatRanking(theEmeraldNightmare), true);

                event.getChannel().sendMessage(eb.build()).queue();
            } catch (ParseException e) {
                e.printStackTrace();
                getBot().getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(),"servername:" + serverName, "region:" + region, "wowguild:" + guild);
            }
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(),"servername:" + serverName, "region:" + region, "wowguild:" + guild);
        }


    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 0;
    }

    @Override
    public String help() {
        return "Retrieve the guild's rank on Raider.IO";
    }

    @Override
    public String shortDescription() {
        return help();
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("guildrank", this, "World of Warcraft");
        getBot().getCommandHandler().addAlias("wprank","guildrank");
        log.info("Command !guildrank loaded!");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeAlias("wprank");
        getBot().getCommandHandler().removeCommand("guildrank");
        log.info("Command !guildrank unloaded!");
    }

    private String formatRanking(JSONObject json) {
        JSONObject normal = (JSONObject) json.get("normal");
        JSONObject heoric = (JSONObject) json.get("heroic");
        JSONObject mythic = (JSONObject) json.get("mythic");
        StringBuilder builder = new StringBuilder();
        if ((long)normal.get("world") != 0 && (long)heoric.get("world") == 0 && (long) mythic.get("world") == 0) {
            builder.append("**Normal**\n");
            subFormatRanking(normal, builder);
        } else if ((long)heoric.get("world") != 0 && (long) mythic.get("world") == 0) {
            builder.append("\n**Heroic**\n");
            subFormatRanking(heoric,builder);
        } else if ((long)mythic.get("world") != 0){
            builder.append("\n**Mythic**\n");
            subFormatRanking(mythic, builder);
        }






        return builder.toString();
    }

    private void subFormatRanking(JSONObject difficulty, StringBuilder builder) {
        if ((long)difficulty.get("world") != 0) {
            builder.append("World: **" + difficulty.get("world") + "**\n");
            builder.append("Region: **" + difficulty.get("region") + "**\n");
            builder.append("Realm: **" + difficulty.get("realm") + "**\n");
        } else {
            builder.append("**Not started**\n");
        }

    }
}
