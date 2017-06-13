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
package com.greatmancode.legendarybot.api;

import com.greatmancode.legendarybot.api.commands.CommandHandler;
import com.greatmancode.legendarybot.api.server.GuildSettings;
import com.mindscapehq.raygun4java.core.RaygunClient;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import ro.fortsoft.pf4j.PluginManager;

public abstract class LegendaryBot {

    private static RaygunClient raygunClient;
    private static String battlenetKey;
    public LegendaryBot(String raygunKey, String battlenetKey) {
        raygunClient = new RaygunClient(raygunKey);
        LegendaryBot.battlenetKey = battlenetKey;
    }
    public abstract CommandHandler getCommandHandler();
    public abstract GuildSettings getGuildSettings(Guild guild);
    public abstract PluginManager getPluginManager();
    public abstract HikariDataSource getDatabase();
    public abstract JDA getJDA();
    public static RaygunClient getRaygunClient() {
        return raygunClient;
    }
    public static String getBattlenetKey() {
        return battlenetKey;
    }
}
