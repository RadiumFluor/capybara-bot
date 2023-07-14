package com.tabernastudios.capybarabot.commands.owner;

import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;

public class Shutdown extends SlashCommand {

    public Shutdown() {
        this.name = "desligar";
        this.help = "Desliga o bot.";
        this.ownerCommand = true;
        this.category = new Category("Owner");
    }

    @Override
    protected void execute(SlashCommandEvent event) {

        event.reply(":octagonal_sign: **`> Está ficando escuro, está ficando frio e confuso...`**").queue();

        try {
            Thread.sleep(1000); // Aguarda 1 segundo
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        event.getJDA().shutdown();
        System.exit(0);

    }
}
