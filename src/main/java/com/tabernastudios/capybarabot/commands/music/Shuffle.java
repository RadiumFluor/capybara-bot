package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class Shuffle extends SlashCommand {

    public Shuffle() {
        this.name = "aleatorio";
        this.help = "Organiza a fila atual de maneira aleatória";
    }
    @Override
    protected void execute(SlashCommandEvent event) {

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        if (!selfVoiceState.inAudioChannel()) {
            event.reply(warningMessage.addContent("Eu preciso estar num canal de voz!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();

        if (!userVoiceState.inAudioChannel()) {
            event.reply(warningMessage.addContent("Você precisa estar num canal de voz!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        if (!userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.reply(warningMessage.addContent("Precisamos estar no mesmo canal de voz!")
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

        musicController.scheduler.shuffleQueue();
        event.reply(":twisted_rightwards_arrows:  **`> Fila embaralhada!`**").queue();
    }
}
