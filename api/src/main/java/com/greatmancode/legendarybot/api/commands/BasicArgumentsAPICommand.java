package com.greatmancode.legendarybot.api.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BasicArgumentsAPICommand implements PublicCommand, APICommand {

    private final LegendaryBot bot;
    private final String helpString;
    private final String longHelpString;
    private final String endpoint;
    private final int minArguments;
    private final int maxArguments;
    private String[] defaultArguments;

    public BasicArgumentsAPICommand(LegendaryBot bot, String endpoint, String helpString, String longHelpString, int minArguments, int maxArguments, String... defaultArguments) {
        this.bot = bot;
        this.helpString = helpString;
        this.longHelpString = longHelpString;
        this.endpoint = endpoint;
        this.minArguments = minArguments;
        this.maxArguments = maxArguments;
        this.defaultArguments = defaultArguments;
    }
    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String newEndpoint = endpoint;
        for (int i = 0; i < args.length; i++) {
            newEndpoint = newEndpoint.replace("{args"+i+"}", args[i]);
        }
        while (newEndpoint.contains("args")) {
            int index = newEndpoint.indexOf("args");
            int argid = Integer.parseInt(newEndpoint.substring(index + 4, index + 5));
            if (defaultArguments.length > argid) {
                newEndpoint = newEndpoint.replace("{args" + argid + "}", defaultArguments[argid]);
            } else {
                newEndpoint = newEndpoint.replace("/{args" + argid + "}", "");
            }
        }
        executeAPICall(bot.getBotSettings().getProperty("api.host"),bot.getBotSettings().getProperty("api.key"), bot, event.getGuild(), event.getChannel(),newEndpoint);
    }

    @Override
    public int minArgs() {
        return minArguments;
    }

    @Override
    public int maxArgs() {
        return maxArguments;
    }

    @Override
    public String help(Guild guild) {
        return bot.getTranslateManager().translate(guild, longHelpString);
    }

    @Override
    public String shortDescription(Guild guild) {
        return bot.getTranslateManager().translate(guild, helpString);
    }
}
