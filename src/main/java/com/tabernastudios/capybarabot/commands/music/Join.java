package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageActivity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class Join extends SlashCommand {


    public Join() {
        this.name = "entrar";
        this.help = "Entro no canal de voz que você está para tocar faixas";

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void execute(SlashCommandEvent event) {


        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        event.reply(":arrows_counterclockwise: **`> Conectando ao seu canal de voz...`**").queue();

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (selfVoiceState.inAudioChannel()) {
            event.getHook().editOriginal(warningMessage.addContent("Eu já estou em um canal de voz!")
                    .addContent("`**")
                    .getContent()).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();

        if (!userVoiceState.inAudioChannel()) {
            event.getHook().editOriginal(warningMessage.addContent("Você precisa estar em um canal de voz!")
                    .addContent("`**")
                    .getContent()).queue();
            return;
        }

        if (userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.getHook().sendMessage(warningMessage.addContent("Já estamos no mesmo canal de voz!")
                    .addContent("`**")
                    .getContent()).setEphemeral(true).queue();;
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel voiceChannel = userVoiceState.getChannel().asVoiceChannel();

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());

        try {
            musicController.scheduler.announceChannel = event.getTextChannel();

            audioManager.openAudioConnection(voiceChannel);

            event.getHook().editOriginal(":sound: **`> Conectado!`** " + voiceChannel.getAsMention()).queue();
        } catch (IllegalStateException exception) {
            event.getHook().editOriginal(":no_entry: **`> Não foi possível conectar ao canal! Tente novamente mais tarde!`**").queue();
        }



    }
}
