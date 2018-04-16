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

package com.greatmancode.legendarybot.api.server;

/**
 * Settings for a specific Discord Guild
 */
public interface GuildSettings {

    /**
     * Retrieve the World of Warcraft server name of the Discord guild.
     * @return the server name of the guild
     */
    String getWowServerName();

    /**
     * Retrieve the Battle.Net region of the DiscordGuild. Usually US or EU
     * @return the region of the Guild. Usually US or EU
     */
    String getRegionName();

    /**
     * Retrieve the name of the Guild in World of Warcraft.
     * @return The Guild name in World of Warcraft
     */
    String getGuildName();

    /**
     * Retrieve a specific setting for the Guild.
     * @param setting The setting key
     * @return The setting value
     */
    String getSetting(String setting);

    /**
     * Set a specific setting for the Guild
     * @param setting The setting key
     * @param value The setting value
     */
    void setSetting(String setting, String value);

    /**
     * Remove a setting from the Guild
     * @param setting The setting key
     */
    void unsetSetting(String setting);

    /**
     * Resets the settings cache of the guild.
     */
    void resetCache();
}
