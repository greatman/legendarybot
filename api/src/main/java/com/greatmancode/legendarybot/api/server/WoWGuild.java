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

public class WoWGuild {

    private final String region, server, guild;
    private final boolean isDefault;

    public WoWGuild(String region, String server, String guild) {
        this(region,server,guild,false);
    }

    public WoWGuild(String region, String server, String guild, boolean isDefault) {
        this.region = region;
        this.server = server;
        this.guild = guild;
        this.isDefault = isDefault;
    }

    public String getRegion() {
        return region;
    }

    public String getServer() {
        return server;
    }

    public String getGuild() {
        return guild;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WoWGuild woWGuild = (WoWGuild) o;

        if (region != null ? !region.equals(woWGuild.region) : woWGuild.region != null) return false;
        if (server != null ? !server.equals(woWGuild.server) : woWGuild.server != null) return false;
        return guild != null ? guild.equals(woWGuild.guild) : woWGuild.guild == null;
    }

    @Override
    public int hashCode() {
        int result = region != null ? region.hashCode() : 0;
        result = 31 * result + (server != null ? server.hashCode() : 0);
        result = 31 * result + (guild != null ? guild.hashCode() : 0);
        return result;
    }
}
