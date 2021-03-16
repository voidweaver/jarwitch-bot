package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Helper;
import jarwitch.Main;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;

import java.util.concurrent.CompletableFuture;

public class IP implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("ip")
            .description("displays the current ip of the bot's host")
            .category("miscellaneous")
            .syntax("ip", "displays the current ip")
            .examples(new String[]{"ip"})
            .build();

    public IP(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        String ip = Main.getIP();

        CompletableFuture<String> getIP;

        if (ip == null) {
            getIP = Helper.refreshIP();
        } else {
            getIP = CompletableFuture.completedFuture(ip);
        }

        getIP.thenCompose(newIP -> context.message.getChannel().sendMessage(newIP).submit());
    }

    @Override
    public void resume(InvocationInfo info) {}
}
