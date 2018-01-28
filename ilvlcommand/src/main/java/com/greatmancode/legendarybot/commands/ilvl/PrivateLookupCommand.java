package com.greatmancode.legendarybot.commands.ilvl;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.entities.Guild;
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
            event.getChannel().sendMessage(bot.getTranslateManager().translate(event.getGuild(), "command.privatelookup.disable")).queue();
        } else {
            bot.getGuildSettings(event.getGuild()).setSetting(IlvlCommand.SETTING_PRIVATE_LOOKUP, "true");
            event.getChannel().sendMessage(bot.getTranslateManager().translate(event.getGuild(), "command.privatelookup.enable")).queue();
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
    public String help(Guild guild) {
        return bot.getTranslateManager().translate(guild, "command.privatelookup.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
