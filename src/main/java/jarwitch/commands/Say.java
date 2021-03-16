package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Constants;
import jarwitch.Helper;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;

public class Say implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("say")
            .description("says the given text")
            .category("miscellaneous")
            .syntax("say TEXT", "says the given text")
            .examples(new String[]{"say hello",
                    "say hey bud, check this out! <https://youtu.be/%64%51%77%34%77%39%57%67%58%63%51>",
                    "echo a quick brown fox jumps over the lazy dog"})
            .selfPermissions(Permission.MESSAGE_MANAGE)
            .strictPermissions(Permission.NICKNAME_CHANGE)
            .build();

    public Say(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        Message message = context.message;
        String text = message.getContentRaw().substring(context.prefix.length() + context.args.get(0).length()).trim();
        message.delete().queue();

        if (text.equals("")) text = Constants.ZWSP;
        message.getChannel()
                .sendMessage(text)
                .onErrorFlatMap(t -> message.getChannel().sendMessage(Helper.restFailedEmbed("Can't even say anything!", t)))
                .queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
