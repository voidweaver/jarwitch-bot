package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Constants;
import jarwitch.Error;
import jarwitch.Helper;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import jarwitch.data.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class Help implements Command {
    private static final Logger log = LogManager.getLogger(Help.class);
    final InvocationInfo context;

    private static boolean isSetup = false;
    private static final EmbedBuilder guildHelpBuilder = new EmbedBuilder();
    private static final EmbedBuilder dmHelpBuilder = new EmbedBuilder();

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("help")
            .description("shows list of commands currently implemented")
            .category("general")
            .syntax("help", "shows list of all commands")
            .syntax("help COMMAND", "shows info for a specific command")
            .examples(new String[]{"help", "help prefix"})
            .build();

    public Help(InvocationInfo context) {
        this.context = context;
    }

    public static void preload() throws ReflectiveOperationException {
        commands.debug("setup run");
        Collection<Pair<Class<? extends Command>, String[]>> definitions = Constants.commands.values();

        Map<String, StringBuilder> guildCategoryBuilders = new LinkedHashMap<>();
        Map<String, StringBuilder> dmCategoryBuilders = new LinkedHashMap<>();

        for (var pair : definitions) {
            Class<? extends Command> command = pair.getLeft();

            CommandInfo info;
            try {
                info = Command.getInfo(command);
            } catch (ReflectiveOperationException e) {
                log.error("Failed to get the info of the command " + command, e);
                throw e;
            }

            String mono = Helper.mono(info.name);

            if (info.guildAllowed) {
                StringBuilder guildBuilder = guildCategoryBuilders.computeIfAbsent(info.category, __ -> new StringBuilder());
                if (!guildBuilder.isEmpty()) guildBuilder.append(", ");

                guildBuilder.append(mono);
            }

            if (info.dmAllowed) {
                StringBuilder dmBuilder = dmCategoryBuilders.computeIfAbsent(info.category, __ -> new StringBuilder());
                if (!dmBuilder.isEmpty()) dmBuilder.append(", ");

                dmBuilder.append(mono);
            }
        }

        guildCategoryBuilders.forEach((key, value) ->
                guildHelpBuilder.addField(Helper.forceCaps(key), value.toString(), false));

        dmCategoryBuilders.forEach((key, value) ->
                dmHelpBuilder.addField(Helper.forceCaps(key), value.toString(), false));

        isSetup = true;

        commands.debug("preload setup");
    }

    @Override
    public void run() {
        if (!isSetup) {
            try {
                preload();
            } catch (ReflectiveOperationException e) {
                Helper.sendBotError(context.message.getChannel(), Error.GETTER_INVOCATION_FAILED);
                return;
            }
        }

        Message message = context.message;
        String prefix = context.prefix;
        List<String> args = context.args;

        if (context.args.size() <= 1) {
            EmbedBuilder builder = message.isFromGuild() ? guildHelpBuilder : dmHelpBuilder;

            builder.setTitle("Current prefix: %s".formatted(Helper.monoPrefix(prefix)));
            builder.setDescription("""
                    Use %s to get usage information for a particular command.
                    %s indicates an **optional** field."""
                    .formatted(
                            Helper.mono(prefix, Help.commandInfo.name, " COMMAND"),
                            Helper.mono("[...]")
                    ));

            message.getChannel().sendMessage(builder.build())
                    .onErrorFlatMap(t -> message.getChannel().sendMessage(Helper.restFailedEmbed("Failed to send help", t)))
                    .queue();
        } else {
            final String query = args.get(1);

            Optional<Pair<Class<? extends Command>, String[]>> match = Constants.commands.entrySet().stream()
                    .filter(entry -> entry.getKey().equals(query) || Arrays.asList(entry.getValue().getRight()).contains(query))
                    .findAny()
                    .map(Map.Entry::getValue);

            if (match.isEmpty()) {
                message.getChannel()
                        .sendMessage(
                                new EmbedBuilder().setTitle("Unknown command " + Helper.mono(prefix, args.get(1)))
                                        .setDescription("Use %s to list all available commands"
                                                .formatted(Helper.mono(prefix, Help.commandInfo.name))).build())
                        .queue();
            } else {
                CommandInfo command;
                try {
                    command = Command.getInfo(match.get().getLeft());
                } catch (ReflectiveOperationException e) {
                    Helper.sendBotError(context.message.getChannel(), Error.GETTER_INVOCATION_FAILED);
                    return;
                }

                String syntaxesString = command.syntaxes.stream()
                        .map(pair -> "%s – %s".formatted(Helper.mono(prefix, pair.getLeft()), pair.getRight()))
                        .collect(Collectors.joining("\n"));

                commands.debug(syntaxesString);

                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Command " + Helper.mono(prefix, command.name))
                        .setDescription(Helper.forceCaps(command.description))
                        .addField(command.syntaxes.size() > 1 ? "Syntaxes" : "Syntax", syntaxesString, false);

                if (!command.selfPermissions.isEmpty())
                    builder.addField("Required bot permissions", Helper.monoJoin(command.selfPermissions), true);

                if (!command.callerPermissions.isEmpty() && context.settings.getUserPref("strict"))
                    builder.addField("Required caller permissions", Helper.monoJoin(command.callerPermissions), true);

                if (!command.selfPermissions.isEmpty() && !command.callerPermissions.isEmpty() && context.settings.getUserPref("strict"))
                    builder.addBlankField(true);

                String[] aliases = match.get().getRight();
                if (aliases.length > 0)
                    builder.addField(aliases.length > 1 ? "Aliases" : "Alias", Helper.monoJoin(aliases), true);

                builder.addField(command.examples.length > 1 ? "Examples" : "Example",
                        Helper.monoJoin(command.examples, "\n", prefix), true);

                builder.addField("Category", Helper.forceCaps(command.category), true);

                if (aliases.length <= 0) builder.addBlankField(true);

                message.getChannel().sendMessage(builder.build()).queue();
            }
        }
    }

    @Override
    public void resume(InvocationInfo info) {}
}
