package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class Queue extends SlashCommand {

    public Queue() {
        this.name = "fila";
        this.help = "Exibe a fila de faixas a serem tocadas.";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        TextChannel textChannel = event.getChannel().asTextChannel();
        MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        ConcurrentLinkedDeque<AudioTrack> queue = musicController.scheduler.queue;

        final AudioTrack nowPlaying = musicController.audioPlayer.getPlayingTrack();

        if (queue.isEmpty() && nowPlaying == null) {
            event.reply("A fila está vazia no momento!").setEphemeral(true).queue();
            return;
        }


        final int trackCount = Math.min(queue.size(), 9);
        final List<AudioTrack> trackList = new ArrayList<>(queue);

        final AudioTrackInfo nowPlayingInfo = nowPlaying.getInfo();
        final MessageCreateAction messageActivity = textChannel.sendMessage("**FILA ATUAL:**\n");
        messageActivity.addContent("\n**>>** `")
                .addContent(nowPlayingInfo.title)
                .addContent("` de **")
                .addContent(nowPlayingInfo.author)
                .addContent("** `[")
                .addContent(formatTime(nowPlaying.getDuration() - nowPlaying.getPosition()))
                .addContent("/")
                .addContent(formatTime(nowPlaying.getDuration()))
                .addContent("]` **<<**\n");

        for (int i = 0; i < trackCount; i++) {
            final AudioTrack track = trackList.get(i);
            final AudioTrackInfo info = track.getInfo();

            messageActivity
                    .addContent("#")
                    .addContent(String.valueOf(i+1))
                    .addContent(" `")
                    .addContent(info.title)
                    .addContent("` de **")
                    .addContent(info.author)
                    .addContent("** `[")
                    .addContent(formatTime(track.getDuration()))
                    .addContent("]`\n");

            if (trackList.size() > trackCount) {
                messageActivity.addContent("E mais ")
                        .addContent(String.valueOf(trackList.size() - trackCount))
                        .addContent(" faixas...");
            }
        }
        event.reply(messageActivity.getContent()).queue();
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
