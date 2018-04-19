package com.greatmancode.legendarybot.plugins.removelastmessage;

import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class RemoveLastMessageListener extends ListenerAdapter {

    private RemoveLastMessagePlugin plugin;

    public RemoveLastMessageListener(RemoveLastMessagePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().equals(event.getJDA().getSelfUser()) && !(event.getChannel() instanceof PrivateChannel)) {
            plugin.addLastMessage(event.getGuild(), event.getMessage());
        }
    }
}
