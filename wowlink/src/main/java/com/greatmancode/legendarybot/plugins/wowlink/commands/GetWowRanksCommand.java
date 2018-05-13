package com.greatmancode.legendarybot.plugins.wowlink.commands;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.api.commands.ZeroArgsCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.json.JSONObject;

public class GetWowRanksCommand extends AdminCommand implements ZeroArgsCommand {

    private WoWLinkPlugin plugin;

    public GetWowRanksCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String setting = plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKS);
        JSONObject rankSettings = new JSONObject();
        if (setting != null) {
            rankSettings = new JSONObject(setting);
        }
        if (rankSettings.length() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("Here are the current rank setup:\n");
            for (int i = 0; i < 10; i++) {
                builder.append(rankSettings.has(i + "") ? i + ": " + rankSettings.getString(i + "") : i + ": None");
                builder.append("\n");
            }
            event.getAuthor().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(builder.toString()).queue());
        }
    }

    @Override
    public String help(Guild guild) {
        return "Get the ranks currently configured for the auto-rank system.";
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }
}
