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
package com.greatmancode.legendarybot.commands.server;

import com.greatmancode.legendarybot.api.commands.PublicCommand;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.api.utils.BattleNet;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.Map;

public class ServerCommand extends LegendaryBotPlugin implements PublicCommand {

    private static final Logger log = LoggerFactory.getLogger(ServerCommand.class);

    public ServerCommand(PluginWrapper wrapper) {
        super(wrapper);

    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("server", this);
        log.info("Command !server loaded.");
    }

    @Override
    public void stop() throws PluginException {
        getBot().getCommandHandler().removeCommand("server");
        log.info("Command !server disabled.");
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        Map<String,String> map;
        if (args.length == 1) {
            map = BattleNet.getServerStatus(args[0]);
        } else {
            String serverName = getBot().getGuildSettings(event.getGuild()).getWowServerName();
            if (serverName == null) {
                event.getChannel().sendMessage("No server set. You are required to type a server.");
            }
            map = BattleNet.getServerStatus(serverName);
        }
        MessageBuilder builder = new MessageBuilder();
        if (map.size() == 4) {
            builder.append("Server: ");
            builder.append(map.get("name"));
            builder.append(" | Status: ");
            builder.append(map.get("status"));
            builder.append(" | Population: ");
            builder.append(map.get("population"));
            builder.append(" | Currently a queue? : ");
            builder.append(map.get("queue"));
        } else {
            builder.append("An error occured. Try again later.");
        }
        event.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public int minArgs() {
        return 0;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public String help() {
        return "!server <Server Name> - Retrieve a server status.";
    }


}
