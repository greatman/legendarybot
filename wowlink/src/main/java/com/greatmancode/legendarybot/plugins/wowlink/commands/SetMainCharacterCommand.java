/*
 * MIT License
 *
 * Copyright (c) Copyright (c) 2017-2017, Greatmancode
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.greatmancode.legendarybot.plugins.wowlink.commands;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.plugins.wowlink.WoWLinkPlugin;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The !setmainchar command
 */
public class SetMainCharacterCommand implements PublicCommand {

    /**
     * The WowLink plugin instance.
     */
    private WoWLinkPlugin plugin;

    public SetMainCharacterCommand(WoWLinkPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (plugin.getBot().getGuildSettings(event.getGuild()).getGuildName() == null || plugin.getBot().getGuildSettings(event.getGuild()).getRegionName() == null) {
            event.getChannel().sendMessage("You can't run this command. A server administrator need to set GUILD_NAME and WOW_REGION_NAME. Refer to documentation.").queue();
            return;
        }

        List<String> characterList = plugin.getUserCharactersInGuild(event.getAuthor(), event.getGuild());
        if (!characterList.contains(args[0])) {
            event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage("This character is not in the Guild " + plugin.getBot().getGuildSettings(event.getGuild()).getGuildName() + " or is not found. Please try syncing your characters.").queue());
            return;
        }

        plugin.setMainCharacterForGuild(event.getAuthor(), event.getGuild(), args[0]);
        if (plugin.getBot().getGuildSettings(event.getGuild()).getSetting(WoWLinkPlugin.SETTING_RANKSET_ENABLED) != null) {
            List<Member> memberList = new ArrayList<>();
            memberList.add(event.getMember());
            plugin.doGuildRankUpdate(event.getMember().getUser(), event.getGuild(), memberList);
        }
        event.getAuthor().openPrivateChannel().queue((c) -> c.sendMessage("Your main character has been set.").queue());
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
    public String help() {
        return "Set your main guild character in this Discord server.\n" +
                "This allows command (if configured) like !syncrank to use this character to set your rank.\n" +
                "It also allows commands like !lookup to be used without giving your character name.\n\n" +
                "__Parameters__\n" +
                "**Wow Character** (Required): The name of your World of Warcraft character.\n\n" +
                "**Example**: ```!setmainchar Kugruon``";
    }

    @Override
    public String shortDescription() {
        return "Set your Main guild character.";
    }
}
