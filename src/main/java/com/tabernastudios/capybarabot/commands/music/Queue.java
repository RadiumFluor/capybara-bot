package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import com.tabernastudios.capybarabot.utils.TimeFormatting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.awt.*;
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

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        TextChannel textChannel = event.getChannel().asTextChannel();
        MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        ConcurrentLinkedDeque<AudioTrack> queue = musicController.scheduler.queue;
        final AudioTrack nowPlaying = musicController.audioPlayer.getPlayingTrack();

        if (queue.isEmpty() && nowPlaying == null) {
            event.reply(warningMessage.addContent("A fila está vazia no momento!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        final int trackCount = Math.min(queue.size(), 9);
        final List<AudioTrack> trackList = new ArrayList<>(queue);
        final EmbedBuilder queueEmbed = new EmbedBuilder();
        final AudioTrackInfo nowPlayingInfo = nowPlaying.getInfo();

        queueEmbed
                .setAuthor("📼 Fila atual:")
                .setColor(Color.getHSBColor(280,75,96))
                .setTitle("**>** "+nowPlayingInfo.title, nowPlayingInfo.uri)
                .addField(new MessageEmbed.Field("⌛ Duração",
                        "`"+TimeFormatting.formatTime(musicController.scheduler.queueDuration)+"`", true))
                .addField(new MessageEmbed.Field("💽 Total de faixas",
                        "`"+trackList.size() + "` faixas.",
                        true))
                .addField(new MessageEmbed.Field(":repeat: Repetir",
                        "`"+musicController.scheduler.repeat+"`",false));


        for (int i = 0; i < trackCount; i++) {
            final AudioTrack track = trackList.get(i);
            final AudioTrackInfo info = track.getInfo();

            // Queue Builder

            queueEmbed.appendDescription("`#"+ (i+2) +"` ");
            queueEmbed.appendDescription("[**"+ info.title +"** :: ");
            queueEmbed.appendDescription(info.author +"]("+ info.uri +") ");
            queueEmbed.appendDescription("`["+ TimeFormatting.formatTime(info.length) +"]` ");
            queueEmbed.appendDescription("\n");

        }

        if (trackList.size() > trackCount) {
            queueEmbed.appendDescription("_E mais "+ (trackList.size()-10) +" faixa(s)_");
        }

        event.getHook().sendMessageEmbeds(queueEmbed.build()).queue();

    }

}
