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
package com.greatmancode.legendarybot.plugins.legionbuilding;

import com.greatmancode.legendarybot.api.commands.PublicAPIBackendZeroArgsEmbedCommandWithPlugin;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.pf4j.PluginWrapper;

/**
 * The !legionbuilding command
 */
public class LegionBuildingCommand extends PublicAPIBackendZeroArgsEmbedCommandWithPlugin {

    public LegionBuildingCommand(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        String region = getBot().getGuildSettings(event.getGuild()).getRegionName();
        if (region != null) {
            executeAPICall(event.getChannel(), "api/legionbuilding/"+region);
        }
    }

    @Override
    public String help(Guild guild) {
        return getBot().getTranslateManager().translate(guild, "command.legionbuilding.help");
    }

    @Override
    public String shortDescription(Guild guild) {
        return help(guild);
    }

    @Override
    public void start() {
        super.start();
        getBot().getCommandHandler().addCommand("legionbuilding", this, "World of Warcraft");
    }

    @Override
    public void stop() {
        getBot().getCommandHandler().removeCommand("legionbuilding");
    }
}
