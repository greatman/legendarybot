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
package com.greatmancode.legendarybot.api.plugin;

import com.greatmancode.legendarybot.api.LegendaryBot;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * Represents a Plugin for the Bot
 */
public abstract class LegendaryBotPlugin extends Plugin {

    /**
     * An instance of the {@link LegendaryBot} class
     */
    private final LegendaryBot bot;

    public LegendaryBotPlugin(PluginWrapper wrapper) {
        super(wrapper);
        bot = ((LegendaryBotPluginManager)wrapper.getPluginManager()).getBot();
    }

    @Override
    public abstract void start() throws PluginException;

    @Override
    public abstract void stop() throws PluginException;

    /**
     * Retrieve the {@link LegendaryBot} instance
     * @return the {@link LegendaryBot} instance.
     */
    public LegendaryBot getBot() {
        return bot;
    }
}
