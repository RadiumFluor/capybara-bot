package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class Pause extends SlashCommand {

    public Pause() {
        this.name = "pausar";
        this.help = "Pausa/despausa a faixa.";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            event.reply(warningMessage.addContent("Eu não estou em um canal de voz no momento!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();

        if (!userVoiceState.inAudioChannel()) {
            event.reply(warningMessage.addContent("Você precisa estar no meu canal de voz!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        if (!userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.reply(warningMessage.addContent("Não estamos no mesmo canal de voz!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        final AudioPlayer audioPlayer = musicController.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            event.reply(warningMessage.addContent("Não há nenhuma faixa tocando no momento!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        boolean togglePause = !musicController.scheduler.player.isPaused();

        musicController.scheduler.setPause(togglePause);

        if (togglePause) {
            event.reply(":pause_button: **`> Faixa pausada!`**").queue();
        } else {
            event.reply(":arrow_forward: **`> Faixa despausada!`**").queue();
        }

    }
}
