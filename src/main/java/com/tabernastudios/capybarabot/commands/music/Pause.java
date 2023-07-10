package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Pause extends SlashCommand {

    public Pause() {
        this.name = "pausar";
        this.help = "Pausa/despausa a faixa.";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("Eu preciso estar em um canal de voz!").setEphemeral(true).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();

        if (!userVoiceState.inAudioChannel()) {
            event.reply("Você precisa estar em um canal de voz!").setEphemeral(true).queue();
            return;
        }

        if (!userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.reply("Precisamos estar no mesmo canal de voz!").setEphemeral(true).queue();
            return;
        }

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        final AudioPlayer audioPlayer = musicController.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            event.reply("Não há nada tocando!").setEphemeral(true).queue();
            return;
        }

        boolean togglePause = !musicController.scheduler.player.isPaused();

        musicController.scheduler.player.setPaused(togglePause);

        if (togglePause) {
            event.reply("Faixa pausada!").queue();
            return;
        } else {
            event.reply("Faixa despausada!").queue();
            return;
        }

    }
}
