package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Main;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.List;

public class Uptime implements Command {
    private final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("uptime")
            .description("displays the bot's uptime")
            .category("miscellaneous")
            .syntax("uptime", "displays the bot's uptime")
            .syntax("uptime iso", "displays the bot's uptime in ISO 8601 time format")
            .examples(new String[]{"uptime", "uptime iso"})
            .build();

    public Uptime(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        List<String> args = context.args;
        Period uptime = new Period(Main.startupTime, Instant.now());
        String periodString = args.contains("iso") || args.contains("i") ? uptime.toString() : PeriodFormat.getDefault().print(uptime);

        context.message.getChannel().sendMessage(periodString).queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
