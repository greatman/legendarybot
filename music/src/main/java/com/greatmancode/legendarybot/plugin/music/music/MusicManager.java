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
package com.greatmancode.legendarybot.plugin.music.music;

import com.greatmancode.legendarybot.api.LegendaryBot;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.*;

/**
 * The Music Manager. Handles all the loading of the songs.
 */
public class MusicManager {

    /**
     * The AudioPlayerManager instance.
     */
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    /**
     * Contains all the music manager instances per guild.
     */
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    private LegendaryBot bot;
    public MusicManager(LegendaryBot bot) {
        this.bot = bot;
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    /**
     * Get a Map of all guild music managers.
     * @return A Map containing all {@link GuildMusicManager} for all connected guilds.
     */
    public synchronized Map<Long,GuildMusicManager> getGuildsMusicManager() {
        return Collections.unmodifiableMap(musicManagers);
    }

    /**
     * Retrieve a GuildMusicManager for a specific guild
     * @param guild The Guild to retrieve the music manager for.
     * @return The {@link GuildMusicManager} instance for a guild.
     */
    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager,guild));

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    /**
     * Load a song
     * @param channel The channel to send the alert in.
     * @param trackUrl The song URL
     * @param voiceChannel the voice channel to play the music in.
     */
    public void loadAndPlay(final TextChannel channel, final String trackUrl, VoiceChannel voiceChannel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, voiceChannel);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, firstTrack, voiceChannel);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    /**
     * Stop the music for a guild.
     * @param channel The channel to send the alert in.
     */
    public void stopMusic(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.stopAll();
        musicManager.player.stopTrack();
        channel.getGuild().getAudioManager().closeAudioConnection();
        channel.sendMessage("Music stopped!").queue();
    }

    /**
     * Play a Audio Track.
     * @param guild The guidl to play the music in.
     * @param musicManager The Guild Music Manager
     * @param track The track to play
     * @param voiceChannel The voice channel to play in.
     */
    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, VoiceChannel voiceChannel) {
        connectToVoiceChannel(guild.getAudioManager(), voiceChannel);
        if (bot.getGuildSettings(guild).getSetting("MUSIC_VOLUME") != null) {
            musicManager.player.setVolume(Integer.parseInt(bot.getGuildSettings(guild).getSetting("MUSIC_VOLUME")));
        }

        musicManager.scheduler.queue(track);
    }

    /**
     * Skip a track and go to the next in the queue.
     * @param channel The channel to send the alert in.
     */
    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    /**
     * Connect the bot to a voice channel
     * @param audioManager The AudioManager of a guild
     * @param voiceChannel The voice channel to connect in.
     */
    private static void connectToVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
        audioManager.setSelfMuted(false);
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            audioManager.openAudioConnection(voiceChannel);

        }
    }

    /**
     * Check if the bot is connected in a guild voice channel.
     * @param guild The guild to check
     * @return True if the bot is in a voice channel, else false.
     */
    public boolean isConnected(Guild guild) {
        return guild.getAudioManager().isConnected();
    }


    /**
     * Add a song to the queue of a guild
     * @param guild The guild to add the song in
     * @param channel The channel to send the alert in.
     * @param url The URL of the song.
     */
    public void addMusicOnly(Guild guild, MessageChannel channel, String url) {
        getPlayerManager().loadItemOrdered(getPlayerManager(), url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                getGuildAudioPlayer(guild).scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                getGuildAudioPlayer(guild).scheduler.queue(firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + url).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    /**
     * Get the global AudioPlayerManager
     * @return An instance of the {@link AudioPlayerManager}
     */
    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }
}
