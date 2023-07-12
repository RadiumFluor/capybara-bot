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
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.Collections;

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

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        String argument = event.optString("valor");

        if (argument == null) {
            event.reply(warningMessage.addContent("Você não especificou uma faixa para busca!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

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


        musicController.scheduler.repeat = argument;
        String emoji = null;

        switch (argument) {
            case "NENHUM":
                emoji = ":fast_forward:";
                        break;
            case "ATUAL":
                emoji = ":repeat_one:";
                break;
            case "TODOS":
                emoji = ":repeat:";
                break;

        }

        event.reply(emoji + " **`> Modo de repetição definido para "+ event.getOption("valor") + "`**").queue();

    }
}
