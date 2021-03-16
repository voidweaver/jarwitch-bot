package jarwitch.data;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

public class InvocationInfo {
    public final Message message;
    public final String snowflake, prefix;
    public final List<String> args;
    public final Settings settings;

    public InvocationInfo(Message message, String snowflake, String prefix, List<String> args, Settings settings) {
        this.message = message;
        this.snowflake = snowflake;
        this.prefix = prefix;
        this.args = args;
        this.settings = settings;
    }
}
