package com.tabernastudios.capybarabot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.tabernastudios.capybarabot.commands.general.Ping;
import com.tabernastudios.capybarabot.commands.music.*;
import com.tabernastudios.capybarabot.listeners.EventListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Arrays;

public class Main extends ListenerAdapter {

    public final static Logger LOG = LoggerFactory.getLogger(Main.class);
    public final static GatewayIntent[] INTENTS = {
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_PRESENCES};
    private final ShardManager shardManager;

    public Main() {

        EventWaiter waiter = new EventWaiter();
        Dotenv config = Dotenv.configure().load();

        CommandClientBuilder builder = new CommandClientBuilder();
        builder
                .addSlashCommands(
                        new Ping(),
                        new Play(),
                        new Join(),
                        new Stop(),
                        new Skip(),
                        new NowPlaying(),
                        new Queue(),
                        new Repeat(),
                        new Pause(),
                        new Leave()
                )
                .setOwnerId(config.get("OWNER_ID"));

        CommandClient commandClient = builder.build();
        builder.useHelpBuilder(false);
        builder.forceGuildOnly(666789289853583370L);
        builder.setActivity(Activity.listening("RadiumFluor"));


        DefaultShardManagerBuilder shardBuilder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"), Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY)
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(Arrays.asList(INTENTS))
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(commandClient, waiter, new EventListener())
                .setActivity(Activity.listening("RadiumFluor"));

        shardManager = shardBuilder.build();
        LOG.info("Bot inicializado com sucesso!!!");
    }

    public static void main(String[] args)
            throws IllegalArgumentException, LoginException, RateLimitedException {
        Main bot = new Main();
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

}