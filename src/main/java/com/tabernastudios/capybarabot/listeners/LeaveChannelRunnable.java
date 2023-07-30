package com.tabernastudios.capybarabot.listeners;

import com.tabernastudios.capybarabot.audio.MusicController;
import com.tabernastudios.capybarabot.audio.PlayerManager;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class LeaveChannelRunnable implements Runnable {

    private VoiceChannel channel;

    public LeaveChannelRunnable(VoiceChannel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {

        if (channel.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {

            MusicController musicController = PlayerManager.getInstance().getMusicController(channel.getGuild());
            musicController.scheduler.repeat = "NENHUM";
            musicController.scheduler.setPause(false);
            musicController.scheduler.stop();
            musicController.scheduler.setLastTrack(null);
            musicController.scheduler.clear();

            final AudioManager audioManager = channel.getGuild().getAudioManager();
            audioManager.closeAudioConnection();

        }
    }

}
