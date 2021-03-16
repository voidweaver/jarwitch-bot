package jarwitch.commands;

import jarwitch.BotConfig;
import jarwitch.Command;
import jarwitch.Helper;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class Prefix implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("prefix")
            .description("shows/set the prefix for bot commands (defaults to %s in servers and %s in DMs)"
                    .formatted(
                            Helper.monoPrefix(BotConfig.get("defaultPrefix")),
                            Helper.monoPrefix(BotConfig.get("defaultDMPrefix"))))
            .category("settings")
            .syntax("prefix", "displays information about the current prefix settings")
            .syntax("prefix PREFIX", "sets the bot's prefix")
            .examples(new String[]{"prefix", "prefix -"})
            .strictPermissions(Permission.MANAGE_SERVER)
            .build();

    public Prefix(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        EmbedBuilder builder = new EmbedBuilder();
        List<String> args = context.args;
        String prefix = context.prefix;
        Message message = context.message;

        if (args.size() == 1) {
            builder.setTitle(String.format("Current prefix: %s", Helper.monoPrefix(prefix)));
        } else {
            prefix = args.get(1);

            if (prefix.contains("`")) {
                message.getChannel()
                        .sendMessage(Helper.errorEmbed("Action refused",
                                "Bot prefix that contains backtick will cause formatting error"))
                        .queue();
                return;
            }

            context.settings.setPrefix(prefix);

            builder.setTitle(String.format("Prefix set to %s", Helper.monoPrefix(prefix)));
        }

        String description = "Use %s followed by a prefix to change the prefix.\n%s";
        if (message.isFromGuild()) {
            builder.setDescription(description.formatted(
                    Helper.mono(prefix, commandInfo.name),
                    String.format("To set it to default, use %s", Helper.mono(prefix, commandInfo.name, " ", BotConfig.get("defaultPrefix")))));
        } else {
            String DMPrefix = BotConfig.get("defaultDMPrefix");
            if (DMPrefix.equals("")) {
                builder.setDescription(description.formatted(
                        Helper.mono(prefix, commandInfo.name),
                        String.format("To clear it, use %s", Helper.mono(prefix, NoPrefix.commandInfo.name))));
            } else {
                builder.setDescription(description.formatted(
                        Helper.mono(prefix, commandInfo.name),
                        String.format("To set it to default, use %s", Helper.mono(prefix, commandInfo.name, " ", BotConfig.get("defaultPrefix")))));
            }
        }

        message.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
