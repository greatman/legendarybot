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
package com.greatmancode.legendarybot.commands.log;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

/**
 * The !log command
 */
public class LogCommand extends LegendaryBotPlugin implements ZeroArgsCommand, PublicCommand {

    /**
     * The OKHttp client
     */
    private OkHttpClient client = new OkHttpClient();

    /**
     * The properties file containing the settings.
     */
    private Properties props;

    public LogCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host("www.warcraftlogs.com")
                .addPathSegments("/v1/reports/guild/"+ getBot().getGuildSettings(event.getGuild()).getGuildName()+"/"+ getBot().getGuildSettings(event.getGuild()).getWowServerName()+"/"+getBot().getGuildSettings(event.getGuild()).getRegionName())
                .addQueryParameter("api_key",props.getProperty("warcraftlogs.key"))
                .build();
        Request webRequest = new Request.Builder().url(url).build();

        try {
            String request = client.newCall(webRequest).execute().body().string();
            if (request == null) {
                event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "command.log.guild.not.found")).queue();
                return;
            }

            try {
                JSONParser parser = new JSONParser(); //{ "status": 400, "error": "Invalid guild name/server/region specified." }
                try {
                    JSONArray jsonArray = (JSONArray) parser.parse(request);
                    if (jsonArray.size() == 0) {
                        event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(),"command.log.no.log.found")).queue();
                        return;
                    }
                    JSONObject jsonObject = (JSONObject) jsonArray.toArray()[jsonArray.size() - 1];
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeZone(TimeZone.getTimeZone("America/Montreal")); //Todo, proper timezone
                    calendar.setTimeInMillis((Long) jsonObject.get("start"));
                    event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "command.log.message",
                            jsonObject.get("title").toString(),
                            jsonObject.get("owner").toString(),
                            calendar.get(Calendar.DAY_OF_MONTH) + "",
                            (calendar.get(Calendar.MONTH) + 1) + "",
                            calendar.get(Calendar.YEAR) + "") + " https://www.warcraftlogs.com/reports/" + jsonObject.get("id")).queue();
                } catch (ClassCastException e) {
                    event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(), "command.log.guild.not.found")).queue();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                getBot().getStacktraceHandler().sendStacktrace(e, "guildId:" + event.getGuild().getId(), "guildName:" + getBot().getGuildSettings(event.getGuild()).getGuildName(), "serverName:" + getBot().getGuildSettings(event.getGuild()).getWowServerName(),"region:" + getBot().getGuildSettings(event.getGuild()).getRegionName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e, "guildId:" + event.getGuild().getId(), "guildName:" + getBot().getGuildSettings(event.getGuild()).getGuildName(), "serverName:" + getBot().getGuildSettings(event.getGuild()).getWowServerName(),"region:" + getBot().getGuildSettings(event.getGuild()).getRegionName());
            event.getChannel().sendMessage(getBot().getTranslateManager().translate(event.getGuild(),"error.occurred.try.again.later")).queue();
        }
    }

    @Override
    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.log.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
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
        getBot().getCommandHandler().addCommand("log", this, "World of Warcraft");
        log.info("Command !log loaded");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("log");
        log.info("Command !log unloaded");
    }
}
