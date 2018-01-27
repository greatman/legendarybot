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
import com.greatmancode.legendarybot.plugin.music.commands.*;
import com.greatmancode.legendarybot.plugin.music.music.MusicManager;
import org.pf4j.PluginWrapper;

/**
 * The Music plugin
 */
public class MusicPlugin extends LegendaryBotPlugin {

    /**
     * The setting to allow member to use music commands
     */
    public static String MEMBER_ALLOWED_SETTING = "musicplugin_memberallowed";

    /**
     * The setting that contains the only channel the bot is allowed to connect in.
     */
    public static String MUSIC_CHANNEL_SETTING = "musicplugin_channel";

    /**
     * The Music Manager
     */
    private MusicManager musicManager = new MusicManager(getBot());

    public MusicPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {

        getBot().getCommandHandler().addCommand("addsong", new AddSongCommand(this), "Music");
        getBot().getCommandHandler().addCommand("playmusic", new PlayMusicCommand(this), "Music");
        getBot().getCommandHandler().addCommand("skipsong", new SkipSongCommand(this), "Music");
        getBot().getCommandHandler().addCommand("stopmusic", new StopMusicCommand(this), "Music");
        getBot().getCommandHandler().addCommand("disallowmembermusic", new DisallowMemberMusicCommand(this), "Music Admin Commands");
        getBot().getCommandHandler().addCommand("allowmembermusic", new AllowMemberMusicCommand(this), "Music Admin Commands");
        getBot().getCommandHandler().addCommand("setmusicchannel", new SetMusicChannelCommand(this), "Music Admin Commands");
        getBot().getCommandHandler().addCommand("unsetmusicchannel", new UnsetMusicChannelCommand(this), "Music Admin Commands");
        getBot().getCommandHandler().addCommand("setvolume", new SetVolumeCommand(this), "Music");
        log.info("Music plugin loaded! Added commands !addsong, !playmusic, !skipsong, !stopmusic");
    }

    @Override
    public void stop() {
        getMusicManager().getPlayerManager().shutdown();
        getBot().getCommandHandler().removeCommand("addsong");
        getBot().getCommandHandler().removeCommand("playmusic");
        getBot().getCommandHandler().removeCommand("skipsong");
        getBot().getCommandHandler().removeCommand("stopmusic");
        getBot().getCommandHandler().removeCommand("disallowmembermusic");
        getBot().getCommandHandler().removeCommand("allowmembermusic");
        getBot().getCommandHandler().removeCommand("setmusicchannel");
        getBot().getCommandHandler().removeCommand("unsetmusicchannel");
        getBot().getCommandHandler().removeCommand("setvolume");
        log.info("Music plugin unloaded! Removed commands !addsong, !playmusic, !skipsong, !stopmusic");
    }

    /**
     * Retrieve the Music Manager
     * @return The Music Manager instance.
     */
    public MusicManager getMusicManager() {
        return musicManager;
    }
}
