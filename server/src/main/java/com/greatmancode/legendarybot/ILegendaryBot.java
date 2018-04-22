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
import com.greatmancode.legendarybot.api.commands.BasicPublicZeroArgsAPICommand;
import com.greatmancode.legendarybot.api.commands.CommandHandler;
import com.greatmancode.legendarybot.api.plugin.LegendaryBotPluginManager;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.greatmancode.legendarybot.api.translate.TranslateManager;
import com.greatmancode.legendarybot.api.utils.BattleNetAPIInterceptor;
import com.greatmancode.legendarybot.api.utils.NullStacktraceHandler;
import com.greatmancode.legendarybot.api.utils.StacktraceHandler;
import com.greatmancode.legendarybot.commands.*;
import com.greatmancode.legendarybot.server.IGuildSettings;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import okhttp3.*;
import org.apache.http.HttpHost;
import org.bson.Document;
import org.elasticsearch.client.RestClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;

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

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new BattleNetAPIInterceptor(this))
            .build();

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

        //We configure our Stacktrace catchers
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(stacktraceHandler));

        //Register the server specific commands
        commandHandler.addCommand("reloadplugins", new ReloadPluginsCommand(this), "Admin Commands");
        commandHandler.addCommand("load", new LoadCommand(this), "Admin Commands");
        commandHandler.addCommand("unload", new UnloadCommand(this), "Admin Commands");
        commandHandler.addCommand("setlanguage", new SetLanguageCommand(this), "Admin Commands");
        commandHandler.addCommand("reloadlanguage", new ReloadLanguagesCommand(this), "Admin Commands");

        //Register user commands that don't need to be a plugin
        commandHandler.addCommand("legionbuilding", new BasicPublicZeroArgsAPICommand(this, "api/legionbuilding/{region}","command.legionbuilding.help", "command.legionbuilding.help"), "World of Warcraft");
        commandHandler.addCommand("blizzardcs", new BasicPublicZeroArgsAPICommand(this, "api/twitter/{region}", "command.blizzardcs.help", "command.blizzardcs.help"), "General Commands");
        commandHandler.addCommand("log", new BasicPublicZeroArgsAPICommand(this, "api/guild/{guild}/getLatestLog", "command.log.help", "command.log.help"), "World of Warcraft");

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
        jdaList.forEach((jda) -> jda.getGuilds().forEach(guild -> {
            guildSettings.put(guild.getId(), new IGuildSettings(guild, this));
            if (Boolean.parseBoolean((String) props.getOrDefault("convertConfig", "false"))) {
                new SettingsConverter(this,guild);
            }

        }));
        if (Boolean.parseBoolean((String) props.getOrDefault("convertConfig", "false"))) {


            //We convert default characters to the new system
            MongoCollection<Document> collection = getMongoDatabase().getCollection("wowCharacters");
            MongoCollection<Document> guildCollection = getMongoDatabase().getCollection("guild");
            JSONArray mainCharacters = new JSONArray();
            collection.find(exists("guild_id")).forEach((Block<Document>) document -> {
                if (document.containsKey("owner")) {
                    Document guildDocument = guildCollection.find(eq("guild_id",document.getString("guild_id"))).first();
                    System.out.println(guildDocument);
                    JSONObject characterObject = new JSONObject();
                    characterObject.put("name", document.getString("name"));
                    characterObject.put("realm", document.getString("realm"));
                    characterObject.put("region", document.getString("region"));
                    characterObject.put("owner", Long.parseLong(document.getString("owner")));
                    characterObject.put("guild_id", Long.parseLong(document.getString("guild_id")));
                    characterObject.put("guildName", ((Document)guildDocument.get("settings")).getString("GUILD_NAME"));
                    mainCharacters.put(characterObject);
                }

            });

            //We save it to the backend
            HttpUrl url = new HttpUrl.Builder().scheme("https")
                    .host(getBotSettings().getProperty("api.host"))
                    .addPathSegments("api/user/rawCharacter")
                    .build();
            Request request = new Request.Builder().url(url).addHeader("x-api-key", getBotSettings().getProperty("api.key")).post(RequestBody.create(MediaType.parse("text/plain"), mainCharacters.toString())).build();
            try {
                OkHttpClient httpClient = new OkHttpClient.Builder().build();
                System.out.println(httpClient.newCall(request).execute());
            } catch (IOException e) {
                e.printStackTrace();
            }


            props.setProperty("convertConfig", "false");
            props.store(new FileWriter("app.properties"),null);
        }



        //We load all plugins
        pluginManager.loadPlugins();
        pluginManager.startPlugins();
        translateManager = new TranslateManager(this);
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
    public Properties getBotSettings() {
        return props;
    }

    @Override
    public OkHttpClient getBattleNetHttpClient() {
        return client;
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
