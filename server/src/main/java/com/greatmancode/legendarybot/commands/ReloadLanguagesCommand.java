package com.greatmancode.legendarybot.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;

public class ReloadLanguagesCommand implements Command {

    private LegendaryBot bot;

    public ReloadLanguagesCommand(LegendaryBot bot) {
        this.bot = bot;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        try {
            bot.getTranslateManager().reload();
            event.getChannel().sendMessage("Reloaded the languages.").queue();
        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("An error occured.").queue();
        }
    }

    @Override
    public boolean canExecute(Member member) {
        return member.getUser().getId().equals("95709957629939712");
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
        return "Reload the language system";
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
