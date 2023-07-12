package com.tabernastudios.capybarabot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.tabernastudios.capybarabot.utils.TimeFormatting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
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


    public void loadAndPlay(TextChannel channel, String trackURL, User member) {
        final MusicController musicController = this.getMusicController(channel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicController, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicController.scheduler.addQueue(track, member, true);

            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

                final List<AudioTrack> trackList = playlist.getTracks();


                if (playlist.isSearchResult()) {

                    AudioTrack track = trackList.get(0);
                    musicController.scheduler.addQueue(track, member, true);

                } else {
                    long totalDuration = 0;
                    for (final AudioTrack track : trackList) {
                        totalDuration += track.getDuration();
                        musicController.scheduler.addQueue(track, member, false);
                    }

                    EmbedBuilder addedToQueue =
                            new EmbedBuilder()
                                    .setAuthor("📥 Adicionadas à fila:")
                                    .setColor(Color.HSBtoRGB(280,75,96))
                                    .setTitle(playlist.getName(), trackURL)
                                    .addField(new MessageEmbed.Field("⌛ Duração",
                                            TimeFormatting.formatTime(totalDuration),
                                            true))
                                    .addField(new MessageEmbed.Field("💽 Total de faixas",
                                            playlist.getTracks().size() + " faixas", true))
                                    .addField(new MessageEmbed.Field("📂 Adicionado por",
                                            member.getAsMention(), false));

                    musicController.scheduler.announceChannel.sendMessageEmbeds(addedToQueue.build()).queue();

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
