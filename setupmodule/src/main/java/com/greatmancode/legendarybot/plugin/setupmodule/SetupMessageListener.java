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
package com.greatmancode.legendarybot.plugin.setupmodule;

import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.api.utils.WoWUtils;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SetupMessageListener extends ListenerAdapter {

    private final OkHttpClient client;
    private SetupPlugin plugin;

    public SetupMessageListener(SetupPlugin plugin) {
        this.plugin = plugin;
        client = new OkHttpClient.Builder()
                .addInterceptor(new BattleNetAPIInterceptor(plugin.getBot()))
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE) || event.getAuthor().isBot()) {
            return;
        }

        SetupHandler setupHandler = plugin.getSetupHandler(event.getGuild());
        if (setupHandler == null) {
            return;
        }

        if (!setupHandler.getUser().equals(event.getAuthor())) {
            return;
        }
        if (!setupHandler.getChannel().equals(event.getChannel())) {
            return;
        }

        if (event.getMessage().getContent().contains("setup")) {
            return;
        }

        if (event.getMessage().getContent().equalsIgnoreCase("cancel")) {
            plugin.setupDone(event.getGuild());
            event.getChannel().sendMessage("Setup canceled. The bot may behave weirdly if you left right in the middle of the setup wizard.").queue();
            return;
        }

        if (setupHandler.getState() == SetupState.STEP_PREFIX) {
            String prefix = event.getMessage().getContent();
            plugin.getBot().getGuildSettings(event.getGuild()).setSetting("PREFIX", prefix);
            setupHandler.setState(SetupState.STEP_REGION);

            event.getChannel().sendMessage("Awesome! I will now answer with the prefix ``" + prefix + "`` . Example: ``" + prefix + "help``").queue();
            StringBuilder builder = new StringBuilder();
            builder.append("Let's move to the next setting.\n");
            builder.append("I have multiple commands that supports a World of Warcraft region.\n");
            builder.append("The regions I support are **US** (North America / Oceanic / Latin America / Brazil) or **EU**\n\n");
            builder.append("**Default Value**: ``None``\n");
            String region = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("WOW_REGION_NAME");
            if (region == null) {
                region = "None";
            }
            builder.append("**Current Setting**: ``" + region + "``\n");
            builder.append("**Examples**: ``US`` ``EU``\n");
            builder.append("So, which **region** you want to use?");
            event.getChannel().sendMessage(builder.toString()).queue();
            return;
        }

        if (setupHandler.getState() == SetupState.STEP_REGION) {
            String region = event.getMessage().getContent();
            if (!region.equalsIgnoreCase("us") && !region.equalsIgnoreCase("eu")) {
                event.getChannel().sendMessage("I only support ``US`` or ``EU``. Please type one or the other.").queue();
                return;
            }

            plugin.getBot().getGuildSettings(event.getGuild()).setSetting("WOW_REGION_NAME", region.toUpperCase());
            setupHandler.setState(SetupState.STEP_REALM);
            event.getChannel().sendMessage("Perfect! I will remember that your Discord server plays on the **" + region.toUpperCase() + "** realms.").queue();
            StringBuilder builder = new StringBuilder();
            builder.append("Let's move to the next setting.\n");
            builder.append("Same thing as the previous setting, but with your **Realm**\n");
            builder.append("A **Realm** is the primary World of Warcraft server you play on.\n");
            builder.append("Don't worry about how you type it. I should find it without a problem!\n\n");
            builder.append("**Default Value**: ``None``\n");
            String realm = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("WOW_SERVER_NAME");
            if (realm == null) {
                realm = "None";
            }
            builder.append("**Current Setting**: ``" + realm + "``\n");
            builder.append("**Examples**: ``Emerald Dream``\n");
            builder.append("So, which **Realm** do you play on?");
            event.getChannel().sendMessage(builder.toString()).queue();
            return;
        }

        if (setupHandler.getState() == SetupState.STEP_REALM) {
            String realm = event.getMessage().getContent();

            try {
                String realmData = WoWUtils.getRealmInformation(plugin.getBot(),plugin.getBot().getGuildSettings(event.getGuild()).getRegionName(), realm);
                if (realmData == null) {
                    event.getChannel().sendMessage("I did not found a Realm with the name of " + realm + ". Did you make a typo?").queue();
                    return;
                }
                JSONParser jsonParser = new JSONParser();
                JSONObject source = (JSONObject) jsonParser.parse(realmData);
                String serverSlug = (String) source.get("slug");
                String timezone = (String) source.get("timezone");
                String name = (String) source.get("name");
                String pvp = (String) source.get("type");
                LocalDateTime time = LocalDateTime.now(ZoneId.of(timezone));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                setupHandler.setTempValue(serverSlug);
                event.getChannel().sendMessage("I found the Realm **" + name + "**. The type of the Realm is **" + pvp.toUpperCase() + "** and the current Realm time is **" + formatter.format(time) + "**. Is this correct?").queue();
                event.getChannel().sendMessage("Say ``yes`` or ``no``.").queue();

                setupHandler.setState(SetupState.STEP_REALM_CONFIRM);
            } catch (ParseException e) {
                e.printStackTrace();
                plugin.getBot().getStacktraceHandler().sendStacktrace(e,"guildid:" + event.getGuild().getId(),"channel:" + event.getChannel().getName(), "realm:" + realm + "setupwizard:" + setupHandler.getState());
                event.getChannel().sendMessage("I did not found a Realm with the name of " + realm + ". Did you make a typo?").queue();
            }
            return;
        }

        if (setupHandler.getState() == SetupState.STEP_REALM_CONFIRM) {
            String confirmation = event.getMessage().getContent();
            if (confirmation.equalsIgnoreCase("no")) {

                event.getChannel().sendMessage("Please type the realm name you want to configure.").queue();
                setupHandler.setState(SetupState.STEP_REALM);

            } else if (confirmation.equalsIgnoreCase("yes")) {

                plugin.getBot().getGuildSettings(event.getGuild()).setSetting("WOW_SERVER_NAME", setupHandler.getTempValue());
                StringBuilder builder = new StringBuilder();
                builder.append("Wonderful! I will now remember that **" + setupHandler.getTempValue() + "** is your preferred realm!\n\n");
                builder.append("We got 1 last setting to configure.\n");
                builder.append("LegendaryBot supports World of Warcraft **Guilds**. It makes commands like ``!log`` find the latest Warcraft Logs entry of your guild.\n\n");
                builder.append("**Default Value**: ``None``\n");
                String guild = plugin.getBot().getGuildSettings(event.getGuild()).getSetting("GUILD_NAME");
                if (guild == null) {
                    guild = "None";
                }
                builder.append("**Current Setting**: ``" + guild + "``\n");
                builder.append("So, what is your **Guild name**? If you don't want to configure one, you can simply exit the setup with the word ``cancel``");
                event.getChannel().sendMessage(builder.toString()).queue();
                setupHandler.setState(SetupState.STEP_GUILD);
            }else {
                event.getChannel().sendMessage("Please say ``yes`` or ``no``.").queue();
            }
            return;
        }
        if (setupHandler.getState() == SetupState.STEP_GUILD) {
            String guild = event.getMessage().getContent();
            GuildSettings settings = plugin.getBot().getGuildSettings(event.getGuild());
            try {
                if (guildExist(settings.getRegionName(),settings.getWowServerName(),guild)) {
                    settings.setSetting("GUILD_NAME", guild);
                    event.getChannel().sendMessage("Such a great Guild name! I will remember that your guild is **" + guild + "**").queue();
                    event.getChannel().sendMessage("That's it! We have finished the setup. For further help, feel free to use the ``" + plugin.getBot().getGuildSettings(event.getGuild()).getSetting("PREFIX") + "help`` command or visit the LegendaryBot server @ https://discord.gg/Cr7G28H").queue();
                    plugin.setupDone(event.getGuild());
                } else {
                    event.getChannel().sendMessage("Guild **" + guild + "** not found! Did you make a typo?").queue();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        //TODO other features setup
    }

    /**
     * Checks if a Guild exist in World of Warcraft
     * @param region The Region the server is hosted in.
     * @param serverName The server name where the guild belongs to
     * @param guildName The guild name
     * @return true if the guild exist.
     */
    public boolean guildExist(String region, String serverName, String guildName) throws IOException {
        HttpUrl url = new HttpUrl.Builder().scheme("https")
                .host(region + ".api.battle.net")
                .addPathSegments("/wow/guild/" + serverName + "/" + guildName)
                .build();
        Request request = new Request.Builder().url(url).build();
        String result = client.newCall(request).execute().body().string();
        //TODO better check here
        return result != null && !result.contains("nok");
    }
}
