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

public class WoWCharacter {

    private final String characterName, realm, guild, region;

    private final HeroClass heroClass;

    public WoWCharacter(String characterName, String realm, String guild, String region, HeroClass heroClass) {
        this.characterName = characterName;
        this.realm = realm;
        this.guild = guild;
        this.region = region;
        this.heroClass = heroClass;
    }

    public String getCharacterName() {
        return characterName;
    }

    public String getRealm() {
        return realm;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public String getGuild() {
        return guild;
    }

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
