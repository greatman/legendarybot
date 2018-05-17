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

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.DiscordEmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import okhttp3.HttpUrl;
import org.pf4j.PluginWrapper;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The Legendary Check plugin
 */
public class LegendaryCheckPlugin extends LegendaryBotPlugin{

    /**
     * The setting name where we save the legendary check channel name.
     */
    public static final String SETTING_NAME = "legendary_check";

    /**
     * The scheduler for the checks.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private AmazonSQS sqs = null;

    private Cache<String, String> messageCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    public LegendaryCheckPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        log.info("Starting LegendaryCheck plugin.");
        AWSStaticCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(getBot().getBotSettings().getProperty("aws.accesskey"), getBot().getBotSettings().getProperty("aws.secretkey")));
        sqs = AmazonSQSClientBuilder.standard().withRegion(getBot().getBotSettings().getProperty("aws.region")).withCredentials(credentialsProvider).build();
        getBot().getCommandHandler().addCommand("enablelc", new EnableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        getBot().getCommandHandler().addCommand("disablelc", new DisableLegendaryCheckCommand(this), "Legendary Check Admin Commands");
        log.info("Command !enablelc, !disablelc and !mutelc added!");
        log.info("Loading the LegendaryCheck Scheduler");

        final Runnable runnable = () -> {
            System.out.println("Checking SQS");
            GetQueueUrlResult queueUrlResult = sqs.getQueueUrl(getBot().getBotSettings().getProperty("aws.sqs.queuename"));
            GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest().withQueueUrl(queueUrlResult.getQueueUrl()).withAttributeNames("ApproximateNumberOfMessages");
            int messageCount = Integer.parseInt(sqs.getQueueAttributes(queueAttributesRequest).getAttributes().get("ApproximateNumberOfMessages"));
            System.out.println(messageCount + " in queue.");
            while (messageCount > 0) {

                ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrlResult.getQueueUrl())
                        .withMessageAttributeNames("server","channel")
                        .withMaxNumberOfMessages(Math.min(messageCount,10));
                sqs.receiveMessage(request).getMessages().forEach(message -> {
                    if (messageCache.getIfPresent(message.getMessageId()) == null) {
                        messageCache.put(message.getMessageId(), "");
                        try {
                            long serverID = Long.parseLong(message.getMessageAttributes().get("server").getStringValue());
                            System.out.println("Server " + serverID + " got a legendary. Message ID:" + message.getMessageId());
                            for (JDA jda : getBot().getJDA()) {
                                Guild guild = jda.getGuildById(serverID);
                                if (guild != null) {
                                    List<TextChannel> channelList = guild.getTextChannelsByName(message.getMessageAttributes().get("channel").getStringValue(),true);
                                    if (!channelList.isEmpty()) {
                                        channelList.get(0).sendMessage(DiscordEmbedBuilder.convertJsonToMessageEmbed(message.getBody())).queue();
                                    }
                                }
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Message " + message.getMessageId() + " was already received. Ignoring.");
                    }

                    sqs.deleteMessage(new DeleteMessageRequest(queueUrlResult.getQueueUrl(), message.getReceiptHandle()));
                });
                messageCount = Integer.parseInt(sqs.getQueueAttributes(queueAttributesRequest).getAttributes().get("ApproximateNumberOfMessages"));
                System.out.println(messageCount + " in queue.");
            }
        };
        scheduler.scheduleAtFixedRate(runnable,0, 60, TimeUnit.SECONDS);
        log.info("Plugin LegendaryCheck started!");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("enablelc");
        getBot().getCommandHandler().removeCommand("disablelc");
        getBot().getCommandHandler().removeCommand("mutelc");
        scheduler.shutdown();
        log.info("Plugin OldLegendaryCheck unloaded! Command !enablelc, !disablelc  and !mutelc removed");
    }

    /**
     * Stops and deletes the config of a legendary check for a guild.
     * @param guild The guild to disable the legendary check.
     */
    public void destroyLegendaryCheck(Guild guild) {
        getBot().getGuildSettings(guild).unsetSetting(SETTING_NAME);
    }
}
