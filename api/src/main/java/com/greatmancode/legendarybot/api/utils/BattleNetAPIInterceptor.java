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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.greatmancode.legendarybot.api.LegendaryBot;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.influxdb.dto.Point;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * OKHttp interceptor to do Battle.Net queries
 */
public class BattleNetAPIInterceptor implements Interceptor {

    /**
     * List containing all available battle.net API keys
     */
    private String usKey;
    private String usSecret;
    private String euKey;
    private String euSecret;
    private static OAuth2AccessToken usToken;
    private static OAuth20Service usService;
    private static long usTokenExpire;
    private static OAuth20Service euService;
    private static OAuth2AccessToken euToken;
    private static long euTokenExpire;

    /**
     * An instance of the bot.
     */
    private LegendaryBot bot;

    /**
     * Build an instance of the Interceptor
     * @param bot A instance of the bot.
     */
    public BattleNetAPIInterceptor(LegendaryBot bot) {
        this.bot = bot;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
            usKey = props.getProperty("battlenet.us.key");
            usSecret = props.getProperty("battlenet.us.secret");
            euKey = props.getProperty("battlenet.eu.key");
            euSecret = props.getProperty("battlenet.eu.secret");
            if (usKey != null && usSecret != null && usService == null) {
                usService = new ServiceBuilder(usKey)
                        .apiSecret(usSecret)
                        .build(new OAuthBattleNetApi("us"));
                usToken = usService.getAccessTokenClientCredentialsGrant();
                usTokenExpire = System.currentTimeMillis() + (usToken.getExpiresIn() * 1000);
            }

            if (euKey != null && euSecret != null && euService == null) {
                euService = new ServiceBuilder(euKey)
                        .apiSecret(euSecret)
                        .build(new OAuthBattleNetApi("eu"));
                euToken = euService.getAccessTokenClientCredentialsGrant();
                euTokenExpire = System.currentTimeMillis() + (euToken.getExpiresIn() * 1000);
            }

        } catch (IOException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //TODO support multiple locale
        bot.getStatsClient().write(Point.measurement("legendarybot")
        .addField("battlenet",1)
        .build());
        HttpUrl url = null;
        if (chain.request().url().host().equals("us.api.battle.net") && chain.request().url().encodedPath().contains("/data/")) {
            //Data mode US
            try {
                refreshToken();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            url = chain.request().url().newBuilder()
                    .addQueryParameter("locale", "en_US")
                    .addQueryParameter("access_token", usToken.getAccessToken())
                    .build();

        } else if (chain.request().url().host().equals("eu.api.battle.net") && chain.request().url().encodedPath().contains("/data/")) {
            //Data mode EU
            try {
                refreshToken();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            url = chain.request().url().newBuilder()
                    .addQueryParameter("locale", "en_US")
                    .addQueryParameter("access_token", euToken.getAccessToken())
                    .build();
        } else if (chain.request().url().host().equals("us.api.battle.net")) {
            url = chain.request().url().newBuilder()
                    .addQueryParameter("apikey", usKey)
                    .addQueryParameter("locale", "en_US")
                    .build();
        } else if (chain.request().url().host().equals("eu.api.battle.net")) {
            url = chain.request().url().newBuilder()
                    .addQueryParameter("apikey", euKey)
                    .addQueryParameter("locale", "en_US")
                    .build();
        }


        if (url == null) {
            //Should not happen. Find why
            try {
                throw new InvalidURLException();
            } catch ( InvalidURLException e) {
                bot.getStacktraceHandler().sendStacktrace(e, "originalUrl:" + chain.request().url().toString());
            }

       }
       Request request = chain.request().newBuilder().url(url).build();
       Response response = chain.proceed(request);
       return response;
    }

    private void refreshToken() throws InterruptedException, ExecutionException, IOException {
        if (usToken == null) {
            usToken = usService.getAccessTokenClientCredentialsGrant();
        } else {
            //We do a time check.
            if (System.currentTimeMillis() > usTokenExpire) {
                usToken = usService.getAccessTokenClientCredentialsGrant();
            }
        }
        if (euToken == null) {
            euToken = euService.getAccessTokenClientCredentialsGrant();
        } else {
            //We do a time check.
            if (System.currentTimeMillis() > euTokenExpire) {
                euToken = euService.getAccessTokenClientCredentialsGrant();
            }
        }
    }
}
