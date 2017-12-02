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
package com.greatmancode.legendarybot.plugins.wowlink.utils;

import com.greatmancode.legendarybot.api.utils.HeroClass;

/**
 * Represends a Wow Character.
 */
public class WoWCharacter {

    /**
     * The Character name
     */
    private final String characterName;

    /**
     * The Character realm
     */
    private final String realm;

    /**
     * The Character guild
     */
    private final String guild;

    /**
     * The Character region
     */
    private final String region;


    /**
     * The Character class
     */
    private final HeroClass heroClass;

    /**
     * Create an instance of a WoWCharacter
     * @param characterName The Character name
     * @param realm The Character realm
     * @param guild The Character guild
     * @param region The Character region
     * @param heroClass The Character class
     */
    public WoWCharacter(String characterName, String realm, String guild, String region, HeroClass heroClass) {
        this.characterName = characterName;
        this.realm = realm;
        this.guild = guild;
        this.region = region;
        this.heroClass = heroClass;
    }

    /**
     * Get the Character name
     * @return the character name
     */
    public String getCharacterName() {
        return characterName;
    }

    /**
     * Get the Character realm.
     * @return The character realm
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Get the Character class.
     * @return The Character class
     */
    public HeroClass getHeroClass() {
        return heroClass;
    }

    /**
     * Get the Character guild.
     * @return The Character guild
     */
    public String getGuild() {
        return guild;
    }

    /**
     * Get the Character region
     * @return The Character region.
     */
    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return "WoWCharacter{" +
                "characterName='" + characterName + '\'' +
                ", realm='" + realm + '\'' +
                ", guild='" + guild + '\'' +
                ", region='" + region + '\'' +
                ", heroClass=" + heroClass +
                '}';
    }
}
