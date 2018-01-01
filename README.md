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

1. You need a MySQL server with a username/password/database prepared for the bot.
2. You need a Discord bot token. You need to create one here: https://discordapp.com/developers/applications/me
3. You need a Battle.net API key. You can create one here: https://dev.battle.net/

Create a app.properties file at the root of the bot folder, the following needs to be added:
```
mysql.address=Your MySQL host
mysql.port=Your MySQL port (usually 3306)
mysql.user=Your MySQL user
mysql.password=Your MySQL pasword
mysql.database=Your MySQL database
bot.token=Your Discord bot token
battlenet.key=Your Battle.net public API key
```

You are now ready to start the bot, start it with the following command while being in the LegendaryBot folder:
```
bin\server
```

## It looks hard! Can you run it for me?

Of course! You can add the bot to your server. Simply [click here](https://discordapp.com/oauth2/authorize?client_id=267134720700186626&scope=bot&permissions=19456). 

## I need support, how can I reach you?

Simply [open a ticket](https://github.com/greatman/legendarybot/issues) and I will gladly answer! You can also join the [Discord](https://discord.gg/Cr7G28H) server!