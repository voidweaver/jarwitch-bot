package jarwitch;

import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Command {
    Logger commands = LogManager.getLogger("commands");

    static CommandInfo getInfo(Class<? extends Command> clazz) throws ReflectiveOperationException {
        return (CommandInfo) clazz.getField("commandInfo").get(null);
    }

    static void preload(Class<? extends Command> clazz) {
        try {
            clazz.getMethod("preload").invoke(null);
        } catch (ReflectiveOperationException e) {
            commands.warn("preload not invoked for " + clazz);
        }
    }

    void run();

    void resume(InvocationInfo info);
}
