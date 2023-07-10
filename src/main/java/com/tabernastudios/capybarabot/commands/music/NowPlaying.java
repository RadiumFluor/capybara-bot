package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class NowPlaying extends SlashCommand {

    public NowPlaying() {
        this.name = "tocando";
        this.help = "Veja qual faixa está sendo reproduzida!";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        final AudioPlayer player = musicController.audioPlayer;
        final AudioTrack track = player.getPlayingTrack();

        if (track == null || !selfVoiceState.inAudioChannel()) {
            event.reply("Não há nenhuma faixa tocando no momento.").setEphemeral(true).queue();
            return;
        }

        final AudioTrackInfo info = track.getInfo();

        event.replyFormat("**>>** Tocando agora: `%s` de **%s** (Link: <%s>)",
                info.title,
                info.author,
                info.uri).queue();

    }
}
