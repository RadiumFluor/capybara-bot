package com.tabernastudios.capybarabot.commands.general;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class Ping extends SlashCommand {

    public Ping() {
        this.name = "ping";
        this.help = "Executa uma verificação de delay no bot.";
    }

    @Override
    protected void execute(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.reply("Pong!").queue();
    }
}
