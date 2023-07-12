package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class Leave extends SlashCommand {

    public Leave() {
        this.name = "sair";
        this.help = "Saio no canal de voz que você está.";
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        MessageCreateAction warningMessage = event.getTextChannel().sendMessage(":warning:")
                .addContent(" **`> ");

        event.reply(":arrows_counterclockwise: **`> Desconectando do canal...`**").queue();

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            event.getHook().editOriginal(warningMessage.addContent("Eu não estou em nenhum canal de voz!")
                    .addContent("`**")
                    .getContent()).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();


        if (!userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.getHook().editOriginal(warningMessage.addContent("Precisamos estar no mesmo canal!")
                    .addContent("`**")
                    .getContent()).queue();
            return;
        }

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());

        musicController.scheduler.stop();

        final AudioManager audioManager = event.getGuild().getAudioManager();

        VoiceChannel voiceChannel = audioManager.getConnectedChannel().asVoiceChannel();
        audioManager.closeAudioConnection();

        event.getHook().editOriginal(":mute: **`> Desconectado!`** " + voiceChannel.getAsMention()).queue();

    }
}
