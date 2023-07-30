package com.tabernastudios.capybarabot.listeners;

import com.tabernastudios.capybarabot.Main;
import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import com.tabernastudios.capybarabot.audio.TrackScheduler;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.internal.entities.GuildVoiceStateImpl;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EventListener extends ListenerAdapter {

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> leaveTask;

    public EventListener() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Main.logger.log(Level.INFO, "Comando >> /" + event.getName() + " -> #" + event.getChannel());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getPresence().setActivity(Activity.listening("RadiumFluor"));
        Main.logger.fine("Cappy Bot inicializado!");
    }

    @Override
    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {

        Member bot = event.getGuild().getSelfMember();
        final MusicController musicController = PlayerManager.getInstance().getMusicController(event.getGuild());
        TextChannel announceChannel = musicController.scheduler.announceChannel;

        if (bot.getVoiceState().inAudioChannel()) {

            VoiceChannel botChannel = bot.getVoiceState().getChannel().asVoiceChannel();

                if (botChannel.getMembers().size() > 1) {

                    if (leaveTask != null) {
                        leaveTask.cancel(false);
                        Main.logger.warning("leaveTask cancelada!");
                    }

                } else {
                    LeaveChannelRunnable leaveRunnable = new LeaveChannelRunnable(botChannel);
                    leaveTask = scheduler.schedule(leaveRunnable, 5, TimeUnit.MINUTES);
                    announceChannel.sendMessage(":waning: **`Irei me desconectar do canal de voz em 5 minutos pois não há ninguém nele! Para evitar isso, entre no canal de voz.`**").queue();
                    Main.logger.warning("leaveTask iniciada!");
                }
        }


    }

}
