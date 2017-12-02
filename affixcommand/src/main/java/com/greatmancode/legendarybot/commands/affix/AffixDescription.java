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
package com.greatmancode.legendarybot.commands.affix;

/**
 * Represents a Affix
 */
public class AffixDescription {

    /**
     * The ID of the affix
     */
    private final int id;

    /**
     * The Description of the affix
     */
    private final String description;

    /**
     * The difficulty of an affix. The value can be 1 = easy, 2 = medium, 3 = hard
     */
    private final int difficulty;

    public AffixDescription(int id, String description, int difficulty) {
        this.id = id;
        this.description = description;
        this.difficulty = difficulty;
    }

    /**
     * Get The ID of the affix
     * @return The ID of the prefix
     */
    public int getId() {
        return id;
    }

    /**
     * Get the description of the affix
     * @return The description of the affix.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the difficulty of the affix
     * @return The Difficulty of the affix.
     */
    public int getDifficulty() {
        return difficulty;
    }
}
