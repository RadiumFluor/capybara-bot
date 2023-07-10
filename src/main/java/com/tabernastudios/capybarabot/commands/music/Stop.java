package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class Stop extends SlashCommand {

    public Stop() {
        this.name = "parar";
        this.help = "Para de reproduzir a fila e a limpa.";
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

        musicController.scheduler.player.stopTrack();
        musicController.scheduler.queue.clear();

        event.reply("A reprodução foi parada e a fila foi limpa!").queue();

    }
}
