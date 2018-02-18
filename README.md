[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e39be91f74de4ea48d35bc95d9508f5e)](https://www.codacy.com/app/greatman/legendarybot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=greatman/legendarybot&amp;utm_campaign=Badge_Grade)

[![Discord Bots](https://discordbots.org/api/widget/status/267134720700186626.svg)](https://discordbots.org/bot/267134720700186626)
[![Discord Bots](https://discordbots.org/api/widget/servers/267134720700186626.svg)](https://discordbots.org/bot/267134720700186626)

# Legendarybot

Originally a Discord bot for alerting of new Legendary drops in a World of Warcraft Guild, it became a fully pledged bot offering multiple features:

- Lookup an item/Achievement (!lookupitem !lookupachievement)
- Mythic+ affix displayer (!affix)
- Get a player's PVE progression/Mythic+ Rank/iLVL/etc. (!lookup)
- Get the current status of the buildings on Broken Shore (!legionbuilding)
- Say if an invasion is on Broken Shore (!invasion)
- Check if someone in the guild looted a new Legendary (!enablelc !disablelc !mutelc)
- Give the latest log of the guild on WarcraftLogs (!log)
- Play some music in a voice channel! (!playmusic !stopmusic !skipsong !addsong)
- Give the Competitive rank of a player in Overwatch (!owrank)
- Show the current status of a Warcraft Realm (!server)
- Give the current price of the WoW token (!token)
- Give the latest tweet of the @blizzardcs Twitter account of your region (!blizzardcs)
- Manage a streamer list on your server (!addstreamer !removestreamer !streamers). Supports Twitch & Mixer.
- Sync your World of Warcraft Guild rank to your Discord server. (!setwowrank !syncguild and more)
- Have custom commands for your server (!createcmd !removecmd !listcommands)
- Search gifs (!gif)

## Initial Bot configuration

To be able to work properly, the bot needs some information from you. Use the !setup command to configure the bot.

## Permissions

Legendarybot have several Admin commands. To be able to run those admin commands, you need one of the following conditions:
1. You are Owner of the Discord server.
2. You need the Admin role on your server.
3. You need the ```legendarybot-admin``` role.

Without one of those conditions, you are only allowed **public** commands.

## Custom prefix
The default bot prefix is !. You can change it to whatever you want with the ```!setup``` command.

## How to compile

To compile Legendarybot, simply have JDK 9 & Maven installed and run the following command at the root of the project
```
.\gradlew clean assemble installDist copyFiles
```
This will create a folder called ``dist`` that will include the bot and all plugins

## How to run

To run the bot, it requires some basic configuration

### Prerequisites

1. You need a MongoDB server.
2. You need a ElasticSearch server for the search capabilities of the bot (Realm finding, items, achivements, etc.).
2. You need a Discord bot token. You need to create one here: https://discordapp.com/developers/applications/me
3. You need a Battle.net API key. You can create one here: https://dev.battle.net/
4. Optional: A Twitter API key for the !blizzardcs command. You can create one here: https://apps.twitter.com/
5. Optional: A WarcraftLogs API key for the !logs command. You can find yours here: https://www.warcraftlogs.com/accounts/changeuser
6. Optional: A Twitch API Key for the !streamers command. You can create an application here: https://dev.twitch.tv/dashboard/apps


Create a app.properties file at the root of the bot folder, the following needs to be added:
```
mongodb.server=localhost #Your MongoDB server address
mongodb.port=27017 #Your MongoDB server port
mongodb.database=legendarybot #Your MongoDB server port.
mongodb.username= #Optional: Your MongoDB username
mongodb.password= #Optional: Your MongoDB password
mongodb.ssl=False #Optional: True if you want to connect with SSL, False if not
bot.token=Your Discord bot token
battlenet.us.key=KEY #Your battle.net API Key
battlenet.us.secret= #Your battle.net API Secret
battlenet.eu.key= #Your battle.net API key. You can set the same as the US one.
battlenet.eu.key= #Your battle.net API secret. You can set the same as the US one.
elasticsearch.address=localhost #Your ElasticSearch server address
elasticsearch.port=9200 #Your ElasticSearch server port
elasticsearch.scheme=http #Your ElasticSearch scheme. By default http.
battlenetoauth.key= #Your battle.net API key that will be used for the !linkwowchars command. Can be the same as your other battle.net key.
battlenetoauth.secret= #Your battle.net API Secret that will be used for the !linkwowchars command. Can be the same as your other battle.net key.
warcraftlogs.key= #Your WarcraftLogs API Key for the !logs command to work.
twitter.key= #Your Twitter API key to have the !blizzardcs command work.
twritter.secret= #Your Twitter API Secret to have the !blizzardcs command work.
sentry.key = #Optional: The bot have Sentry.io integration. If you wish to upload the stacktraces to the Sentry.io platform enter your key here
bot.shard= #Optional: The number of shards the bot will have on Discord. Only recommended to put it if over 800 discord servers use the bot.
twitch.key = #Your Twitch API key to query if a stream is online or not for the !streamers command.
```

You are now ready to start the bot, start it with the following command while being in the LegendaryBot folder:
```
bin\server
```

##Loading the ElasticSearch database with data

Several features of the bot use the ElasticSearch server to query for data like realm name, items, etc. By default it is empty. For now, you can download the backup of the live database [here](https://github.com/greatman/legendarybot/files/1734877/backup.zip).
To import it in ElasticSearch, download [elasticdump](https://www.npmjs.com/package/elasticdump) and use the following command: 
```
elasticdump --input backup.json --output=http://localhost:9200/wow
```

In the future the database will be populated on the first run of the bot.
## It looks hard! Can you run it for me?

Of course! You can add the bot to your server. Simply [click here](https://discordapp.com/oauth2/authorize?client_id=267134720700186626&scope=bot&permissions=19456). 

## I need support, how can I reach you?

Simply [open a ticket](https://github.com/greatman/legendarybot/issues) and I will gladly answer! You can also join the [Discord](https://discord.gg/Cr7G28H) server!

## I wish LegendaryBot spoke my language!

LegendaryBot support multiple languages. To see if the bot supports it, check the ```!setlanguage``` command.

The language you want is missing or you saw a mistake? You are in luck! You can contribute to the [Crowdin Project](https://crowdin.com/project/legendarybot) and your language will be available to use by everybody once it get's added to bot files.