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
import com.greatmancode.legendarybot.api.translate.TranslateManager;
import com.greatmancode.legendarybot.api.utils.NullStacktraceHandler;
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
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Implementation of a {@link LegendaryBot} bot.
 */
public class ILegendaryBot extends LegendaryBot {

    /**
     * The Plugin Manager
     */
    private PluginManager pluginManager = new LegendaryBotPluginManager(this);

    /**
     * The Command handler
     */
    private CommandHandler commandHandler = new CommandHandler(this);

    /**
     * The settings of every guilds that this bot is connected to
     */
    private Map<String, GuildSettings> guildSettings = new HashMap<>();

    /**
     * The Database data source
     */
    private HikariDataSource dataSource;

    /**
     * The instance of the Stacktrace Handler.
     */
    private StacktraceHandler stacktraceHandler;

    /**
     * Elastic search client
     */
    private RestClient restClient;

    private InfluxDB influxDB;

    /**
     * The app.properties file.
     */
    private static Properties props;

    /**
     * The List of all JDA instances
     */
    private List<JDA> jdaList = new ArrayList<>();

    /**
     * The Logger
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The state of the bot. True = Ready to accept commands, False = not ready to accept commands.
     */
    private boolean ready;

    private TranslateManager translateManager;

    /**
     * Start all the feature of the LegendaryBot
     */
    public ILegendaryBot() throws IOException, LoginException, InterruptedException, RateLimitedException {
        log.info("LegendaryBot Starting.");
        if (props.containsKey("stats.enable") && Boolean.parseBoolean(props.getProperty("stats.enable"))) {
            influxDB = InfluxDBFactory.connect("http://localhost:8086").setDatabase("legendarybot2");
        } else {
            influxDB = new InfluxDBNull();
        }
        influxDB.createDatabase("legendarybot2");

        if (props.containsKey("sentry.key")) {
            this.stacktraceHandler = new IStacktraceHandler(this, props.getProperty("sentry.key"));
        } else {
            this.stacktraceHandler = new NullStacktraceHandler();
        }



        //Load the database
        HikariConfig config = new HikariConfig();
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("serverName", System.getenv("MYSQL_ADDRESS") != null ? System.getenv("MYSQL_ADDRESS") : props.getProperty("mysql.address"));
        config.addDataSourceProperty("port", System.getenv("MYSQL_PORT") != null ? System.getenv("MYSQL_PORT") : props.getProperty("mysql.port"));
        config.addDataSourceProperty("databaseName", System.getenv("MYSQL_DATABASE") != null ? System.getenv("MYSQL_DATABASE") : props.getProperty("mysql.database"));
        config.addDataSourceProperty("user", System.getenv("MYSQL_USER") != null ? System.getenv("MYSQL_USER") : props.getProperty("mysql.user"));
        config.addDataSourceProperty("password", System.getenv("MYSQL_PASSWORD") != null ? System.getenv("MYSQL_PASSWORD") : props.getProperty("mysql.password"));
        config.setConnectionTimeout(5000);
        config.addDataSourceProperty("characterEncoding","utf8");
        config.addDataSourceProperty("useUnicode","true");
        dataSource = new HikariDataSource(config);

        //We configure our Stacktrace catchers
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(stacktraceHandler));
        //Register the server specific commands
        commandHandler.addCommand("reloadplugins", new ReloadPluginsCommand(this), "Admin Commands");
        commandHandler.addCommand("load", new LoadCommand(this), "Admin Commands");
        commandHandler.addCommand("unload", new UnloadCommand(this), "Admin Commands");


        //We create the Config table
        String SERVER_CONFIG_TABLE = "CREATE TABLE IF NOT EXISTS `guild_config` (\n" +
                "  `guildId` VARCHAR(64) NOT NULL,\n" +
                "  `configName` VARCHAR(255) NOT NULL,\n" +
                "  `configValue` MEDIUMTEXT NOT NULL,\n" +
                "  PRIMARY KEY (`guildId`, `configName`))ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;\n";
        try {
            Connection conn = getDatabase().getConnection();
            PreparedStatement statement = conn.prepareStatement(SERVER_CONFIG_TABLE);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            getStacktraceHandler().sendStacktrace(e);
        }

        //We build JDA and connect.
        JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(System.getenv("BOT_TOKEN") != null ? System.getenv("BOT_TOKEN") : props.getProperty("bot.token")).setReconnectQueue(new SessionReconnectQueue());
        builder.addEventListener(new MessageListener(this));
        int maxShard = props.containsKey("bot.shard") ? Integer.parseInt(props.getProperty("bot.shard")) : 1;
        for (int i = 0; i < maxShard; i++) {
            log.info("Starting shard " + i);
            jdaList.add(builder.useSharding(i,maxShard)
                    .buildBlocking(JDA.Status.CONNECTED));
        }
        //Load the settings for each guild
        jdaList.forEach((jda) -> jda.getGuilds().forEach(guild -> guildSettings.put(guild.getId(), new IGuildSettings(guild, this))));




        //We load all plugins
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        ready = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ready = false;
            for (PluginWrapper wrapper : getPluginManager().getPlugins()) {
                getPluginManager().unloadPlugin(wrapper.getPluginId());
            }
            jdaList.forEach(JDA::shutdown);

            File plugins = new File("plugins");
            Arrays.stream(plugins.listFiles(File::isDirectory)).forEach(file -> {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    getStacktraceHandler().sendStacktrace(e);
                }
            });
            if (restClient != null) {
                try {
                    restClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    getStacktraceHandler().sendStacktrace(e);
                }
            }
            log.info("Legendarybot Shutdown.");
        }));

        translateManager = new TranslateManager(this);

        log.info("LegendaryBot now ready!");

    }

    /**
     * Launch the Bot
     * @param args Command line arguments (unused)
     * @throws IOException IOException
     * @throws LoginException LoginException
     * @throws InterruptedException InterruptedException
     * @throws RateLimitedException RateLimitedException
     */
    public static void main(String[] args) throws IOException, LoginException, InterruptedException, RateLimitedException {
        //We load the properties file.
        props = new Properties();
        props.load(new FileInputStream("app.properties"));
        new ILegendaryBot();
    }

    @Override
    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    @Override
    public GuildSettings getGuildSettings(Guild guild) {
        return guildSettings.get(guild.getId());
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public HikariDataSource getDatabase() {
        return dataSource;
    }

    @Override
    public void addGuild(Guild guild) {
        guildSettings.put(guild.getId(), new IGuildSettings(guild, this));
    }

    @Override
    public StacktraceHandler getStacktraceHandler() {
        return stacktraceHandler;
    }

    @Override
    public RestClient getElasticSearch() {
        if (restClient == null) {
            restClient = RestClient.builder(
                    new HttpHost(props.getProperty("elasticsearch.address"), Integer.parseInt(props.getProperty("elasticsearch.port")), props.getProperty("elasticsearch.scheme"))).build();
        }
        return restClient;
    }

    @Override
    public InfluxDB getStatsClient() {
        return influxDB;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public TranslateManager getTranslateManager() {
        return translateManager;
    }

    @Override
    public JDA getJDA(Guild guild) {
        for (JDA jda : jdaList) {
            if (jda.getGuildById(guild.getId()) != null) {
                return jda;
            }
        }
        return null;
    }

    @Override
    public List<JDA> getJDA() {
        return Collections.unmodifiableList(jdaList);
    }
}
