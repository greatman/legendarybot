package com.greatmancode.legendarybot.plugins.removelastmessage;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class RemoveLastMessageCommand extends AdminCommand {

    private RemoveLastMessagePlugin plugin;

    public RemoveLastMessageCommand(RemoveLastMessagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
            plugin.removeLastMessage(event.getGuild());
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
        return plugin.getBot().getTranslateManager().translate(guild, "command.remove.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
