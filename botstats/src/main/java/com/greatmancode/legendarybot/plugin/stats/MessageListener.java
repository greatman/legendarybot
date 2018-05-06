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

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageListener extends ListenerAdapter {

    /**
     * The OKHttp client
     */
    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private int messageCount = 0;
    /**
     * Create a Message Listener
     * @param plugin The {@link StatsPlugin} instance this Message Listener is linked to.
     */
    public MessageListener(StatsPlugin plugin) {
        System.out.println("Loading MessageListener");
        final Runnable postStats = () -> {
            System.out.println("SENDING MESSAGE STATS");
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name","Message Count");
            jsonObject.put("value", messageCount);
            jsonArray.put(jsonObject);
            messageCount = 0;
            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host(plugin.getBot().getBotSettings().getProperty("api.host"))
                    .addPathSegments("api/stats")
                    .build();
            Request request = new Request.Builder().url(url).addHeader("x-api-key", plugin.getBot().getBotSettings().getProperty("api.key")).post(RequestBody.create(StatsPlugin.MEDIA_TYPE_JSON,jsonArray.toString())).build();
            try {
                client.newCall(request).execute().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        scheduler.scheduleAtFixedRate(postStats,0, 1, TimeUnit.MINUTES);
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        messageCount++;
    }

    /**
     * Stop the Stats Handler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

}
