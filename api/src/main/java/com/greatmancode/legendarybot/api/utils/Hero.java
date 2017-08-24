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

package com.greatmancode.legendarybot.api.utils;

/**
 * Represents a World of Warcraft character
 */
public class Hero {


    /**
     * The Character name
     */
    private final String name;
    /**
     * The Character class
     */
    private final HeroClass heroClass;
    /**
     * The Character level (Currently goes from 1 to 110)
     */
    private final long level;

    /**
     * The Bag iLvl
     */
    private final long ilvl;

    /**
     * The equipped iLvl
     */
    private final long equipilvl;

    /**
     * Instantiate a World of Warcraft character
     * @param name The Character name
     * @param heroClass The Character class
     * @param level The Character level
     * @param ilvl The character bag ilvl
     * @param equipilvl The character equipped iLvl
     */
    public Hero(String name, HeroClass heroClass, long level, long ilvl, long equipilvl) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = level;
        this.ilvl = ilvl;
        this.equipilvl = equipilvl;
    }

    /**
     * Returns the name of the Character
     * @return the Name of the Character
     */
    public String getName() {
        return name;
    }

    /**
     * Return the Class of the Character
     * @return a {@link HeroClass} of the Character
     */
    public HeroClass getHeroClass() {
        return heroClass;
    }

    /**
     * Return the level of the character
     * @return The lever of the character. Currently being between 1 and 110.
     */
    public long getLevel() {
        return level;
    }

    /**
     * Return the bag iLvl of a Character.
     * @return The bag iLvl of a Character
     */
    public long getIlvl() {
        return ilvl;
    }

    /**
     * Return the equipped iLvl of a Character.
     * @return The equipped iLvl of a Character
     */
    public long getEquipilvl() {
        return equipilvl;
    }
}