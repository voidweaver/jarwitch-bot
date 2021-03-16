package jarwitch;

import jarwitch.commands.Help;
import jarwitch.data.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static jarwitch.Main.jda;

public class CommandManager {
    private static final Logger log = LogManager.getLogger(CommandManager.class);
    static final Map<Pair<String, String>, Command> ongoingCommands = new HashMap<>();

    public static void setOngoing(String channelFlake, User author, Command command) {
        ongoingCommands.put(Pair.of(channelFlake, author.getId()), command);
    }

    public static void endOngoing(String channelFlake, User author) {
        ongoingCommands.remove(Pair.of(channelFlake, author.getId()));
    }

    static void parseThenRun(Message message, Settings settings) {
        Command ongoingCommand = ongoingCommands.get(Pair.of(message.getChannel().getId(), message.getAuthor().getId()));
        if (ongoingCommand != null) {
            InvocationInfo info = new InvocationInfo(message, settings.getSnowflake(), settings.getPrefix(), null, settings);
            ongoingCommand.resume(info);
            return;
        }

        String prefix = settings.getPrefix();
        String content = message.getContentRaw().strip();

        boolean isMentioned = message.isMentioned(jda.getSelfUser(), Message.MentionType.USER);

        if (!(content.startsWith(prefix) && content.length() > prefix.length()) && !isMentioned) return;
        if (isMentioned && !content.startsWith("<")) return;

        List<String> tokens = Arrays.stream(content.substring(
                isMentioned ?
                        content.indexOf(">") + 1
                        : prefix.length()
        ).strip().split(" ")).map(String::strip).filter(s -> !s.isEmpty()).collect(Collectors.toList());

        log.trace(tokens);

        final String alias = tokens.get(0).toLowerCase();
        Map<String, Pair<Class<? extends Command>, String[]>> definitions = Constants.commands;

        // find the actual command name from (potential) alias
        Class<? extends Command> command =
                definitions.entrySet().stream()
                .filter(e -> e.getKey().equals(alias) || Arrays.asList(e.getValue().getRight()).contains(alias))
                .findAny()
                .map(e -> e.getValue().getLeft())
                .orElse(null);

        if (command == null) {
            if (settings.getUserPref("warnUnknown")) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle("Unknown command " + Helper.mono(prefix, tokens.get(0)))
                        .setDescription("Use %s to get list all commands available".formatted(Helper.mono(prefix, Help.commandInfo.name)));

                message.getChannel().sendMessage(builder.build()).queue();
            }
            log.trace("unknown command received");
        } else {
            InvocationInfo info = new InvocationInfo(message, settings.getSnowflake(), prefix, tokens, settings);
            run(command, info);
        }
    }

    private static void run(Class<? extends Command> clazz, InvocationInfo info) {
        Command command;
        CommandInfo commandInfo;
        try {
            command = clazz.getConstructor(InvocationInfo.class).newInstance(info);
            commandInfo = Command.getInfo(clazz);
        } catch (ReflectiveOperationException e) {
            log.error("Failed to create a new instance of a command", e);
            Helper.sendBotError(info.message.getChannel(), Error.GETTER_INVOCATION_FAILED);
            return;
        }

        Message message = info.message;

        if (message.isFromGuild()) {
            if (!commandInfo.guildAllowed) {
                message.getTextChannel()
                        .sendMessage(Helper.errorEmbed("Invalid action", "This command can only be used in DMs"))
                        .queue();
                return;
            }

            // Check permissions
            Member member = message.getMember();
            if (member == null) {
                log.fatal("message is from guild but getMember() returned null");
                Helper.sendBotError(message.getChannel(), Error.GUILD_NULL_MEMBER);
                return;
            }

            boolean isStrict = info.settings.getUserPref("strict");

            EnumSet<Permission> missingSelfPerm = commandInfo.selfPermissions.clone();
            EnumSet<Permission> missingCallerPerm = isStrict ? commandInfo.callerPermissions.clone() : EnumSet.noneOf(Permission.class);

            TextChannel textChannel = message.getTextChannel();

            missingSelfPerm.removeAll(member.getGuild().getSelfMember().getPermissions(textChannel));
            missingCallerPerm.removeAll(member.getPermissions(textChannel));

            if (commandInfo.voiceRequired) {
                if (member.getVoiceState() == null) {
                    log.fatal("getVoiceState() returned null, ensure that CacheFlag.VOICE_STATE is enabled");
                    Helper.sendBotError(message.getChannel(), Error.VOICE_STATE_CACHE_DISABLED);
                    return;
                }

                VoiceChannel voiceChannel = member.getVoiceState().getChannel();

                if (voiceChannel == null) {
                    MessageEmbed error = Helper.errorEmbed(":no_entry: Invalid action", "You are not in a voice channel");
                    textChannel.sendMessage(error).queue();
                    return;
                }

                missingSelfPerm.removeAll(member.getGuild().getSelfMember().getPermissions(voiceChannel));
                missingCallerPerm.removeAll(member.getPermissions(voiceChannel));
            }

            if (missingSelfPerm.size() + missingCallerPerm.size() > 0) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setTitle(":no_entry: Invalid action")
                        .setDescription("Some permissions are missing");

                if(missingSelfPerm.size() > 0) builder.addField("I don't have the following permissions", Helper.monoJoin(missingSelfPerm), false);
                if(missingCallerPerm.size() > 0) builder.addField("You don't have the following permissions", Helper.monoJoin(missingCallerPerm), false);

                message.getChannel().sendMessage(builder.build()).queue();
                return;
            }
        } else if (!commandInfo.dmAllowed) {
            message.getPrivateChannel()
                    .sendMessage(Helper.errorEmbed("Invalid action", "This command can only be used in servers"))
                    .queue();
            return;
        }

        command.run();
    }

}
