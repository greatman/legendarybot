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
package com.greatmancode.legendarybot.plugin.blizzazrdcscommand;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.Utils;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class BlizzardCSCommand extends LegendaryBotPlugin implements ZeroArgsCommand, PublicCommand{

    private Properties props;

    public BlizzardCSCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        getBot().getCommandHandler().addCommand("blizzardcs", this);
        log.info("Command !blizzardcs loaded!");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("blizzardcs");
        log.info("Command !blizzardcs unloaded!");
    }

    public String getLastTweet(String username) {
        String result = "";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        byte[] key = Base64.getEncoder().encode((props.getProperty("twitter.key") + ":" + props.getProperty("twitter.secret")).getBytes());
        headers.put("Authorization", "Basic " + new String(key));
        String auth = Utils.doRequest("https://api.twitter.com/oauth2/token","POST","grant_type=client_credentials", headers);
        try {
            org.json.simple.JSONObject authObject = (org.json.simple.JSONObject) Utils.jsonParser.parse(auth);
            if (authObject.containsKey("token_type")) {
                String bearer = (String) authObject.get("access_token");
                headers.clear();
                headers.put("Authorization", "Bearer " + bearer);
                String twitterTimeline = Utils.doRequest("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name="+ username +"&exclude_replies=1&count=100", headers);
                JSONArray twitterObject = (JSONArray) Utils.jsonParser.parse(twitterTimeline);
                JSONObject messageObject = (JSONObject) twitterObject.get(0);

                Date date = getTwitterDate((String)messageObject.get("created_at"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                result = cal.get(Calendar.DAY_OF_MONTH) +"/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) + " " + String.format("%02d",cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d",cal.get(Calendar.MINUTE)) + " : " + messageObject.get("text");
            }
        } catch (ParseException | java.text.ParseException e) {
            LegendaryBot.getInstance().getStacktraceHandler().sendStacktrace(e);
            e.printStackTrace();
        }
        return result;
    }
    public Date getTwitterDate(String date) throws java.text.ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER,Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }


    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        event.getChannel().sendMessage(getLastTweet("blizzardcs")).queue();
    }

    @Override
    public String help() {
        return "blizzardcs - Get the last tweet of Blizzardcs US";
    }
}
