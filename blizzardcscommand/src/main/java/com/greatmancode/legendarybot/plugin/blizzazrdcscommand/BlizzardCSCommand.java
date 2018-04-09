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

package com.greatmancode.legendarybot.plugin.blizzazrdcscommand;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.DiscordEmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Command that gives the latest tweet of the @blizzardcs account.
 */
public class BlizzardCSCommand extends LegendaryBotPlugin implements ZeroArgsCommand, PublicCommand{

    /**
     * Instance of the HTTP Client
     */
    private OkHttpClient client = new OkHttpClient();

    /**
     * A instance of the bot's configuration file
     */
    private Properties props;


    public BlizzardCSCommand(PluginWrapper wrapper) {
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
        getBot().getCommandHandler().addCommand("blizzardcs", this, "General Commands");
        log.info("Command !blizzardcs loaded!");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("blizzardcs");
        log.info("Command !blizzardcs unloaded!");
    }


    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String region = getBot().getGuildSettings(event.getGuild()).getRegionName();
        if (region != null) {
            OkHttpClient client = new OkHttpClient.Builder().build();
            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host(props.getProperty("api.host"))
                    .addPathSegments("api/twitter/"+region)
                    .build();
            Request request = new Request.Builder().url(url).build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    event.getChannel().sendMessage(DiscordEmbedBuilder.convertJsonToMessageEmbed(response.body().string())).queue();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.blizzardcs.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
