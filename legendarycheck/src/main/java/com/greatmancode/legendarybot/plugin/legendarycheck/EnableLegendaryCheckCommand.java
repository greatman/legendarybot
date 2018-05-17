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
package com.greatmancode.legendarybot.plugin.legendarycheck;

import com.greatmancode.legendarybot.api.commands.AdminCommand;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * The !enablelc command
 */
public class EnableLegendaryCheckCommand extends AdminCommand {

    /**
     * An instance of the Legendary Check plugin
     */
    private LegendaryCheckPlugin plugin;

    public EnableLegendaryCheckCommand(LegendaryCheckPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        if (event.getMessage().getMentionedChannels().size() != 0) {
            plugin.getBot().getGuildSettings(event.getGuild()).setSetting(LegendaryCheckPlugin.SETTING_NAME, event.getMessage().getMentionedChannels().get(0).getName());
            event.getChannel().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(),"command.enablelegendarycheck.message",args[0])).queue();
        } else {
            event.getAuthor().openPrivateChannel().complete().sendMessage(plugin.getBot().getTranslateManager().translate(event.getGuild(), "channel.not.found")).queue();
        }

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
        return plugin.getBot().getTranslateManager().translate(guild, "command.enablelegendarycheck.longhelp");
    }

    @Override
    public String shortDescription(Guild guild) {
        return plugin.getBot().getTranslateManager().translate(guild, "command.enablelegendarycheck.shorthelp");
    }
}
