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

package com.greatmancode.legendarybot.commands.token;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.awt.*;
import java.io.IOException;

/**
 * !token Command - Get the price in Gold of the WoW Token.
 */
public class TokenCommand extends LegendaryBotPlugin implements ZeroArgsCommand,PublicCommand {

    /**
     * The HTTP Client to do web requests.
     */
    private OkHttpClient client = new OkHttpClient();

    public TokenCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        Request webRequest = new Request.Builder().url("https://data.wowtoken.info/snapshot.json").build();


        try {
            String request = client.newCall(webRequest).execute().body().string();
            if (request == null) {
                event.getChannel().sendMessage("An error occured. Please try again later!").queue();
                return;
            }
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(request);
            String region = getBot().getGuildSettings(event.getGuild()).getRegionName();
            if (region == null) {
                event.getChannel().sendMessage("The owner of the server needs to configure the region. Example: !setserversetting WOW_REGION_NAME US").queue();
                return;
            }
            if (getBot().getGuildSettings(event.getGuild()).getRegionName().equals("US")) {
                region = "NA";
            }
            JSONObject naserver = (JSONObject) object.get(region);
            if (naserver == null) {
                event.getChannel().sendMessage("The region isn't set properly or WowToken is having issues. Please use !setserversetting WOW_REGION_NAME US/EU .").queue();
                return;
            }
            JSONObject prices = (JSONObject) naserver.get("formatted");
            String price = (String) prices.get("buy");
            String minPrice = (String) prices.get("24min");
            String maxPrice = (String) prices.get("24max");
            double pctPrice = Double.parseDouble(prices.get("24pct").toString());
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Price for 1 WoW Token in the " + region + " region");
            eb.setThumbnail("http://wow.zamimg.com/images/wow/icons/large/wow_token01.jpg");
            eb.setColor(new Color(255,215,0));
            eb.addField("Current Price", price, true);
            eb.addField("Minimum 24H", minPrice, true);
            eb.addField("Maximum 24H", maxPrice, true);
            eb.addField("Percentage 24H range", pctPrice + "", true);
            eb.setFooter("Information taken from https://wowtoken.info/", "http://wow.zamimg.com/images/wow/icons/large/wow_token01.jpg");
            event.getChannel().sendMessage(eb.build()).queue();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "regionName:" + getBot().getGuildSettings(event.getGuild()).getRegionName());
            event.getChannel().sendMessage("An error occured. Try again later!").queue();
        }
    }

    @Override
    public String help() {
        return "token - Return the WoW token price";
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("token", this);
        log.info("Command !token loaded.");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("token");
        log.info("Command !token unloaded");
    }
}
