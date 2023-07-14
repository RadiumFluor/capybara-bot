package com.tabernastudios.capybarabot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.Main;
import com.tabernastudios.capybarabot.utils.TimeFormatting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {


    public final AudioPlayer player;
    public final ConcurrentLinkedDeque<AudioTrack> historyQueue;
    public final ConcurrentLinkedDeque<AudioTrack> queue;
    public final Throwable onErrorHook = null;
    private AudioTrack lastTrack;
    private AudioTrack lastLoadedTrack;
    public String repeat = "NENHHUM";
    public Long queueDuration;
    public TextChannel announceChannel;
    public Message lastAnnounceMessage;



    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new ConcurrentLinkedDeque<>();
        this.historyQueue = new ConcurrentLinkedDeque<>();
        this.queueDuration = 0L;
    }

    public void addQueue(AudioTrack track, User member, boolean announce) {
        track.setUserData(member.getIdLong());
        queueDuration += track.getDuration();
        queue.add(track);

        // TODO > Adicionar o anúncio de faixa adicionada;

        final AudioTrackInfo info = track.getInfo();

        if (announceChannel != null && queue.size() > 0 && announce) {

            EmbedBuilder addedToQueue =
                    new EmbedBuilder()
                            .setAuthor("📥 Adicionado à fila:")
                            .setColor(Color.HSBtoRGB(280,75,50))
                            .setTitle(info.title, info.uri)
                            .addField(new MessageEmbed.Field("⌛ Duração",
                                    TimeFormatting.formatTime(track.getDuration()),
                                    true))
                            .addField(new MessageEmbed.Field("👥 Por",
                                    info.author, true))
                            .addField(new MessageEmbed.Field("📂 Adicionado por",
                                    member.getAsMention(), false));


            announceChannel.sendMessageEmbeds(addedToQueue.build()).complete();



        }


        if (player.isPaused()) {
            setPause(false);
        }

        if (player.getPlayingTrack() == null) {
            nextTrack();
        }

    }

    public void stopTrack() {
        this.player.stopTrack();
    }

    public void stop() {
        String lastRepeatMode = repeat;
        this.repeat = "NENHUM";
        clear();
        updateHistoryQueue();
        stopTrack();
        this.repeat = lastRepeatMode;
    }

    public void setPause(boolean pause) {
        player.setPaused(pause);
    }


    public void clear() {
        queue.clear();
        historyQueue.clear();
        queueDuration = 0L;
        this.lastTrack = null;
        setLastTrack(null);
    }

    public void shuffleQueue() {
        List<AudioTrack> trackList = new ArrayList<>(queue);
        Collections.shuffle(trackList);
        queue.clear();
        queue.addAll(trackList);
    }

    public void skip(int index) {

        if (index > 0 || index < queue.size()) {

            int currentIndex = index-1;
                if (currentIndex >= 0 && currentIndex < queue.size()) {
                    while (currentIndex > 0) {
                        queueDuration -= queue.peekFirst() != null ? queue.peekFirst().getDuration() : 0;
                        queue.removeFirst();
                        currentIndex--;
                    }
                }

        }

        trackSkipped();
        stopTrack();
    }

    public void setLastTrack(AudioTrack lastTrack) {
        this.lastTrack = lastTrack;
    }

    public void nextTrack() {
        AudioTrack audioTrack = provideAudioTrack();
        lastLoadedTrack = audioTrack;
        this.player.startTrack(audioTrack, false);
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

        // A track started playing
        // TODO > Adicionar e refinar o aviso de faixa tocando;
        if (announceChannel != null) {

            if (lastAnnounceMessage != null) {
                lastAnnounceMessage.delete().queue();
            }

            final AudioTrackInfo info = track.getInfo();
            final UserSnowflake member = User.fromId(track.getUserData(Long.class));

            EmbedBuilder nowPlayingEmbed =
                    new EmbedBuilder()
                            .setAuthor("🎧 Tocando agora:")
                            .setColor(Color.HSBtoRGB(280,75,50))
                            .setTitle(info.title, info.uri)
                            .addField(new MessageEmbed.Field("⌛ Duração",
                                    TimeFormatting.formatTime(track.getDuration()),
                                    true))
                            .addField(new MessageEmbed.Field("👥 Por",
                                    info.author, true))
                            .addField(new MessageEmbed.Field("📂 Adicionado por",
                                    member.getAsMention(), false));

            announceChannel.sendMessageEmbeds(nowPlayingEmbed.build())
                    .queueAfter(1,TimeUnit.SECONDS,message -> lastAnnounceMessage = message);

        } else { Main.logger.severe("Não foi possível enviar o anúncio de faixa!");}



    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        if (endReason == AudioTrackEndReason.FINISHED || endReason == AudioTrackEndReason.STOPPED) {
            updateHistoryQueue();
            nextTrack();
            Main.logger.info("Faixa " + track.getIdentifier() + " finalizada!" );

        } else if (endReason == AudioTrackEndReason.CLEANUP) {
            Main.logger.info("Faixa " + track.getIdentifier() + " foi limpa!" );

        } else if (endReason == AudioTrackEndReason.LOAD_FAILED) {

            if (onErrorHook != null) {
                Main.logger.warning("Faixa" + track.getIdentifier() + " falhou ao carregar!");
            }
            trackSkipped();
            nextTrack();
        } else if (endReason == AudioTrackEndReason.REPLACED) {
            Main.logger.info("Faixa " + track.getIdentifier() + " foi interrompida!" );
            trackSkipped();
            nextTrack();
        } else {
            Main.logger.warning("Faixa" + track.getIdentifier() + " terminou com erro inesperado!");
        }

        if (queue.isEmpty()) {
            announceChannel.sendMessage(":no_entry: **`> A fila está vazia! Todas as faixas foram tocadas.`**");
            return;
        }

        queueDuration -= track.getDuration();

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    private void trackSkipped() {
        lastTrack = null;
    }

    public AudioTrack provideAudioTrack() {
        if (repeat.equals("ATUAL") && lastTrack != null) {
            return lastTrack.makeClone();
        }

        if (repeat.equals("TODOS") && lastTrack != null) {
            AudioTrack clone = lastTrack.makeClone();
            queue.addLast(clone);
            queueDuration += clone.getDuration();
        }

        lastTrack = queue.poll();
        return lastTrack;
    }

    private void updateHistoryQueue() {
        setLastTrack(lastLoadedTrack);

        if (lastTrack == null) {
            return;
        }

        historyQueue.add(lastTrack);
    }


    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }



}
