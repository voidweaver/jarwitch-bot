package jarwitch.commands;

import jarwitch.Command;
import jarwitch.CommandManager;
import jarwitch.Helper;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import jarwitch.data.Pair;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.requests.RestAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class Impersonate implements Command {
    private static final Logger log = LogManager.getLogger(Impersonate.class);
    final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("impersonate")
            .description("~~lets the user impersonate as bot~~\nbe that annoying 5yo who won't stop repeating everything after being told to repeat after someone")
            .category("miscellaneous")
            .syntax("impersonate", "starts impersonation session")
            .examples(new String[]{"impersonate"})
            .selfPermissions(Permission.MESSAGE_MANAGE)
            .strictPermissions(Permission.MESSAGE_MANAGE)
            .build();

    public Impersonate(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        CommandManager.setOngoing(context.message.getChannel().getId(), context.message.getAuthor(), this);
        log.debug("snowflake, id: %s, %s".formatted(context.snowflake, context.message.getAuthor().getId()));
        log.debug("hashCode: " + Pair.of(context.snowflake, context.message.getAuthor().getId()).hashCode());

        Message message = context.message;
        if (context.message.isFromGuild()) message.delete().queue();
        message.getChannel()
                .sendMessage("Impersonation session started, use %s to stop".formatted(Helper.mono(context.prefix, "stop")))
                .queue(sentMessage -> sentMessage.delete().queueAfter(3, TimeUnit.SECONDS));
    }

    @Override
    public void resume(InvocationInfo info) {
        Message message = info.message;

        if (!message.getAuthor().equals(context.message.getAuthor())) return;
        if (message.getContentRaw().strip().equals(context.prefix + "stop")) {
            CommandManager.endOngoing(context.message.getChannel().getId(), message.getAuthor());

            RestAction<Message> sendTerminated = message.getChannel().sendMessage("Impersonation session terminated");
            if (context.message.isFromGuild()) {
                message.delete().flatMap(__ -> sendTerminated).queue();
            } else sendTerminated.queue();

            return;
        }

        if (message.getType() == MessageType.DEFAULT) {
            if (context.message.isFromGuild()) message.delete().queue();
            message.getChannel().sendMessage(message).queue();
        }
    }
}
