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

        Request request = new Request.Builder().url("https://www.wowprogress.com/guild/"+region+"/"+serverName+"/"+guild+"/json_rank").build();
        String result;
        try {
            result = client.newCall(request).execute().body().string();
            if (result.equals("null")) {
                event.getChannel().sendMessage("Guild not found on WowProgress!").queue();
                return;
            }

            try {
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(result);
                event.getChannel().sendMessage("Guild **" + guild + "** | World: **" + obj.get("world_rank") + "** | Region Rank: **" + obj.get("area_rank") + "** | Realm rank: **" + obj.get("realm_rank") + "**").queue();
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
        return "Retrieve the guild's rank on WowProgress";
    }

    @Override
    public String shortDescription() {
        return help();
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("wprank", this, "World of Warcraft");
        log.info("Command !wprank loaded!");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("wprank");
        log.info("Command !wprank unloaded!");
    }
}
