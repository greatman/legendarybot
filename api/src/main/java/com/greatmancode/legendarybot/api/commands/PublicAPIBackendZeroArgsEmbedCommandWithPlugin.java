package com.greatmancode.legendarybot.api.commands;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.DiscordEmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public abstract class PublicAPIBackendZeroArgsEmbedCommandWithPlugin extends LegendaryBotPlugin implements PublicCommand,ZeroArgsCommand {

    private Properties props;

    public PublicAPIBackendZeroArgsEmbedCommandWithPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
    }


    public void executeAPICall(MessageChannel channel, String url) {
        executeAPICall(channel, url, null);
    }

    public void executeAPICall(MessageChannel channel, String url, Map<String,String> queryParameters) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        HttpUrl.Builder httpurl = new HttpUrl.Builder().scheme("https")
                .host(props.getProperty("api.host"))
                .addPathSegments(url);
        if (queryParameters != null) {
            queryParameters.forEach(httpurl::addQueryParameter);
        }
        Request request = new Request.Builder().url(httpurl.build()).build();
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
