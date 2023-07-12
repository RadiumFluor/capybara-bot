package com.tabernastudios.capybarabot.listeners;

import com.tabernastudios.capybarabot.Main;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class EventListener extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Main.logger.log(Level.INFO, "Comando >> /" + event.getName() + " -> #" + event.getChannel());
    }



    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getPresence().setActivity(Activity.listening("RadiumFluor"));
        Main.logger.fine("Cappy Bot inicializado!");
    }


}
