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

public class Leave extends SlashCommand {

    public Leave() {
        this.name = "sair";
        this.help = "Saio no canal de voz que você está.";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void execute(SlashCommandEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        if (!selfVoiceState.inAudioChannel()) {
            event.reply("Eu não estou em nenhum canal!").setEphemeral(true).queue();
            return;
        }

        final Member user = event.getMember();
        final GuildVoiceState userVoiceState = user.getVoiceState();


        if (!userVoiceState.getChannel().equals(selfVoiceState.getChannel())) {
            event.reply("Precisamos estar no mesmo canal!").setEphemeral(true).queue();
            return;
        }

        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());

        musicController.scheduler.player.stopTrack();
        musicController.scheduler.queue.clear();
        musicController.scheduler.repeat = "NENHUM";
        musicController.audioPlayer.setPaused(false);

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel voiceChannel = userVoiceState.getChannel().asVoiceChannel();

        audioManager.closeAudioConnection();



        event.reply("Desconectando do canal...").queue();

    }
}
