package jarwitch.commands;

import jarwitch.BotConfig;
import jarwitch.Command;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.EmbedBuilder;

public class NoPrefix implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("noprefix")
            .description("sets the prefix settings to no prefix")
            .category("settings")
            .syntax("noprefix", "sets the prefix to no prefix")
            .examples(new String[]{"noprefix"})
            .allowedInGuild(BotConfig.get("defaultPrefix").equals(""))
            .allowedInDM(BotConfig.get("defaultDMPrefix").equals(""))
            .strictPermissions(Prefix.commandInfo.callerPermissions)
            .build();

    public NoPrefix(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        context.settings.setPrefix("");
        context.message.getChannel().sendMessage(new EmbedBuilder().setTitle("Prefix set to none").build()).queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
