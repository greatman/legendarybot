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
package com.greatmancode.legendarybot.api.utils;

import com.greatmancode.legendarybot.api.LegendaryBot;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BattleNetAPIInterceptor implements Interceptor {

    private final List<String> battleNetKey = new ArrayList<>();
    private LegendaryBot bot;
    public BattleNetAPIInterceptor(LegendaryBot bot) {
        this.bot = bot;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
            battleNetKey.add(props.getProperty("battlenet.key"));
            for (int i = 2; i<= 10; i++) {
                if (props.containsKey("battlenet"+i+".key")) {
                    battleNetKey.add(props.getProperty("battlenet"+i+".key"));
                }
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e);
        }
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        bot.getStatsClient().incrementCounter("legendarybot.battlenet.query");
        int keyid = 0;
        HttpUrl url = chain.request().url().newBuilder()
                .addQueryParameter("apikey", battleNetKey.get(0))
                .addQueryParameter("locale", "en_US")
                .build();
        Request request = chain.request().newBuilder().url(url).build();
        Response  response = chain.proceed(request);
        while (response.code() == 403) {
            keyid++;
            if (keyid >= battleNetKey.size()) {
                return response;
            }
            url = chain.request().url().newBuilder()
                    .addQueryParameter("apikey", battleNetKey.get(keyid))
                    .build();
            request = chain.request().newBuilder().url(url).build();
            response = chain.proceed(request);
        }
        return response;
    }
}
