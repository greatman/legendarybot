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
package com.greatmancode.legendarybot.plugin.streamers;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class StreamersPlugin extends LegendaryBotPlugin {

    private OkHttpClient client = new OkHttpClient();
    private Properties props;

    public static final String STATUS_KEY = "status";
    public static final String GAME_KEY = "game";
    public static final String CONFIG_KEY = "streamersPlugin";

    public StreamersPlugin(PluginWrapper wrapper) {
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
        getBot().getCommandHandler().addCommand("streamers", new StreamersCommand(this));
        log.info("Command !streamers loaded!");
        getBot().getCommandHandler().addCommand("addstreamer", new AddStreamerCommand(this));
        log.info("Command !addstreamer loaded!");
        getBot().getCommandHandler().addCommand("removestreamer", new RemoveStreamerCommand(this));
        log.info("Command !removestreamer unloaded!");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("streamers");
        log.info("Command !streamers unloaded!");
        getBot().getCommandHandler().removeCommand("addstreamer");
        log.info("Command !addstreamer unloaded!");
        getBot().getCommandHandler().removeCommand("removestreamer");
        log.info("Command !removestreamer unloaded!");
    }

    public Map<String, String> isStreaming(String username, StreamPlatform platform) {
        Map<String, String> map = new HashMap<>();
        JSONParser parser = new JSONParser();
        switch (platform) {
            case TWITCH:
                Request request = new Request.Builder()
                        .url("https://api.twitch.tv/kraken/streams/"+username)
                        .addHeader("Client-ID", props.getProperty("twitch.key"))
                        .build();
                try {
                    String result = client.newCall(request).execute().body().string();

                    JSONObject json = (JSONObject) parser.parse(result);
                    JSONObject stream = (JSONObject) json.get("stream");
                    if (stream != null) {
                        map.put(STATUS_KEY, (String) ((JSONObject)stream.get("channel")).get("status"));
                        map.put(GAME_KEY, (String) stream.get("game"));
                        map.put("created_at", (String) stream.get("created_at"));
                    }
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    getBot().getStacktraceHandler().sendStacktrace(e);
                }

                break;
            case MIXER:
                request = new Request.Builder().url("https://mixer.com/api/v1/channels/" + username).build();
                try {
                    String result = client.newCall(request).execute().body().string();
                    JSONObject json = (JSONObject) parser.parse(result);
                    if ((boolean)json.get("online")) {
                        JSONObject stream = (JSONObject) json.get("type");
                        map.put(STATUS_KEY, (String) json.get("name"));
                        map.put(GAME_KEY, (String) stream.get("name"));
                    }
                } catch (ParseException | IOException e) {
                    e.printStackTrace();
                    getBot().getStacktraceHandler().sendStacktrace(e);
                }
                break;
            default:
                break;
        }
        return map;
    }

    //TODO Improve this, duplicate code
    public boolean streamerExist(String username, StreamPlatform platform) {
        boolean result = false;
        switch (platform) {
            case TWITCH:
                Request request = new Request.Builder()
                        .url("https://api.twitch.tv/kraken/channels/"+username)
                        .addHeader("Client-ID", props.getProperty("twitch.key"))
                        .build();
                try {
                    result = client.newCall(request).execute().body().string() != null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case MIXER:
                request = new Request.Builder()
                        .url("https://mixer.com/api/v1/channels/" + username)
                        .build();
                try {
                    result = client.newCall(request).execute().body().string() != null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;

        }
        return result;
    }

    public void addStreamer(Guild guild, String username, StreamPlatform platform) {
        String settings = getBot().getGuildSettings(guild).getSetting(CONFIG_KEY);
        if (settings != null) {
            if (!settings.contains(username + "," + platform)) {
                settings += ";" + username + "," + platform;
            }
        } else {
            settings = username + "," + platform;
        }
        getBot().getGuildSettings(guild).setSetting(CONFIG_KEY,settings);
    }

    public void removeStreamer(Guild guild, String username, StreamPlatform platform) {
        String settings = getBot().getGuildSettings(guild).getSetting(CONFIG_KEY);
        if (settings != null && settings.contains(username + "," + platform)) {
            settings = settings.replaceAll(username + "," + platform, "").replaceAll(";;", ";");
            getBot().getGuildSettings(guild).setSetting(CONFIG_KEY, settings);
        }
    }
}
