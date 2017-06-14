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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class Utils {

    public final static DateTime startDateMythicPlus = new DateTime(2017,3,28,0,0, DateTimeZone.forID("America/Montreal"));
    public final static String[][] mythicPlusAffixes = {
            {"Raging","Volcanic","Tyrannical"},
            {"Teeming","Explosive","Fortified"},
            {"Bolstering","Grievous","Tyrannical"},
            {"Sanguine", "Volcanic","Fortified"},
            {"Bursting", "Skittish", "Tyrannical"},
            {"Teeming","Quaking","Fortified"},
            {"Raging", "Necrotic","Tyrannical"},
            {"Bolstering", "Skittish", "Fortified"},
            {"Teeming", "Necrotic", "Tyrannical"},
            {"Sanguine","Grievous", "Fortified"},
            {"Bolstering", "Explosive", "Tyrannical"},
            {"Bursting", "Quaking", "Fortified"}
    };
}
