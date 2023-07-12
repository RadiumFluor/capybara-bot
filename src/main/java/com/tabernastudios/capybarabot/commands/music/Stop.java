package com.tabernastudios.capybarabot.commands.music;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

public class Stop extends SlashCommand {

    public Stop() {
        this.name = "parar";
        this.help = "Para de reproduzir a fila e a limpa.";
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



        musicController.scheduler.stop();

        event.reply(":stop_button: **`> A reprodução foi parada e a fila foi limpa!`**").queue();

    }
}
