package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

public class Join extends SlashCommand {

    public Join() {
        this.name = "entrar";
        this.help = "Entro no canal de voz que você está para tocar faixas";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void execute(SlashCommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState.inAudioChannel()) {
            event.reply("Eu já estou em um canal de voz!").setEphemeral(true).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();

        if (!userVoiceState.inAudioChannel()) {
            event.reply("Você precisa estar em um canal de voz!").setEphemeral(true).queue();
            return;
        }

        if (userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.reply("Já estamos no mesmo canal de voz!").setEphemeral(true).queue();
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel voiceChannel = userVoiceState.getChannel().asVoiceChannel();

        audioManager.openAudioConnection(voiceChannel);
        event.reply("Conectando ao seu canal de voz...").queue();

    }
}
