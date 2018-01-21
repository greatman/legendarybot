package com.greatmancode.legendarybot.commands.ilvl;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class PrivateLookupCommand extends AdminCommand {


    private LegendaryBot bot;

    public PrivateLookupCommand(LegendaryBot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (bot.getGuildSettings(event.getGuild()).getSetting(IlvlCommand.SETTING_PRIVATE_LOOKUP) != null) {
            bot.getGuildSettings(event.getGuild()).unsetSetting(IlvlCommand.SETTING_PRIVATE_LOOKUP);
            event.getChannel().sendMessage("The bot now answers the !lookup command in the channel it has been requested").queue();
        } else {
            bot.getGuildSettings(event.getGuild()).setSetting(IlvlCommand.SETTING_PRIVATE_LOOKUP, "true");
            event.getChannel().sendMessage("The bot now answers the !lookup command in a private message.").queue();
        }
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 0;
    }

    @Override
    public String help() {
        return "Make the !lookup command answer in a private message.";
    }

    @Override
    public String shortDescription() {
        return "Make the !lookup command answer in a private message.";
    }
}
