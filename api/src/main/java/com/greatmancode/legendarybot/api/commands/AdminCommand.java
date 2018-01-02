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
package com.greatmancode.legendarybot.api.commands;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;

/**
 * Represents a {@link Command} that can only be run by Guild administrators, guild members with the legendarybot-admin rank or the bot creator itself.
 */
public abstract class AdminCommand implements Command {

    @Override
    public boolean canExecute(Member member) {
        if (member.isOwner() || member.getUser().getId().equals("95709957629939712")) {
            return true;
        }
        for (Role role : member.getRoles()) {
            if (role.hasPermission(Permission.ADMINISTRATOR) || role.getName().equals("legendarybot-admin")) {
                return true;
            }
        }
        return false;
    }
}
