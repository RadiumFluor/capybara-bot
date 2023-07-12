package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class Play extends SlashCommand {

    public Play() {
        this.name = "tocar";
        this.help = "Adiciona uma faixa na fila.";

        this.options = List.of(
                new OptionData(
                        OptionType.STRING,
                        "busca",
                        "Adicionar faixa pela URL ou busca pelo nome."
                ).setRequired(true),
                new OptionData(
                        OptionType.STRING,
                        "plataforma",
                        "Plataforma onde devo buscar a faixa. (Padrão: Youtube)")
                        .setRequired(false)
                        .addChoices(
                                new Command.Choice("youtube", "ytsearch:"),
                                new Command.Choice("soundcloud", "scsearch:")
                        )
        );

    }

    @Override
    protected void execute(SlashCommandEvent event) {

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        String argument = event.optString("busca");
        String choice = event.optString("plataforma");

        if (argument == null) {
            event.reply(warningMessage.addContent("Você não especificou uma faixa para busca!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();
            return;
        }

        final TextChannel channel = event.getTextChannel();
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

        String link;

        if (!isURL(argument)) {

            link = Objects.requireNonNullElse(choice, "ytsearch:") + argument;
        } else {
            link = argument;
        }


        PlayerManager.getInstance()
                .loadAndPlay(channel, link);

        event.reply(":open_file_folder: **`> Adicionando faixa(s)...`**").setEphemeral(true).queue();

    }

    private boolean isURL(String argument) {
        try {
            new URI(argument);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }

    }
}
