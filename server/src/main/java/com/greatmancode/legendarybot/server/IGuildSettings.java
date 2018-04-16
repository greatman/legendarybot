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

package com.greatmancode.legendarybot.server;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import net.dv8tion.jda.core.entities.Guild;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A representation of Discord Guild settings. Use a MySQL database to save the parameters
 */
public class IGuildSettings implements GuildSettings {

    /**
     * A instance of the bot.
     */
    private LegendaryBot bot;

    /**
     * The ID of the Guild
     */
    private String guildId;

    /**
     * A cache of the Guild settings
     */
    private Map<String, String> settings = new HashMap<>();

    private OkHttpClient client = new OkHttpClient.Builder().build();

    private static final MediaType TEXT = MediaType.parse("text/plain");

    /**
     * Create a {@link GuildSettings} instance
     * @param guild The Guild those settings are being linked to.
     * @param bot A {@link LegendaryBot} instance.
     */
    public IGuildSettings(Guild guild, LegendaryBot bot) {
        this.bot = bot;
        this.guildId = guild.getId();
    }


    @Override
    public String getWowServerName() {
        return settings.containsKey("WOW_SERVER_NAME") ? settings.get("WOW_SERVER_NAME") : getSetting("WOW_SERVER_NAME");

    }

    @Override
    public String getRegionName() {
        return settings.containsKey("WOW_REGION_NAME") ? settings.get("WOW_REGION_NAME") : getSetting("WOW_REGION_NAME");
    }

    @Override
    public String getGuildName() {
        return settings.containsKey("GUILD_NAME") ? settings.get("GUILD_NAME") : getSetting("GUILD_NAME");
    }

    @Override
    public String getSetting(String setting) {
        if (!settings.containsKey(setting)) {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("https")
                    .host(bot.getBotSettings().getProperty("api.host"))
                    .addPathSegments("api/guild/" + guildId + "/setting/" +setting)
                    .build();
            Request request = new Request.Builder().url(url).addHeader("x-api-key", bot.getBotSettings().getProperty("api.key")).build();
            try {
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    settings.put(setting, response.body().string());
                }
            } catch (IOException e) {
                e.printStackTrace();
                bot.getStacktraceHandler().sendStacktrace(e, "guildid:" + guildId, "setting:" + setting);
            }
        }
        return settings.get(setting);
    }

    @Override
    public void setSetting(String setting, String value) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(bot.getBotSettings().getProperty("api.host"))
                .addPathSegments("api/guild/" + guildId + "/setting/" +setting)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(TEXT,value))
                .addHeader("x-api-key", bot.getBotSettings().getProperty("api.key"))
                .build();
        try {
            client.newCall(request).execute();
            settings.put(setting,value);
        } catch (IOException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e, "guildid:" + guildId, "setting:" + setting);
        }
    }

    @Override
    public void unsetSetting(String setting) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host(bot.getBotSettings().getProperty("api.host"))
                .addPathSegments("api/guild/" + guildId + "/setting/" +setting)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .addHeader("x-api-key", bot.getBotSettings().getProperty("api.key"))
                .build();
        try {
            client.newCall(request).execute();
            settings.remove(setting);
        } catch (IOException e) {
            e.printStackTrace();
            bot.getStacktraceHandler().sendStacktrace(e, "guildid:" + guildId, "setting:" + setting);
        }
    }

    @Override
    public void resetCache() {
        settings.clear();
    }
}
