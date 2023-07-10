package com.tabernastudios.capybarabot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final Map<Long, MusicController> MusicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.MusicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);

    }

    public MusicController getMusicController(Guild guild) {
        return this.MusicManagers.computeIfAbsent(guild.getIdLong(), (guildID) -> {
           final MusicController musicController = new MusicController((this.audioPlayerManager));

           guild.getAudioManager().setSendingHandler(musicController.getSendHandler());
           return musicController;
        });
    }


    public void loadAndPlay(TextChannel channel, String trackURL) {
        final MusicController musicController = this.getMusicController(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicController, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicController.scheduler.setQueue(track);
                channel.sendMessage("Adicionado à fila: `"
                        + track.getInfo().title
                        + "` de **" + track.getInfo().author + "**.")
                        .queue();

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

                final List<AudioTrack> trackList = playlist.getTracks();


                if (playlist.isSearchResult()) {

                    AudioTrack track = trackList.get(0);
                    musicController.scheduler.setQueue(track);
                    channel.sendMessage("Adicionado à fila: `"
                                    + track.getInfo().title
                                    + "` de **" + track.getInfo().author + "**.")
                            .queue();

                } else {
                    for (final AudioTrack track : trackList) {

                        musicController.scheduler.setQueue(track);
                    }

                    channel.sendMessage("Adicionado(s) `"
                                    + String.valueOf(trackList.size())
                                    + "` faixas da playlist **"
                                    + playlist.getName()
                                    + "** à fila.")
                            .queue();
                }


            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
    }
    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}
