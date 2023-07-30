package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sun.jdi.Field;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.awt.*;
import java.text.Format;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NowPlaying extends SlashCommand {

    public NowPlaying() {
        this.name = "tocando";
        this.help = "Veja qual faixa está sendo reproduzida!";
    }

    @Override
    protected void execute(SlashCommandEvent event) {


        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        final AudioPlayer player = musicController.audioPlayer;
        final AudioTrack track = player.getPlayingTrack();

        if (track == null || !selfVoiceState.inAudioChannel()) {
            event.reply(warningMessage.addContent("Não há nenhuma faixa tocando no momento!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final AudioTrackInfo info = track.getInfo();

        Pattern pattern = Pattern.compile("v=([\\w-]+)");
        Matcher matcher = pattern.matcher(info.uri);
        String videoID = null;

        if (matcher.find()) {
            videoID = matcher.group(1);
        }


        EmbedBuilder nowPlayingEmbed =
                new EmbedBuilder()
                        .setColor(Color.getHSBColor(24, 78, 81))
                        .setTitle("► " + info.title, info.uri)
                        .addField(new MessageEmbed.Field("⌛ Duração",
                                "`" + formatTime(track.getPosition()) + " / " + formatTime(track.getDuration()) + "`",
                                true))
                        .addField(new MessageEmbed.Field("👥 Por",
                                info.author, true))
                        .setImage("https://img.youtube.com/vi/"+ videoID +"/hqdefault.jpg")
                ;


        event.replyEmbeds(nowPlayingEmbed.build()).queue();

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
