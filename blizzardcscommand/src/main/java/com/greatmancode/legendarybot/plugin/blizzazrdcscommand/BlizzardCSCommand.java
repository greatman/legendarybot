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

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Command that gives the latest tweet of the @blizzardcs account.
 */
public class BlizzardCSCommand extends LegendaryBotPlugin implements ZeroArgsCommand, PublicCommand{

    /**
     * Instance of the HTTP Client
     */
    private OkHttpClient client = new OkHttpClient();

    /**
     * A instance of the bot's configuration file
     */
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
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        getBot().getCommandHandler().addCommand("blizzardcs", this);
        log.info("Command !blizzardcs loaded!");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("blizzardcs");
        log.info("Command !blizzardcs unloaded!");
    }

    /**
     * Retrieve the latest tweet of a username that is not a mention
     * @param username The twitter username to look
     * @return The latest tweet of the user.
     */
    public String getLastTweet(String username) {
        String result = "";
        byte[] key = Base64.getEncoder().encode((props.getProperty("twitter.key") + ":" + props.getProperty("twitter.secret")).getBytes());
        Request request = new Request.Builder()
                .url("https://api.twitter.com/oauth2/token")
                .post(RequestBody.create(MediaType.parse( "application/x-www-form-urlencoded;charset=UTF-8"), "grant_type=client_credentials"))
                .addHeader("Authorization","Basic " + new String(key))
                .build();

        try {
            String auth = client.newCall(request).execute().body().string();
            JSONParser parser = new JSONParser();
            org.json.simple.JSONObject authObject = (org.json.simple.JSONObject) parser.parse(auth);
            if (authObject.containsKey("token_type")) {
                String bearer = (String) authObject.get("access_token");
                HttpUrl url = new HttpUrl.Builder().scheme("https")
                        .host("api.twitter.com")
                        .addPathSegments("1.1/statuses/user_timeline.json")
                        .addQueryParameter("screen_name", username)
                        .addQueryParameter("exclude_replies", "1")
                        .addQueryParameter("count", "100")
                        .build();
                request = new Request.Builder().url(url).addHeader("Authorization","Bearer " + bearer).build();
                String twitterTimeline = client.newCall(request).execute().body().string();
                JSONArray twitterObject = (JSONArray) parser.parse(twitterTimeline);
                JSONObject messageObject = (JSONObject) twitterObject.get(0);

                Date date = getTwitterDate((String)messageObject.get("created_at"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.setTimeZone(TimeZone.getTimeZone("America/Montreal"));
                result = cal.get(Calendar.DAY_OF_MONTH) +"/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) + " " + String.format("%02d",cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d",cal.get(Calendar.MINUTE)) + " : " + messageObject.get("text");
            }
        } catch (ParseException | java.text.ParseException e) {
            getBot().getStacktraceHandler().sendStacktrace(e, "twitterusername:" + username);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "twitterusername:" + username);
        }
        return result;
    }

    /**
     * Convert a Twitter formatted date to a Java format
     * @param date The twitter date
     * @return a {@link Date} instance of the twitter date
     * @throws java.text.ParseException If we can't parse the date
     */
    public Date getTwitterDate(String date) throws java.text.ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER,Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }


    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        //TODO Support EU.
        event.getChannel().sendMessage(getLastTweet("blizzardcs")).queue();
    }

    @Override
    public String help() {
        return "blizzardcs - Get the last tweet of Blizzardcs US";
    }
}
