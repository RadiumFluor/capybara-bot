package com.tabernastudios.capybarabot.commands.general;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

import java.awt.*;

public class Ping extends SlashCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Executa uma verificação de delay no bot.";
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply().queue();
        event.getHook().sendMessage(":signal_strength: **`> Tempo de resposta: " + event.getJDA().getGatewayPing() + "ms!`**").queue();

    }
}
