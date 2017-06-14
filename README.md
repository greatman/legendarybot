#Legendarybot

Originally a Discord bot for alerting of new Legendary drops in a World of Warcraft Guild, it became a fully pledged bot offering multiple features:

- Mythic+ affix displayer (!affix)
- Give a player iLVL (!ilvl)
- Say if an invasion is on Broken Shore (!invasion)
- Check if someone in the guild looted a new Legendary (!enablelc !disablelc !mutelc)
- Give the latest log of the guild on WarcraftLogs (!log)
- Play some music in a voice channel! (!playmusic !stopmusic !skipsong !addsong)
- Give the Competitive rank of a player in Overwatch (!owrank) *Only supports US stats.*
- Show the current status of a Warcraft Realm (!server)
- Give the current price of the WoW token (!token)

##How to compile

To compile Legendarybot, simply have JDK 8 & Maven installed and run the following command at the root of the project
```
mvn clean package
```
This will create the .jar and .zip for the server and every plugins in their respective **target** folder.

##How to run

To run the bot, it requires some basic configuration

###Prerequisites

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

##It looks hard! Can you run it for me?

Of course! You can add the bot to your server. Simply [click here](https://discordapp.com/oauth2/authorize?client_id=267134720700186626&scope=bot&permissions=0). 

##I need support, how can I reach you?

Simply [open a ticket](https://github.com/greatman/legendarybot/issues) and I will gladly answer!