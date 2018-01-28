package com.greatmancode.legendarybot.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SetLanguageCommand extends AdminCommand {

    private LegendaryBot bot;

    public SetLanguageCommand(LegendaryBot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (!bot.getTranslateManager().getLanguages().contains(args[0])) {
            StringBuilder builder = new StringBuilder();
            bot.getTranslateManager().getLanguages().forEach(k -> builder.append(k).append(", "));
            event.getChannel().sendMessage(bot.getTranslateManager().translate(event.getGuild(),"command.setlanguage.langnotfound",builder.toString().substring(0,builder.toString().length() - 1))).queue();
            return;
        }
        bot.getGuildSettings(event.getGuild()).setSetting("LANGUAGE",args[0]);
        event.getChannel().sendMessage(bot.getTranslateManager().translate(event.getGuild(), "command.setlanguage.message",args[0])).queue();
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 1;
    }


    @Override
    public String help(Guild guild) {
        StringBuilder builder = new StringBuilder();
        bot.getTranslateManager().getLanguages().forEach(k -> builder.append(k).append(","));
        return bot.getTranslateManager().translate(guild,"command.setlanguage.longhelp",builder.toString().substring(0,builder.toString().length() - 1));
    }

    @Override
    public String shortDescription(Guild guild) {
        return bot.getTranslateManager().translate(guild, "command.setlanguage.shorthelp");
    }
}
