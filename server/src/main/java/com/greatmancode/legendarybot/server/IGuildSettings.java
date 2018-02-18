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
import com.mongodb.client.MongoCollection;
import com.mongodb.*;
import com.mongodb.client.model.UpdateOptions;
import net.dv8tion.jda.core.entities.Guild;
import org.bson.Document;

import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

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

    private static final String MONGO_COLLECTION_NAME = "guild";

    /**
     * Create a {@link GuildSettings} instance
     * @param guild The Guild those settings are being linked to.
     * @param bot A {@link LegendaryBot} instance.
     */
    public IGuildSettings(Guild guild, LegendaryBot bot) {
        this.bot = bot;
        this.guildId = guild.getId();
        MongoCollection<Document> collection = bot.getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        collection.find(eq("guild_id",guildId)).forEach((Block<Document>) document -> {
            if (((Document)document.get("settings")) != null) {
                ((Document)document.get("settings")).forEach((k, v) -> settings.put(k, (String) v));
            }

        });
    }


    @Override
    public String getWowServerName() {
        return settings.get("WOW_SERVER_NAME");
    }

    @Override
    public String getRegionName() {
        return settings.get("WOW_REGION_NAME");
    }

    @Override
    public String getGuildName() {
        return settings.get("GUILD_NAME");
    }

    @Override
    public String getSetting(String setting) {
        return settings.get(setting);
    }

    @Override
    public void setSetting(String setting, String value) {
        MongoCollection<Document> collection = bot.getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        collection.updateOne(eq("guild_id", guildId),set("settings." + setting, value), new UpdateOptions().upsert(true));
        settings.put(setting,value);
    }

    @Override
    public void unsetSetting(String setting) {
        MongoCollection<Document> collection = bot.getMongoDatabase().getCollection(MONGO_COLLECTION_NAME);
        collection.updateOne(eq("guild_id", guildId),unset("settings." + setting));
        settings.remove(setting);
    }
}
