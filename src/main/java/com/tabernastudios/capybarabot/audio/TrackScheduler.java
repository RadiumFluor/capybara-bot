package com.tabernastudios.capybarabot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {


    public final AudioPlayer player;
    public final ConcurrentLinkedDeque<AudioTrack> historyQueue;
    public final ConcurrentLinkedDeque<AudioTrack> queue;
    public final Throwable onErrorHook = null;
    private AudioTrack lastTrack;
    private AudioTrack lastLoadedTrack;
    public String repeat = "NONE";
    public TextChannel announceChannel;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new ConcurrentLinkedDeque<>();
        this.historyQueue = new ConcurrentLinkedDeque<>();
    }

    public void addQueue(AudioTrack track) {
        queue.add(track);
        // TODO > Adicionar o anúncio de faixa adicionada;

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
        clear();
        stopTrack();
    }

    public void setPause(boolean pause) {
        player.setPaused(pause);
    }


    public void clear() {
        lastTrack = null;
        queue.clear();
        historyQueue.clear();
    }

    public void skip() {
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
            final AudioTrackInfo info = track.getInfo();

            EmbedBuilder nowPlayingEmbed =
                    new EmbedBuilder()
                            .setColor(Color.getHSBColor(24, 78, 81))
                            .setTitle(info.title, info.uri)
                            .addField(new MessageEmbed.Field("⌛ Duração",
                                    formatTime(track.getDuration()),
                                    true))
                            .addField(new MessageEmbed.Field("👥 Por",
                                    info.author, true));

            announceChannel.sendMessageFormat("**>>** Tocando agora: `%s` de **%s** (Link: <%s>)",
                    track.getInfo().title,
                    track.getInfo().author,
                    track.getInfo().uri).queue();
        }



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
        }

        lastTrack = queue.poll();
        return lastTrack;
    };

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

    private String formatTime(long duration) {
        final long hours = duration / TimeUnit.HOURS.toMillis(1);
        final long minutes = duration / TimeUnit.MINUTES.toMillis(1);
        final long seconds = duration % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if (hours == 0) {
            return String.format("%02d:%02d", minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);


    }


}
