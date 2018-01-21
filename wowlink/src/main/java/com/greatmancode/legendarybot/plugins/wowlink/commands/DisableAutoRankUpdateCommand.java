package com.greatmancode.legendarybot.plugins.wowlink.commands;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class DisableAutoRankUpdateCommand extends AdminCommand {

    private WoWLinkPlugin plugin;

    public DisableAutoRankUpdateCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        plugin.disableAutoRankUpdate(event.getGuild());
        event.getChannel().sendMessage("Auto Rank Update disabled. The bot will not set automatically the ranks every 30 minutes.").queue();
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
        return shortDescription();
    }

    @Override
    public String shortDescription() {
        return "Disable the Auto Rank Update Scheduler.";
    }
}
