package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Main;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.concurrent.CompletableFuture;

public class Ping implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("ping")
            .description("pings the user back with bot latency info")
            .category("general")
            .syntax("ping", "pings the bot")
            .examples(new String[]{"ping"})
            .build();

    public Ping(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        Message message = context.message;

        MessageAction msg = message.getChannel().sendMessage("Pinging...");
        commands.debug("sending ping text");

        long creationTime = System.currentTimeMillis();

        commands.debug("creationTime: " + creationTime);

        CompletableFuture<Message> msgSend = msg.submit();
        CompletableFuture<Long> getRestPing = Main.jda.getRestPing().submit();

        CompletableFuture<Long> getMsgPing = msgSend.thenApply(__ -> System.currentTimeMillis());

        CompletableFuture.allOf(getRestPing, getMsgPing).thenRun(() -> {
            Message sentMessage = msgSend.join();
            Long restPing = getRestPing.join();
            Long receivedTime = getMsgPing.join();

            commands.debug("receivedTime: " + receivedTime);

            sentMessage.editMessage(
                    String.format("""
                            Server latency: %dms (round trip)
                            API latency: %dms
                            """,
                            receivedTime - creationTime,
                            restPing)).queue();

            commands.debug("ping text edited");
        });

        commands.debug("ping text sent");
    }

    @Override
    public void resume(InvocationInfo info) {}
}
