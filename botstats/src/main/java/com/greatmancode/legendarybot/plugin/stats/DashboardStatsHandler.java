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
package com.greatmancode.legendarybot.plugin.stats;

import com.greatmancode.legendarybot.api.LegendaryBot;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handler to send stats do the StatsClient (DataDog)
 */
public class DashboardStatsHandler {

    /**
     * The OKHttp client
     */
    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    /**
     * Scheduler to send stats at a specific interval
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public DashboardStatsHandler(StatsPlugin plugin) {
        LegendaryBot bot = plugin.getBot();
        final Runnable postStats = () -> {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name","Total Servers");
            jsonObject.put("value", plugin.getGuildCount());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name","Text Channels");
            jsonObject.put("value", plugin.getTextChannelCount());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name","voice Channels");
            jsonObject.put("value",plugin.getVoiceChannelCount());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "Member Count");
            jsonObject.put("value", plugin.getMemberCount());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "Music Sound Queue");
            jsonObject.put("value", plugin.getSongQueue());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "Audio Connections");
            jsonObject.put("value", plugin.getAudioConnections());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "Used RAM");
            jsonObject.put("value", plugin.getUsedRam());
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("name", "Ping");
            jsonObject.put("value", bot.getJDA().get(0).getPing());
            jsonArray.put(jsonObject);

            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host(plugin.getBot().getBotSettings().getProperty("api.host"))
                    .addPathSegments("api/stats")
                    .build();
            Request request = new Request.Builder().url(url).addHeader("x-api-key", bot.getBotSettings().getProperty("api.key")).post(RequestBody.create(StatsPlugin.MEDIA_TYPE_JSON,jsonArray.toString())).build();
            try {
                client.newCall(request).execute().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(postStats,0, 1, TimeUnit.MINUTES);
    }

    /**
     * Stop the Stats Handler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }
}
