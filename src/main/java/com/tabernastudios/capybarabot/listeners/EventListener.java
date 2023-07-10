package com.tabernastudios.capybarabot.listeners;

import com.tabernastudios.capybarabot.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.Presence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventListener extends ListenerAdapter {

    public final static Logger LOG = LoggerFactory.getLogger(EventListener.class);

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        event.getGuild().getJDA().getPresence().setActivity(Activity.listening("RadiumFluor"));
        LOG.info("Comando %s usado.", event.getName());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getPresence().setActivity(Activity.listening("RadiumFluor"));
        LOG.info("Inicializado totalmente.");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListener.class);

}
