package com.tabernastudios.capybarabot.listeners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.Main;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import com.tabernastudios.capybarabot.utils.TimeFormatting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class QueueInteraction extends ListenerAdapter {


    public static int currentPage = 1;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        ConcurrentLinkedDeque<AudioTrack> queue = musicController.scheduler.queue;
        final AudioTrack nowPlaying = musicController.audioPlayer.getPlayingTrack();

        int pageSize = 9;
        int numPages = (int) Math.ceil((double) queue.size() / pageSize);


        switch (event.getComponentId()) {
            case "previous":

                int previousPage = Math.max(1, currentPage - 1);
                currentPage = previousPage;
                break;
            case "next":
                int nextPage = Math.min(numPages, currentPage + 1);
                currentPage = nextPage;
                break;

            default:
                return;
        }
        ;

        Main.logger.info("Interação com botão: "+event.getComponentId());



        final List<AudioTrack> trackList = new ArrayList<>(queue);
        final EmbedBuilder queueEmbed = new EmbedBuilder();
        final AudioTrackInfo nowPlayingInfo = nowPlaying.getInfo();

        queueEmbed
                .setAuthor("📼 Fila atual:")
                .setColor(Color.getHSBColor(280, 75, 96))
                .setTitle("**>** " + nowPlayingInfo.title, nowPlayingInfo.uri)
                .addField(new MessageEmbed.Field("⌛ Duração",
                        "`" + TimeFormatting.formatTime(musicController.scheduler.queueDuration) + "`", true))
                .addField(new MessageEmbed.Field("💽 Total de faixas",
                        "`" + (trackList.size()+1) + "` faixas.",
                        true))
                .addField(new MessageEmbed.Field(":repeat: Repetir",
                        "`" + musicController.scheduler.repeat + "`", false));

        int pageNumber = Math.max(1, Math.min(currentPage, numPages));
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, queue.size());
        List<AudioTrack> pageTracks = new ArrayList<>(queue);
        pageTracks.subList(startIndex, endIndex);

        for (int i = startIndex; i < endIndex; i++) {
            AudioTrack track = pageTracks.get(i);
            final AudioTrackInfo info = track.getInfo();

            // Queue Builder

            queueEmbed.appendDescription("`#" + (i + 2) + "` ");
            queueEmbed.appendDescription("[**" + formatTrackTitle(info.title, info.author) + "** :: ");
            queueEmbed.appendDescription(info.author + "](" + info.uri + ") ");
            queueEmbed.appendDescription("`[" + TimeFormatting.formatTime(info.length) + "]` ");
            queueEmbed.appendDescription("\n");

        }

        if (numPages > 1 && endIndex < queue.size()) {
            int remainingTracks = queue.size() - endIndex;
            queueEmbed.appendDescription("**_E mais " + remainingTracks + " faixa(s)_**");
        }

        boolean disablePrev = false, disableNext = false;

        if (currentPage == 1) {
            disablePrev = true;
        }

        if (currentPage >= numPages) {
            disableNext = true;
        }


        String pageCounter = currentPage + " de " + Math.max(numPages, 1);
        event.editMessageEmbeds(queueEmbed.build())
                .setActionRow(
                        Button.primary("previous", "Página anterior").withDisabled(disablePrev),
                        Button.secondary("currentPage", pageCounter).withDisabled(true),
                        Button.primary("next", "Próxima página").withDisabled(disableNext)
                )
                .queue();

    }


    public String formatTrackTitle(String title, String author) {
        int maxTitleLength = 70 - author.length() - 8;
        title = title.replace(author, "").trim();

        title = title.trim();

        if (title.startsWith("- ")) {
            title = title.substring(2).trim();
        }

        if (title.endsWith(" -")) {
            title.substring(0, title.length() - 2).trim();
        }
        if (title.length() > maxTitleLength) {
            title = title.substring(0, maxTitleLength - 3) + "...";
            return title;
        }
        return title;
    }
}
