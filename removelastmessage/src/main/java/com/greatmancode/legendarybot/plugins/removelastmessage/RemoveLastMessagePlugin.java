package com.greatmancode.legendarybot.plugins.removelastmessage;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import org.pf4j.PluginWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RemoveLastMessagePlugin extends LegendaryBotPlugin {

    private Map<String, List<Message>> lastMessageMap = new HashMap<>();
    private RemoveLastMessageListener listener = new RemoveLastMessageListener(this);

    public RemoveLastMessagePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        getBot().getCommandHandler().addCommand("remove", new RemoveLastMessageCommand(this), "Admin Commands");
        getBot().getJDA().forEach(jda -> jda.addEventListener(listener));
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("remove");
        getBot().getJDA().forEach(jda -> jda.removeEventListener(listener));
    }

    public void addLastMessage(Guild guild, Message message) {
        if (lastMessageMap.containsKey(guild.getId())) {
            lastMessageMap.get(guild.getId()).add(message);
        } else {
            lastMessageMap.put(guild.getId(), new ArrayList<>());
            lastMessageMap.get(guild.getId()).add(message);
        }
        if (lastMessageMap.get(guild.getId()).size() >= 10) {
            lastMessageMap.get(guild.getId()).remove(0);
        }
    }

    public void removeLastMessage(Guild guild) {
        if (lastMessageMap.containsKey(guild.getId())) {
            List<Message> messageMap = lastMessageMap.get(guild.getId());
            if (messageMap.size() > 0) {
                Message message = messageMap.get(messageMap.size() - 1);
                message.delete().queue();
                messageMap.remove(message);
            }
        }

    }
}
