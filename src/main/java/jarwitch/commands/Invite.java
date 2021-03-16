package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Helper;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.InviteAction;

import java.util.Map;

public class Invite implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("invite")
            .description("generates an invite to this server")
            .category("general")
            // Note: U+00A0 " " NO-BREAK SPACE
            .syntax("invite [...u] [...h] [...m]", "generates an invite (u\u00A0=\u00A0number of uses, h\u00A0=\u00A0hours, m\u00A0=\u00A0minutes)")
            .examples(new String[]{"invite", "invite 2u 1h", "invite 1u 30m", "invite 4h 20m"})
            .allowedInDM(false)
            .selfPermissions(Permission.CREATE_INSTANT_INVITE)
            .strictPermissions(Permission.CREATE_INSTANT_INVITE)
            .build();

    public Invite(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        Map<String, String> parsed = Helper.parseArgs(Helper.FlagType.TRAILING, context.args);
        String uses = parsed.get("u"), hours = parsed.get("h"), minutes = parsed.get("m");

        Message message = context.message;

        InviteAction inviteAction = message.getTextChannel().createInvite();
        if (hours != null || minutes != null) {
            int age = 0;

            if (hours != null) age += Integer.parseInt(hours) * 3600;
            if (minutes != null) age += Integer.parseInt(minutes) * 60;

            //noinspection ResultOfMethodCallIgnored
            inviteAction.setMaxAge(age);
        }

        if (uses != null) {
            //noinspection ResultOfMethodCallIgnored
            inviteAction.setMaxUses(Integer.parseInt(uses));
        }

        inviteAction
                .flatMap(invite -> message.getChannel().sendMessage(invite.getUrl()))
                .onErrorFlatMap(t -> message.getChannel().sendMessage(Helper.restFailedEmbed("Failed to create invite", t)))
                .queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
