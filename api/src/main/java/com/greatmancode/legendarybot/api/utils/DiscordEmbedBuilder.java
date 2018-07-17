package com.greatmancode.legendarybot.api.utils;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;

public class DiscordEmbedBuilder {

    public static MessageEmbed convertJsonToMessageEmbed(String json) {
        JSONObject jsonObject = new JSONObject(json);
        EmbedBuilder builder = new EmbedBuilder();
        if (jsonObject.has("thumbnail")) {
            builder.setThumbnail(jsonObject.getJSONObject("thumbnail").getString("url"));
        }
        if (jsonObject.has("color")) {
            builder.setColor(new Color(jsonObject.getInt("color")));
        }
        if (jsonObject.has("footer")) {
            JSONObject footer = jsonObject.getJSONObject("footer");
            if (footer.has("text") && footer.has("icon_url")) {
                builder.setFooter(footer.getString("text"), footer.getString("icon_url"));
            } else {
                builder.setFooter(footer.getString("text"), null);
            }

        }
        if (jsonObject.has("author")) {
            JSONObject author = jsonObject.getJSONObject("author");
            if (author.has("name") && author.has("url") && author.has("icon_url")) {
                builder.setAuthor(author.getString("name"),author.getString("url"), author.getString("icon_url"));
            } else if (author.has("name") && author.has("url")) {
                builder.setAuthor(author.getString("name"),author.getString("url"));
            } else {
                builder.setAuthor(author.getString("name"));
            }

        }
        if (jsonObject.has("fields")) {
            JSONArray fields = jsonObject.getJSONArray("fields");
            fields.forEach(fieldEntry -> {
                JSONObject field = (JSONObject) fieldEntry;
                builder.addField(field.getString("name"),field.getString("value"), field.getBoolean("inline"));
            });
        }
        if (jsonObject.has("timestamp")) {
            builder.setTimestamp(OffsetDateTime.parse(jsonObject.getString("timestamp")));
        }
        if (jsonObject.has("image")) {
            builder.setImage(jsonObject.getJSONObject("image").getString("url"));
        }

        if (jsonObject.has("description")) {
            builder.setDescription(jsonObject.getString("description"));
        }
        if (jsonObject.has("title")) {
            if (jsonObject.has("url")) {
                builder.setTitle(jsonObject.getString("title"), jsonObject.getString("url"));
            } else {
                builder.setTitle(jsonObject.getString("title"));
            }
        }
        return builder.build();
    }
}
