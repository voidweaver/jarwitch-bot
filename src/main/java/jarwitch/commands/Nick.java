package jarwitch.commands;

import jarwitch.Command;
import jarwitch.CommandManager;
import jarwitch.Helper;
import jarwitch.Main;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class Nick implements Command {
    private final InvocationInfo context;
    private String nick;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("nick")
            .description("change the bot's nickname")
            .category("settings")
            .syntax("nick", "clears the bot's nickname to default")
            .syntax("nick NICKNAME", "sets the bot's nickname")
            .examples(new String[]{"nick", "nick JARVIS"})
            .allowedInDM(false)
            .selfPermissions(Permission.NICKNAME_CHANGE)
            .strictPermissions(Permission.NICKNAME_MANAGE)
            .build();

    public Nick(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        List<String> args = context.args;
        Message message = context.message;

        if (args.size() <= 1) {
            // TODO: Generate help embed for command bc it's ambiguous to reset nickname by just calling nick
            message.getGuild().getSelfMember()
                    .modifyNickname(null)
                    .flatMap(__ -> message.getChannel()
                            .sendMessage("Nickname reset to %s".formatted(Main.jda.getSelfUser().getName())))
                    .onErrorFlatMap(t -> message.getChannel().sendMessage(Helper.restFailedEmbed("Failed to set nickname", t)))
                    .queue();
        } else {
            String nick = message.getContentRaw().substring(context.prefix.length() + args.get(0).length()).trim();

            if (!message.getEmotes().isEmpty()) {
                message.getChannel()
                        .sendMessage("Setting nickname to an emote may cause unexpected results, do you still want to continue? (y/N)")
                        .queue();

                this.nick = nick;
                CommandManager.setOngoing(context.message.getChannel().getId(), message.getAuthor(), this);
                return;
            }

            message.getGuild().getSelfMember()
                    .modifyNickname(nick)
                    .flatMap(__ -> message.getChannel().sendMessage("Nickname set to %s".formatted(nick)))
                    .onErrorFlatMap(t -> message.getChannel().sendMessage(Helper.restFailedEmbed("Failed to set nickname", t)))
                    .queue();
        }
    }

    @Override
    public void resume(InvocationInfo info) {
        String response = info.message.getContentRaw().trim().toLowerCase();

        if (response.equals("y") || response.equals("yes")) {
            info.message.getGuild().getSelfMember()
                    .modifyNickname(nick)
                    .flatMap(__ -> info.message.getChannel().sendMessage("Nickname set to %s".formatted(nick)))
                    .onErrorFlatMap(t -> info.message.getChannel().sendMessage(Helper.restFailedEmbed("Failed to set nickname", t)))
                    .queue();
        } else {
            info.message.getChannel().sendMessage("Action cancelled").queue();
        }

        CommandManager.endOngoing(info.message.getChannel().getId(), info.message.getAuthor());
    }
}
