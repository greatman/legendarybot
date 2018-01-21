package com.greatmancode.legendarybot.plugins.wowlink.commands;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class EnableAutoRankUpdateCommand extends AdminCommand {

    private WoWLinkPlugin plugin;

    public EnableAutoRankUpdateCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (plugin.getBot().getGuildSettings(event.getGuild()).getGuildName() == null || plugin.getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage("You can't run this command. A server administrator needs to configure the bot first. Ask him to use !setup.").queue();
            return;
        }

        if (!plugin.getBot().getJDA(event.getGuild()).getGuildById(event.getGuild().getId()).getMember(event.getJDA().getSelfUser()).hasPermission(Permission.MANAGE_ROLES)) {
            event.getChannel().sendMessage("The bot need the \"**Manage Roles**\" permission to be able to set roles to the users.").queue();
            return;
        }

        if (plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKSET_ENABLED) != null && plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_SCHEDULER) == null) {
            event.getChannel().sendMessage("The Auto Rank Update is already enabled.");
            return;
        }

        plugin.enableAutoRankUpdate(event.getGuild());
        event.getChannel().sendMessage("Auto Rank Update enabled. Ranks will be updated every 30 minutes.").queue();

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
        return "Enable the Auto Rank Update Scheduler. It makes the bot updates the ranks automaticly every 30 minutes.";
    }
}
