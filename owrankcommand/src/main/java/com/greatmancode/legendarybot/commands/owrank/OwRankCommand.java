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

package com.greatmancode.legendarybot.commands.owrank;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.server.WoWGuild;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;

/**
 * !owrank command - Get the competitive ranking of a player in Overwatch
 */
public class OwRankCommand extends LegendaryBotPlugin implements PublicCommand {

    /**
     * The Http Client to do web requests.
     */
    private OkHttpClient client = new OkHttpClient();

    public OwRankCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("owrank", this);
        log.info("Command !owrank loaded");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("owrank");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String user = args[0].substring(0, 1).toUpperCase() + args[0].substring(1);
        new Thread(() -> {
            Request webRequest = new Request.Builder().url("https://owapi.net/api/v3/u/"+user+"/stats").build();

            try {
                String output = client.newCall(webRequest).execute().body().string();
                if (output == null) {
                    MessageBuilder builder = new MessageBuilder();
                    builder.append("User ").append(args[0]).append(" not found!");
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(output);
                if (json.containsKey("error")) {
                    MessageBuilder builder = new MessageBuilder();
                    builder.append("User ").append(args[0]).append(" not found!");
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                WoWGuild guild = getBot().getWowGuildManager(event.getGuild()).getDefaultGuild();
                String region = guild.getRegion().toLowerCase();
                if (json.get(region) == null) {
                    MessageBuilder builder = new MessageBuilder();
                    builder.append("User ").append(args[0]).append(" doesn't play competitive!");
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }
                JSONObject competitive = (JSONObject) ((JSONObject)((JSONObject)json.get(region)).get("stats")).get("competitive");
                if (competitive == null) {
                    MessageBuilder builder = new MessageBuilder();
                    builder.append("User ").append(args[0]).append(" doesn't play competitive!");
                    event.getChannel().sendMessage(builder.build()).queue();
                    return;
                }

                JSONObject competitiveStats = (JSONObject) competitive.get("overall_stats");
                MessageBuilder builder = new MessageBuilder();
                builder.append("Player ")
                        .append(args[0])
                        .append(" | Rank: ")
                        .append(competitiveStats.get("tier"))
                        .append(" (")
                        .append(competitiveStats.get("comprank"))
                        .append(") | Wins: ")
                        .append(competitiveStats.get("wins"))
                        .append(" | Losses: ")
                        .append(competitiveStats.get("losses"))
                        .append(" | Ties: ")
                        .append(competitiveStats.get("ties"))
                        .append(" | Win Rate: ")
                        .append(competitiveStats.get("win_rate"))
                        .append("%");
                event.getChannel().sendMessage(builder.build()).queue();
            } catch (IOException | ParseException | NullPointerException e) {
                e.printStackTrace();
                getBot().getStacktraceHandler().sendStacktrace(e, "user:" + user);
            }
        }).start();
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "owrank [BattleTag] - Get a player's Overwatch competitive rank. The BattleTag format is Username-numbers. Example: Greatman-1189.";
    }
}
