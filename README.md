# Legendarybot

Originally a Discord bot for alerting of new Legendary drops in a World of Warcraft Guild, it became a fully pledged bot offering multiple features:

- Lookup an item/Achievement (!lookupitem !lookupachievement)
- Mythic+ affix displayer (!affix)
- Mythic+ rank checker (!mplusrank)
- Give a player iLVL (!ilvl)
- Say if an invasion is on Broken Shore (!invasion)
- Check if someone in the guild looted a new Legendary (!enablelc !disablelc !mutelc)
- Give the latest log of the guild on WarcraftLogs (!log)
- Play some music in a voice channel! (!playmusic !stopmusic !skipsong !addsong)
- Give the Competitive rank of a player in Overwatch (!owrank) *Only supports US stats.*
- Show the current status of a Warcraft Realm (!server)
- Give the current price of the WoW token (!token)
- Give the latest tweet of the US @blizzardcs Twitter account (!blizzardcs)
- Manage a streamer list on your server (!addstreamer !removestreamer !streamers). Supports Twitch & Mixer.

## Initial Bot configuration

To be able to work properly, the bot needs 3 things from you:

1. Your Region ```!setserversetting WOW_REGION_NAME US/EU```
2. Your WoW Realm ```!setserversetting WOW_SERVER_NAME Arthas``` (Arthas is an example)
3. Your Guild Name ```!setserversetting GUILD_NAME YourGuildName```

Congrats! The bot is now configured for your server.

## Permissions

Legendarybot have several Admin commands. To be able to run those admin commands, you need one of the following conditions:
1. You are Owner of the Discord server.
2. You need the Admin role on your server.
3. You need the ```legendarybot-admin``` role.

Without one of those conditions, you are only allowed **public** commands.

## Custom prefix
The default bot prefix is !. You can change it to whatever you want with the following command ```!setserversetting PREFIX What_You_Want```

## How to compile

To compile Legendarybot, simply have JDK 8 & Maven installed and run the following command at the root of the project
```
mvn clean package
```
This will create the .jar and .zip for the server and every plugins in their respective **target** folder.

## How to run

To run the bot, it requires some basic configuration

### Prerequisites

1. You need a MySQL server with a username/password/database prepared for the bot.
2. You need a Discord bot token. You need to create one here: https://discordapp.com/developers/applications/me
3. You need a Battle.net API key. You can create one here: https://dev.battle.net/

This is the folder layout needed:
```
-Root
--server.jar
--app.properties
--plugins
---Every plugins .zip wanted
```
In app.properties, the following needs to be added:
```
mysql.address=Your MySQL host
mysql.port=Your MySQL port (usually 3306)
mysql.user=Your MySQL user
mysql.password=Your MySQL pasword
mysql.database=Your MySQL database
bot.token=Your Discord bot token
battlenet.key=Your Battle.net public API key
```

You are now ready to start the bot, start it with the following command:
```
java -jar server.jar
```

## It looks hard! Can you run it for me?

Of course! You can add the bot to your server. Simply [click here](https://discordapp.com/oauth2/authorize?client_id=267134720700186626&scope=bot&permissions=19456). 

## I need support, how can I reach you?

Simply [open a ticket](https://github.com/greatman/legendarybot/issues) and I will gladly answer! You also join the [Discord](https://discord.gg/Cr7G28H) server!