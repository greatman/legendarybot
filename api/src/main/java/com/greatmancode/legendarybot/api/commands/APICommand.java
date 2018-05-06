package com.greatmancode.legendarybot.api.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.utils.DiscordEmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.Map;

public interface APICommand {

    default void executeAPICall(String apiHost, LegendaryBot bot, Guild guild, MessageChannel channel, String url) {
        executeAPICall(apiHost, null, bot, guild, channel, url, null);
    }
    default void executeAPICall(String apiHost, String apiKey, LegendaryBot bot, Guild guild, MessageChannel channel, String url) {
        executeAPICall(apiHost, apiKey, bot, guild, channel, url, null);
    }

    default void executeAPICall(String apiHost, String apiKey, LegendaryBot bot, Guild guild, MessageChannel channel, String url, Map<String,String> queryParameters) {
        if (url.contains("{guild}")) {
            url = url.replace("{guild}", guild.getId());
        }
        if (url.contains("{region}")) {
            url = url.replace("{region}", bot.getGuildSettings(guild).getRegionName());
        }
        if (url.contains("{realm}")) {
            url = url.replace("{realm}", bot.getGuildSettings(guild).getWowServerName());
        }
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl.Builder httpurl = new HttpUrl.Builder().scheme("https")
                .host(apiHost)
                .addPathSegments(url);
        if (queryParameters != null) {
            queryParameters.forEach(httpurl::addQueryParameter);
        }
        Request.Builder requestBuilder = new Request.Builder().url(httpurl.build());
        if (apiKey != null) {
            requestBuilder.addHeader("x-api-key", apiKey);
        }
        Request request = requestBuilder.build();
        try {
            okhttp3.Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                channel.sendMessage(DiscordEmbedBuilder.convertJsonToMessageEmbed(response.body().string())).queue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
