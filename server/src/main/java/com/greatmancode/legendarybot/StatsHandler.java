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

package com.greatmancode.legendarybot;

import com.greatmancode.legendarybot.api.LegendaryBot;
import net.dv8tion.jda.core.JDA;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Stats sender to the various bot lists
 */
public class StatsHandler  {
    private static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    /**
     * Scheduler to send stats at a specific interval
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Create a Stat Handler.
     * @param properties The app.properties file instance.
     * @param bot The {@link LegendaryBot} instance.
     */
    public StatsHandler(Properties properties, LegendaryBot bot) {
        JDA jda = bot.getJDA();

        final Runnable postStats = () -> {
            Map<String, String> map = new HashMap<>();
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.info("Sending stats");
            JSONObject object = new JSONObject();
            object.put("server_count", jda.getGuilds().size());
            Request request = new Request.Builder().url("https://bots.discord.pw/api/bots/267134720700186626/stats")
                    .post(RequestBody.create(MEDIA_TYPE_JSON, object.toJSONString()))
                    .addHeader("Authorization",properties.getProperty("stats.botsdiscordpw")).build();
            try {
                if (client.newCall(request).execute().isSuccessful()) {
                    System.out.println("Sent stats to Discord PW");
                } else {
                    System.out.println("Error while sending stats to Discord PW");
                }
                request = request.newBuilder().header("Authorization",properties.getProperty("stats.discordbotorg")).url("https://discordbots.org/api/bots/267134720700186626/stats").build();
                if (client.newCall(request).execute().isSuccessful()) {
                    System.out.println("Sent stats to Discordbots.org");
                } else {
                    System.out.println("Error while sending stats to Discordbots.org");
                }
            } catch (IOException e) {
                e.printStackTrace();
                bot.getStacktraceHandler().sendStacktrace(e);
            }
            logger.info("Stats sent.");
        };
        scheduler.scheduleAtFixedRate(postStats,0, 60, TimeUnit.MINUTES);
    }

    /**
     * Stop the Stats Handler.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

}
