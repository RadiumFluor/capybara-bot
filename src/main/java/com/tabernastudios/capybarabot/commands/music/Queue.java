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
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.data.DataObject;

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

        MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        ConcurrentLinkedDeque<AudioTrack> queue = musicController.scheduler.queue;
        final AudioTrack nowPlaying = musicController.audioPlayer.getPlayingTrack();
        int pageSize = 10, currentPage = 1;
        int numPages = (int) Math.ceil((double) queue.size() / pageSize);
        int pageNumber = Math.max(1, Math.min(currentPage, numPages));

        if (queue.isEmpty() && nowPlaying == null) {
            event.reply(warningMessage.addContent("A fila está vazia no momento!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        event.deferReply().queue();

        final List<AudioTrack> trackList = new ArrayList<>(queue);
        final int trackCount = Math.min(queue.size(), 9);
        final EmbedBuilder queueEmbed = new EmbedBuilder();
        final AudioTrackInfo nowPlayingInfo = nowPlaying.getInfo();

        queueEmbed
                .setAuthor("📼 Fila atual:")
                .setColor(Color.getHSBColor(280,75,96))
                .setTitle("**>** "+nowPlayingInfo.title, nowPlayingInfo.uri)
                .addField(new MessageEmbed.Field("⌛ Duração",
                        "`"+TimeFormatting.formatTime(musicController.scheduler.queueDuration)+"`", true))
                .addField(new MessageEmbed.Field("💽 Total de faixas",
                        "`"+ (trackList.size()+1) + "` faixas.",
                        true))
                .addField(new MessageEmbed.Field(":repeat: Repetir",
                        "`"+musicController.scheduler.repeat+"`",false));


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
            queueEmbed.appendDescription("_E mais " + remainingTracks + " faixa(s)_");
        }

        boolean disablePrev = false, disableNext = false;

        if (currentPage == 1) {
            disablePrev = true;
        }

        if (currentPage >= numPages) {
            disableNext = true;
        }


        String pageCounter = currentPage + " de " + Math.max(numPages, 1);
        event.getHook().editOriginalEmbeds(queueEmbed.build())
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
