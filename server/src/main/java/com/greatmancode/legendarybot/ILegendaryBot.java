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
import com.greatmancode.legendarybot.commands.*;
import com.greatmancode.legendarybot.server.IGuildSettings;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import org.apache.http.HttpHost;
import org.bson.Document;
import org.elasticsearch.client.RestClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

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
    private MongoClient mongoClient;

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

    private String databaseName;

    /**
     * Start all the feature of the LegendaryBot
     */
    public ILegendaryBot() throws IOException, LoginException, InterruptedException, RateLimitedException {
        log.info("LegendaryBot Starting.");

        //TODO variable it into app.properties.
        influxDB = (props.containsKey("stats.enable") && Boolean.parseBoolean(props.getProperty("stats.enable"))) ? InfluxDBFactory.connect("http://localhost:8086").setDatabase("legendarybot2") : new InfluxDBNull();
        influxDB.createDatabase("legendarybot2");


        //We load the stacktrace handler.
        this.stacktraceHandler = props.containsKey("sentry.key") ? new IStacktraceHandler(this, props.getProperty("sentry.key")) : new NullStacktraceHandler();

        //Load the database
        databaseName = props.getProperty("mongodb.database");
        MongoClientOptions.Builder mongoClientOptionsBuilder = MongoClientOptions.builder();
        if (props.containsKey("mongodb.ssl")) {
            mongoClientOptionsBuilder.sslEnabled(Boolean.parseBoolean(props.getProperty("mongodb.ssl")));
        }

        if (props.containsKey("mongodb.username") && props.containsKey("mongodb.password")) {
            MongoCredential credential = MongoCredential.createCredential(props.getProperty("mongodb.username"),"admin", props.getProperty("mongodb.password").toCharArray());
            mongoClient = new MongoClient(new ServerAddress(props.getProperty("mongodb.server"), Integer.parseInt(props.getProperty("mongodb.port"))),credential,mongoClientOptionsBuilder.build());
        } else {
            mongoClient = new MongoClient(new ServerAddress(props.getProperty("mongodb.server"), Integer.parseInt(props.getProperty("mongodb.port"))), mongoClientOptionsBuilder.build());
        }

        final boolean[] databaseExist = {false};
        mongoClient.listDatabaseNames().forEach((Block<String>) s -> {
            if (s.equals(databaseName)) {
                databaseExist[0] = true;
            }
        });

        if (!databaseExist[0] && props.containsKey("mysql.address")) {
            log.info("new MongoDB database not found. Creating it and converting old MySQL database to MongoDB");
            convertDatabase();
        }

        //We configure our Stacktrace catchers
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(stacktraceHandler));

        //Register the server specific commands
        commandHandler.addCommand("reloadplugins", new ReloadPluginsCommand(this), "Admin Commands");
        commandHandler.addCommand("load", new LoadCommand(this), "Admin Commands");
        commandHandler.addCommand("unload", new UnloadCommand(this), "Admin Commands");
        commandHandler.addCommand("setlanguage", new SetLanguageCommand(this), "Admin Commands");
        commandHandler.addCommand("reloadlanguage", new ReloadLanguagesCommand(this), "Admin Commands");


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
        log.info("LegendaryBot is now ready!");
        ready = true;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ready = false;
            for (PluginWrapper wrapper : getPluginManager().getPlugins()) {
                getPluginManager().unloadPlugin(wrapper.getPluginId());
            }
            jdaList.forEach(JDA::shutdown);

            if (restClient != null) {
                try {
                    restClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    getStacktraceHandler().sendStacktrace(e);
                }
            }
            influxDB.close();
            mongoClient.close();
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
    public MongoDatabase getMongoDatabase() {
        return mongoClient.getDatabase(databaseName);
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

    private void convertDatabase() {
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
        HikariDataSource dataSource = new HikariDataSource(config);

        try {
            Connection connection = dataSource.getConnection();
            log.info("Converting guild_config table to guild collection");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM guild_config");
            MongoCollection<Document> mongoCollection = getMongoDatabase().getCollection("guild");
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                if (set.getString("configName").equals("")) {
                    continue;
                }
                String value = set.getString("configValue");
                if (set.getString("configName").equals("WOW_SERVER_NAME")) {
                    value = value.toLowerCase();
                }
                mongoCollection.updateOne(eq("guild_id", set.getString("guildId")),set("settings." + set.getString("configName"),value), new UpdateOptions().upsert(true));

            }
            set.close();
            statement.close();
            log.info("Converting guild_commands");
            statement = connection.prepareStatement("SELECT * FROM guild_commands");
            set = statement.executeQuery();
            while (set.next()) {
                if (set.getString("command_name").equals("")) {
                    continue;
                }
                mongoCollection.updateOne(eq("guild_id", set.getString("guild_id")),set("customCommands." + set.getString("command_name"),set.getString("text")), new UpdateOptions().upsert(true));
            }
            set.close();
            statement.close();
            mongoCollection = getMongoDatabase().getCollection("wowCharacters");

            log.info("Converting legendarycheck");
            statement = connection.prepareStatement("SELECT * FROM legendarycheck");
            set = statement.executeQuery();
            while (set.next()) {
                Document document = new Document("name", set.getString("playerName"))
                        .append("realm", set.getString("serverName"))
                        .append("region", set.getString("region"))
                        .append("lastUpdate", set.getLong("lastModified"));
                mongoCollection.insertOne(document);
            }
            set.close();
            statement.close();

            log.info("Converting user_characters");
            statement = connection.prepareStatement("SELECT * FROM user_characters");
            set = statement.executeQuery();
            while (set.next()) {
                mongoCollection.updateOne(
                        and(
                                eq("name",set.getString("characterName")),
                                eq("region", set.getString("region")),
                                eq("realm", set.getString("realmName").toLowerCase())
                        ),
                        and(
                                set("guild", set.getString("guildName")),
                                set("owner", set.getString("user_id"))
                        ),
                        new UpdateOptions().upsert(true));
            }
            set.close();
            statement.close();

            log.info("Converting user_characters_guild");
            statement = connection.prepareStatement("SELECT * FROM user_characters_guild");
            set = statement.executeQuery();
            while (set.next()) {
                String guildId = set.getString("guild_id");

                MongoCollection<Document> mongoCollectionGuild = getMongoDatabase().getCollection("guild");
                Document guildDocument = mongoCollectionGuild.find(eq("guild_id", guildId)).first();
                Document settingsDocument = (Document) guildDocument.get("settings");
                String region = settingsDocument.getString("WOW_REGION_NAME");
                String realm = settingsDocument.getString("WOW_SERVER_NAME");
                if (region != null && realm != null) {
                    realm = realm.toLowerCase();
                    mongoCollection.updateOne(
                            and(
                                    eq("realm", realm),
                                    eq("region", region),
                                    eq("name", set.getString("characterName"))
                            ),
                            set("guild_id", set.getString("guild_id"))
                    );
                }
            }
            set.close();
            statement.close();
            connection.close();
            dataSource.close();

            log.info("Convert done. LegendaryBot is now using the MongoDB database.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
