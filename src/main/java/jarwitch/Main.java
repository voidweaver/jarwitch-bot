package jarwitch;

import jarwitch.data.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Instant;

import javax.security.auth.login.LoginException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class Main extends ListenerAdapter {
    private static final Logger log = LogManager.getLogger(Main.class);
    public static JDA jda;

    public static final Instant startupTime = Instant.now();

    static String ip = null;

    @Nullable public static String getIP() {
        return ip;
    }

    public static void main(String[] args) {
        log.debug("Encoding: " + Charset.defaultCharset());

        BotConfig.load();

        try {
            JDABuilder builder = JDABuilder.createDefault(BotConfig.get("token.discord"));

            builder.setActivity(Activity.watching("itself being built"));
            builder.addEventListeners(new Main());

            jda = builder.build();
        } catch (LoginException e) {
            log.error("invalid token");
            System.exit(1);
        }

        log.info("logging in to Discord");

        if (Arrays.asList(args).contains("preload")) {
            log.info("preloading setup() methods");
            for (var pair : Constants.commands.values()) {
                try {
                    pair.getLeft().getDeclaredMethod("preload").invoke(null);
                } catch (ReflectiveOperationException e) {
                    log.trace("Preload function for %s not found, skipping".formatted(pair.getLeft()));
                }
            }
            log.info("setup() methods preloaded");
        }

        // TODO: check all commands if it's implemented correctly
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        log.info("logged in to Discord as >> '%s' [%s]".formatted(jda.getSelfUser().getAsTag(), jda.getSelfUser().getId()));

        TextChannel logChannel = jda.getTextChannelById(BotConfig.get("loggingChannel"));

        if (logChannel != null) {
            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("Logged in")
                    .setColor(Integer.parseInt(BotConfig.get("colors.green"), 16))
                    .setTimestamp(java.time.Instant.now());

            logChannel.sendMessage(builder.build()).queue();
        }

        Helper.refreshIP();

        Settings.DB.connect();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        super.onMessageReceived(event);

        Message message = event.getMessage();
        if (message.getAuthor() == jda.getSelfUser()) return;

        log.trace("Received " + message.getContentDisplay());

        String snowflake = message.isFromGuild() ? message.getGuild().getId() : message.getAuthor().getId();
        String name = message.isFromGuild() ? message.getGuild().getName() : message.getAuthor().getAsTag();

        Settings settings = new Settings(snowflake, name, message.isFromGuild());

        if (!message.getGuild().getSelfMember().hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
            message.getChannel()
                    .sendMessage("Jarvis requires the permission %s in order to function"
                            .formatted(Helper.mono(Permission.MESSAGE_EMBED_LINKS.toString())))
                    .queue();
            return;
        }

        CommandManager.parseThenRun(message, settings);
    }
}
