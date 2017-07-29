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
package com.greatmancode.legendarybot.plugin.legendarycheck;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.utils.Utils;
import net.dv8tion.jda.core.entities.Guild;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

public class LegendaryCheck {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long[] itemIDIgnore = {147451,151462};
    public LegendaryCheck(Guild guild, LegendaryCheckPlugin plugin) {
        final Runnable checkNews = () -> {
            try {
                String serverName = plugin.getBot().getGuildSettings(guild).getWowServerName();
                String regionName = plugin.getBot().getGuildSettings(guild).getRegionName();
                String guildName = plugin.getBot().getGuildSettings(guild).getGuildName();
                if (regionName == null || serverName == null || guildName == null) {
                    return;
                }
                String urlString = "https://" + regionName + ".api.battle.net/wow/guild/" + serverName + "/" + guildName + "?fields=members&locale=en_US&apikey=" + LegendaryBot.getBattlenetKey();
                String channelName = plugin.getBot().getGuildSettings(guild).getSetting(LegendaryCheckPlugin.SETTING_NAME);
                String request = Utils.doRequest(urlString);
                if (request == null) {
                    return;
                }
                try {
                    System.out.println("Starting Legendary check for server " + guild.getName());
                    JSONObject object = (JSONObject) new JSONParser().parse(request);
                    JSONArray membersArray = (JSONArray) object.get("members");
                    for (Object memberObject : membersArray) {
                        JSONObject member = (JSONObject) ((JSONObject) memberObject).get("character");
                        String name = (String) member.get("name");
                        String realm = (String) member.get("realm");
                        if (realm == null) {
                            realm = (String) member.get("guildRealm");
                        }
                        long level = (Long) member.get("level");
                        if (level != 110) {
                            continue;
                        }
                        String memberURL = "https://" + regionName + ".api.battle.net/wow/character/" + realm + "/" + name + "?fields=feed&locale=en_US&apikey=" + LegendaryBot.getBattlenetKey();
                        String memberFeedRequest = Utils.doRequest(memberURL);
                        if (memberFeedRequest == null) {
                            continue;
                        }
                        JSONObject memberJson = (JSONObject) new JSONParser().parse(memberFeedRequest);

                        long memberLastModified = (Long) memberJson.get("lastModified");
                        long dbLastModified = plugin.getPlayerInventoryDate(regionName, serverName, name);
                        if (memberLastModified <= dbLastModified) {
                            continue;
                        }
                        plugin.setPlayerInventoryDate(regionName, serverName, name, memberLastModified);
                        //We check the items
                        JSONArray feedArray = (JSONArray) memberJson.get("feed");
                        for (Object feedObject : feedArray) {
                            JSONObject feed = (JSONObject) feedObject;
                            if (feed.get("type").equals("LOOT")) {
                                long itemID = (Long) feed.get("itemId");
                                long timestamp = (Long) feed.get("timestamp");
                                if (timestamp <= dbLastModified) {
                                    continue;
                                }
                                if (LongStream.of(itemIDIgnore).anyMatch(x -> x == itemID)) {
                                    continue;
                                }
                                String urlItem = "https://" + regionName + ".api.battle.net/wow/item/" + itemID + "?locale=en_US&apikey=" + LegendaryBot.getBattlenetKey();
                                String itemRequest = Utils.doRequest(urlItem);
                                if (itemRequest == null) {
                                    continue;
                                }
                                JSONObject itemObject = (JSONObject) new JSONParser().parse(itemRequest);
                                long quality = (Long) itemObject.get("quality");
                                if (quality == 5) {
                                    System.out.println(name + " just looted a legendary");
                                    //We got a legendary!
                                    guild.getTextChannelsByName(channelName, true).get(0).sendMessage(name + " just looted the legendary " + itemObject.get("name") + "! :tada:  http://www.wowhead.com/item=" + itemID).queue();
                                }
                            }
                            if (Thread.interrupted()) {
                                return;
                            }
                        }
                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                    System.out.println("Went through Legendary check for server " + guild.getName());
                } catch (ParseException e) {
                    e.printStackTrace();
                    LegendaryBot.getInstance().getStacktraceHandler().sendStacktrace(e);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println("Crashed for guild " + guild.getName() + ":" + guild.getId());
                LegendaryBot.getInstance().getStacktraceHandler().sendStacktrace(e);
            }
        };
        scheduler.scheduleAtFixedRate(checkNews, 0,10, TimeUnit.MINUTES);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
