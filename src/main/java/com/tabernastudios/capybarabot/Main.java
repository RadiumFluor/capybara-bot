package com.tabernastudios.capybarabot;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.SlashCommand;
import com.jagrosh.jdautilities.command.SlashCommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.tabernastudios.capybarabot.commands.general.Ping;
import com.tabernastudios.capybarabot.commands.music.*;
import com.tabernastudios.capybarabot.commands.owner.Shutdown;
import com.tabernastudios.capybarabot.listeners.EventListener;
import com.tabernastudios.capybarabot.listeners.QueueInteraction;
import com.tabernastudios.capybarabot.logging.LoggingFormater;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.*;


public class Main extends ListenerAdapter {

    public static final Logger logger = Logger.getLogger(Main.class.getSimpleName());
    final ConsoleHandler newHandler = new ConsoleHandler();
    final Formatter formatter = new LoggingFormater();


    public final static GatewayIntent[] INTENTS = {
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.SCHEDULED_EVENTS};
    private final ShardManager shardManager;

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Main() {

        logger.setUseParentHandlers(false);
        newHandler.setFormatter(formatter);
        newHandler.setLevel(Level.FINEST);
        logger.addHandler(newHandler);
        logger.setLevel(Level.FINEST);

        logger.log(Level.INFO, "Logger inicializado...");

        logger.info("Inicializando EventWaiter...");
        EventWaiter waiter = new EventWaiter();

        logger.info("Importando credenciais...");
        Dotenv config = Dotenv.configure().load();

        logger.info("Importando comandos...");
        CommandClientBuilder builder = new CommandClientBuilder();
        builder
                .setOwnerId(config.get("OWNER_ID"))
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
                        new Leave(),
                        new Shuffle(),
                        new Shutdown()
                );


        logger.fine("Comandos importados!");
        logger.info("Registrando comandos...");

        CommandClient commandClient = builder.build();
        builder.useHelpBuilder(false);
        builder.forceGuildOnly(config.get("GUILD_ID"));
        logger.fine("Comandos registrados!");

        logger.info("Inicializando sharding...");
        DefaultShardManagerBuilder shardBuilder = DefaultShardManagerBuilder.createDefault(config.get("TOKEN"), Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE, CacheFlag.ACTIVITY, CacheFlag.EMOJI)
                .setStatus(OnlineStatus.ONLINE)
                .enableIntents(Arrays.asList(INTENTS))
                .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(commandClient,
                        waiter,
                        new EventListener(),
                        new QueueInteraction())
                .setActivity(Activity.listening("RadiumFluor"));

        shardManager = shardBuilder.build();
        logger.fine("Sharding iniciada!");
    }

    @SuppressWarnings("unused")
    public static void main(String[] args)
            throws IllegalArgumentException {
        Main bot = new Main();

        Thread commandThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Digite um comando: ");
                String comando = scanner.nextLine();

                if (comando.equals("shutdown")) {

                    Thread taskThread = new Thread(() -> {
                        bot.getShardManager().shutdown();
                        Main.logger.warning("Desligando bot...");
                    });

                    taskThread.start();

                    try {
                        taskThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Main.logger.info("Bot desligado!");

                    System.exit(0);

                }
            }
        });

        commandThread.start();
        logger.info("Cappy Bot implantado!");
        try {
            commandThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}