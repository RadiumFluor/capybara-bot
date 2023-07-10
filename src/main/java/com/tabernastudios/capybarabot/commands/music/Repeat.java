package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class Repeat extends SlashCommand {

    public Repeat() {
        this.name = "repetir";
        this.help = "Liga/Desliga a reprodução em repetição";
        this.options = Collections.singletonList(
                new OptionData(
                        OptionType.STRING,
                        "valor",
                        "Se irá repetir apenas a faixa atual ou a fila inteira."
                )
                        .setRequired(true)
                        .addChoices(
                                new Command.Choice("desligar", "NENHUM"),
                                new Command.Choice("apenas atual", "ATUAL"),
                                new Command.Choice("fila inteira", "TODOS")
                        )
        );
    }



    @Override
    protected void execute(SlashCommandEvent event) {


        String argument = event.optString("valor");

        if (argument == null) {
            event.reply("Erro! Você não providenciou os argumentos necessários!").setEphemeral(true).queue();
            return;
        }

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
            event.reply("Não há faixas tocando!").setEphemeral(true).queue();
            return;
        }

        musicController.scheduler.repeat = argument;

        event.reply("Modo de repetição definido para: `"+ argument +"`").queue();

    }
}
