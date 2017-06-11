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
package com.greatmancode.legendarybot;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.greatmancode.legendarybot.api.commands.CommandHandler;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPluginManager;
import com.greatmancode.legendarybot.api.server.ServerSettings;
import com.greatmancode.legendarybot.api.utils.LogListener;
import com.greatmancode.legendarybot.commands.ReloadCommand;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ILegendaryBot extends LegendaryBot {

    private PluginManager pluginManager = new LegendaryBotPluginManager(this);
    private CommandHandler commandHandler = new CommandHandler();
    private JDA jda;
    private Map<String, ServerSettings> serverSettings = new HashMap<>();

    private static Properties props;

    public ILegendaryBot(JDA jda, String raygunKey, String battlenetKey) throws IOException {
        super(raygunKey, battlenetKey);
        this.jda = jda;

        //Register the server specific commands
        commandHandler.addCommand("reload", new ReloadCommand(this));

        //We register the message listener
        jda.addEventListener(new MessageListener(this));

        //We load all plugins
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (PluginWrapper wrapper : getPluginManager().getPlugins()) {
                getPluginManager().unloadPlugin(wrapper.getPluginId());
            }
            jda.shutdown();

            System.out.println("Legendarybot shutdown.");
        }));
    }

    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {

        //We configure our Stacktrace catchers
        //Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        SimpleLog.getLog("JDA").addListener(new LogListener());

        //Load the configuration
        props = new Properties();
        props.load(new FileInputStream("app.properties"));

        //Connect the bot to Discord
        JDA jda = new JDABuilder(AccountType.BOT).setToken(System.getenv("BOT_TOKEN") != null ? System.getenv("BOT_TOKEN") : props.getProperty("bot.token")).buildBlocking();
        //We launch the bot
        new ILegendaryBot(jda, props.getProperty("raygun.key"), props.getProperty("battlenet.key"));
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public ServerSettings getServerSettings(Guild guild) {
        //TODO Use the actual Hashmap for server settings
        return new ServerSettings();
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
