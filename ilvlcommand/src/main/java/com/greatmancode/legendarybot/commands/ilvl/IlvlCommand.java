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
package com.greatmancode.legendarybot.commands.ilvl;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.DiscordEmbedBuilder;
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import com.greatmancode.legendarybot.plugins.wowlink.utils.WowCommand;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import okhttp3.*;
import org.json.simple.parser.ParseException;
import org.pf4j.PluginWrapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * The !lookup command
 */
public class IlvlCommand extends LegendaryBotPlugin implements WowCommand, PublicCommand {


    /**
     * The OKHttp client
     */
    private final OkHttpClient client = new OkHttpClient.Builder()
            .build();

    public static final String SETTING_PRIVATE_LOOKUP = "lookupCommandPrivate";

    private Properties props;

    public IlvlCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        //Load the configuration
        props = new Properties();
        try {
            props.load(new FileInputStream("app.properties"));
        } catch (java.io.IOException e) {
            e.printStackTrace();
            getBot().getStacktraceHandler().sendStacktrace(e);
        }
        getBot().getCommandHandler().addCommand("lookup", this, "World of Warcraft");
        getBot().getCommandHandler().addAlias("ilvl", "lookup");
        getBot().getCommandHandler().addAlias("mplusrank", "lookup");
        getBot().getCommandHandler().addAlias("raidrank", "lookup");
        getBot().getCommandHandler().addCommand("privatelookup", new PrivateLookupCommand(getBot()), "WoW Admin Commands");
        log.info("command !ilvl loaded");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("lookup");
        getBot().getCommandHandler().removeAlias("ilvl");
        getBot().getCommandHandler().removeAlias("mplusrank");
        getBot().getCommandHandler().removeAlias("raidrank");
        getBot().getCommandHandler().removeCommand("privatelookup");
        log.info("command !ilvl unloaded");
    }

    public void execute(MessageReceivedEvent event, String[] args) {
        String[] realmInfo = new String[0];
        if (args.length > 1) {
            realmInfo = new String[args.length - 1];
            System.arraycopy(args, 1, realmInfo, 0, args.length - 1);
        }

        String[] realmResult = WoWUtils.extractRealmRegionInfo(getBot(), event.getGuild(), realmInfo);
        System.out.println(Arrays.toString(realmResult));
        String characterName = args[0];
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(props.getProperty("api.host"))
                .addPathSegments("api/character/"+realmResult[1]+"/"+realmResult[0]+"/"+characterName)
                .build();
        System.out.println(url);
        Request request = new Request.Builder().url(url).build();

        event.getChannel().sendMessage(new MessageBuilder()
                .append(event.getAuthor())
                .append(" please wait. Lookups can take a few seconds...")
                .build()).queue(message -> client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        message.editMessage(new MessageBuilder()
                        .append("Sorry ")
                        .append(event.getAuthor())
                        .append(". An error occurred. Try again in some minutes.")
                        .build()).queue();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.code() != 200) {
                            message.editMessage(new MessageBuilder()
                                .append("Sorry ")
                                .append(event.getAuthor())
                                .append(". It looks like " + characterName + "-" + realmResult[0] + " is not a valid character.").build()).queue();
                            return;
                        }
                        message.editMessage(new MessageBuilder().append(event.getAuthor()).append(", here is the requested information:").build()).queue();
                        message.editMessage(DiscordEmbedBuilder.convertJsonToMessageEmbed(response.body().string())).queue();
                    }
                }));

    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 99;
    }

    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild,"command.lookup.longhelp");
    }

    @Override
    public String shortDescription(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.lookup.shorthelp");
    }
}
