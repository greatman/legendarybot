package com.greatmancode.legendarybot.api.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class BasicPublicZeroArgsAPICommand implements PublicCommand, ZeroArgsCommand, APICommand {

    private final LegendaryBot bot;
    private final String helpString;
    private final String longHelpString;
    private final String endpoint;


    public BasicPublicZeroArgsAPICommand(LegendaryBot bot, String endpoint, String helpString, String longHelpString) {
        this.bot = bot;
        this.helpString = helpString;
        this.longHelpString = longHelpString;
        this.endpoint = endpoint;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        executeAPICall(bot.getBotSettings().getProperty("api.host"),bot.getBotSettings().getProperty("api.key"), bot, event.getGuild(), event.getChannel(),endpoint);
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
