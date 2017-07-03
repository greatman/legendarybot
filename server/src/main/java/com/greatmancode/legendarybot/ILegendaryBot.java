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
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.utils.StacktraceHandler;
import com.greatmancode.legendarybot.commands.LoadCommand;
import com.greatmancode.legendarybot.commands.ReloadPluginsCommand;
import com.greatmancode.legendarybot.commands.UnloadCommand;
import com.greatmancode.legendarybot.server.IGuildSettings;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.apache.commons.io.FileUtils;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ILegendaryBot extends LegendaryBot {

    private PluginManager pluginManager = new LegendaryBotPluginManager(this);
    private CommandHandler commandHandler = new CommandHandler(this);
    private JDA jda;
    private Map<String, GuildSettings> guildSettings = new HashMap<>();
    private HikariDataSource dataSource;
    private StatsHandler statsHandler;
    private IStacktraceHandler stacktraceHandler;
    private static Properties props;

    public ILegendaryBot(JDA jda, String raygunKey, String battlenetKey) {
        super(battlenetKey);
        this.jda = jda;
        this.stacktraceHandler = new IStacktraceHandler(raygunKey);

        //We configure our Stacktrace catchers
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(stacktraceHandler));
        SimpleLog.getLog("JDA").addListener(new LogListener(stacktraceHandler));

        //Register the server specific commands
        commandHandler.addCommand("reloadplugins", new ReloadPluginsCommand(this));
        commandHandler.addCommand("load", new LoadCommand(this));
        commandHandler.addCommand("unload", new UnloadCommand(this));

        //We register the message listener
        jda.addEventListener(new MessageListener(this));

        //We create the Config table
        String SERVER_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS `guild_config` (\n" +
                "  `guildId` VARCHAR(64) NOT NULL,\n" +
                "  `configName` VARCHAR(255) NOT NULL,\n" +
                "  `configValue` MEDIUMTEXT NOT NULL,\n" +
                "  PRIMARY KEY (`guildId`, `configName`));\n";
        try {
            Connection conn = getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement(SERVER_CONFIG_TABLE);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Load the settings for each guild
        jda.getGuilds().forEach(guild -> guildSettings.put(guild.getId(), new IGuildSettings(guild, this)));

        //We load all plugins
        pluginManager.loadPlugins();
        pluginManager.startPlugins();


        //
        if (Boolean.parseBoolean(props.getProperty("stats.enable"))) {
            statsHandler = new StatsHandler(props, jda);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (PluginWrapper wrapper : getPluginManager().getPlugins()) {
                getPluginManager().unloadPlugin(wrapper.getPluginId());
            }
            jda.shutdown();

            File plugins = new File("plugins");
            Arrays.stream(plugins.listFiles(File::isDirectory)).forEach(file -> {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if (statsHandler != null) {
                statsHandler.stop();
            }
            System.out.println("Legendarybot shutdown.");
        }));
        jda.getGuilds().size();
    }

    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {



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

    public GuildSettings getGuildSettings(Guild guild) {
        return guildSettings.get(guild.getId());
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public HikariDataSource getDatabase() {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
            config.addDataSourceProperty("serverName", System.getenv("MYSQL_ADDRESS") != null ? System.getenv("MYSQL_ADDRESS") : props.getProperty("mysql.address"));
            config.addDataSourceProperty("port", System.getenv("MYSQL_PORT") != null ? System.getenv("MYSQL_PORT") : props.getProperty("mysql.port"));
            config.addDataSourceProperty("databaseName", System.getenv("MYSQL_DATABASE") != null ? System.getenv("MYSQL_DATABASE") : props.getProperty("mysql.database"));
            config.addDataSourceProperty("user", System.getenv("MYSQL_USER") != null ? System.getenv("MYSQL_USER") : props.getProperty("mysql.user"));
            config.addDataSourceProperty("password", System.getenv("MYSQL_PASSWORD") != null ? System.getenv("MYSQL_PASSWORD") : props.getProperty("mysql.password"));
            config.setConnectionTimeout(5000);
            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }


    public void addGuild(Guild guild) {
        guildSettings.put(guild.getId(), new IGuildSettings(guild, this));
    }

    @Override
    public StacktraceHandler getStacktraceHandler() {
        return stacktraceHandler;
    }

    @Override
    public JDA getJDA() {
        return jda;
    }
}
