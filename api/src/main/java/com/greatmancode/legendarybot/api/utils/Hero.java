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

public class Hero {


    private final String name;
    private final HeroClass heroClass;
    private final long level;
    private long ilvl;
    private long equipilvl;
    private String legendary;

    public Hero(String name, HeroClass heroClass, long level) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = level;
    }

    public Hero(String name, HeroClass heroClass, long level, long ilvl, long equipilvl, String legendary) {
        this.name = name;
        this.heroClass = heroClass;
        this.level = level;
        this.ilvl = ilvl;
        this.equipilvl = equipilvl;
        this.legendary = legendary;
    }

    public String getName() {
        return name;
    }

    public HeroClass getHeroClass() {
        return heroClass;
    }

    public long getLevel() {
        return level;
    }

    public long getIlvl() {
        return ilvl;
    }

    public long getEquipilvl() {
        return equipilvl;
    }

    public String getLegendary() {
        return legendary;
    }

    public void setIlvl(long ilvl) {
        this.ilvl = ilvl;
    }

    public void setEquipilvl(long equipilvl) {
        this.equipilvl = equipilvl;
    }
}