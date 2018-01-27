package com.greatmancode.legendarybot.plugin.music.commands;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.AdminCommand;
import com.greatmancode.legendarybot.plugin.music.MusicPlugin;
import com.greatmancode.legendarybot.plugin.music.music.GuildMusicManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class SetVolumeCommand extends AdminCommand {

    private MusicPlugin plugin;


    public SetVolumeCommand(MusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        try {
            int volume = Integer.parseInt(args[0]);
            if (volume < 0 || volume > 100) {
                event.getChannel().sendMessage("The volume needs to be between 0 and 100!").queue();
                return;

            }

            GuildMusicManager manager = plugin.getMusicManager().getGuildsMusicManager().get(event.getGuild().getId());
            if (manager != null) {
                manager.player.setVolume(volume);
            }
            plugin.getBot().getGuildSettings(event.getGuild()).setSetting("MUSIC_VOLUME", volume + "");
            event.getChannel().sendMessage("Volume set to " + volume).queue();
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("The volume needs to be between 0 and 100!").queue();
        }
    }

    @Override
    public boolean canExecute(Member member) {
        return (super.canExecute(member) || plugin.getBot().getGuildSettings(member.getGuild()).getSetting(MusicPlugin.MEMBER_ALLOWED_SETTING) != null);
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
        return "Set the volume of the bot. Number between 1 and 100.";
    }

    public String shortDescription(Guild guild) {
        return "Set the volume of the bot. Number between 1 and 100.";
    }
}
