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
package com.greatmancode.legendarybot.plugin.music;

import com.greatmancode.legendarybot.api.plugin.LegendaryBotPlugin;
import com.greatmancode.legendarybot.plugin.music.commands.AddSongCommand;
import com.greatmancode.legendarybot.plugin.music.commands.PlayMusicCommand;
import com.greatmancode.legendarybot.plugin.music.commands.SkipSongCommand;
import com.greatmancode.legendarybot.plugin.music.commands.StopMusicCommand;
import com.greatmancode.legendarybot.plugin.music.music.MusicManager;
import ro.fortsoft.pf4j.PluginException;
import ro.fortsoft.pf4j.PluginWrapper;

public class MusicPlugin extends LegendaryBotPlugin {

    private MusicManager musicManager = new MusicManager();
    public MusicPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() throws PluginException {
        getBot().getCommandHandler().addCommand("addsong", new AddSongCommand(this));
        getBot().getCommandHandler().addCommand("playmusic", new PlayMusicCommand(this));
        getBot().getCommandHandler().addCommand("skipsong", new SkipSongCommand(this));
        getBot().getCommandHandler().addCommand("stopmusic", new StopMusicCommand(this));
        log.info("Music plugin loaded! Added commands !addsong, !playmusic, !skipsong, !stopmusic");
    }

    @Override
    public void stop() throws PluginException {
        getMusicManager().getPlayerManager().shutdown();
        getBot().getCommandHandler().removeCommand("addsong");
        getBot().getCommandHandler().removeCommand("playmusic");
        getBot().getCommandHandler().removeCommand("skipsong");
        getBot().getCommandHandler().removeCommand("stopmusic");
        log.info("Music plugin unloaded! Removed commands !addsong, !playmusic, !skipsong, !stopmusic");
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }
}
