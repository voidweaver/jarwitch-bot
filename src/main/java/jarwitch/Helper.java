package jarwitch;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Helper {
    private static final Logger log = LogManager.getLogger(Helper.class);

    public static CompletableFuture<String> refreshIP() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Process pr = Runtime.getRuntime().exec("dig +short -4 myip.opendns.com @resolver1.opendns.com");
                pr.waitFor();

                BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));

                try {
                    String line = buf.readLine();

                    Main.ip = line.strip();
                    log.debug("IP fetched: " + line.strip());
                } catch (IOException e) {
                    log.warn("{refreshIP} error reading result from command");
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("{refreshIP} command I/O error");
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.warn("{refreshIP} Thread interrupted");
            }

            return Main.ip;
        });
    }

    public static void sendBotError(MessageChannel channel, String error) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(":dizzy_face:  Error encountered")
                .setDescription("Error %s encountered".formatted(mono(error)))
                .build();

        channel.sendMessage(embed)
                .onErrorFlatMap(t -> channel.sendMessage(
                        Helper.restFailedEmbed("Error encountered, but discord won't let me say it :frown:", t)))
                .queue();
    }

    public static MessageEmbed errorEmbed(String title, @Nullable String description) {
        EmbedBuilder builder = new EmbedBuilder().setTitle(title);
        if (description != null) builder.setDescription(description);
        return builder.build();
    }

    public static MessageEmbed restFailedEmbed(String description, Throwable throwable) {
        return new EmbedBuilder()
                .setTitle("Action failed")
                .setDescription(description)
                .addField("Cause", Helper.mono(throwable.toString()), false)
                .build();
    }

    public static String mono(String... strs) {
        StringBuilder builder = new StringBuilder("`");

        for (String str : strs) {
            for (int index = 0; index < str.length(); index++) {
                char c = str.charAt(index);
                builder.append(c);
                if (c == '`') log.warn("mono() function receives a backtick, it should not.");
            }
        }

        builder.append("`");
        return builder.toString();
    }

    public static <T> String monoJoin(T[] arr) { return monoJoin(Arrays.asList(arr)); }

    public static <T> String monoJoin(T[] arr, @Nullable String delimiter, @Nullable String prefix) {
        return monoJoin(Arrays.asList(arr), delimiter, prefix);
    }

    public static <T> String monoJoin(Iterable<T> list) {
        return monoJoin(list, ", ", null);
    }

    public static <T> String monoJoin(Iterable<T> list, @Nullable String delimiter, @Nullable String prefix) {
        StringBuilder builder = new StringBuilder();
        for (T item : list) {
            String str = item.toString();

            if (delimiter != null && !builder.isEmpty()) builder.append(delimiter);

            builder.append("`");
            if (prefix != null) builder.append(prefix);
            for (int index = 0; index < str.length(); index++) {
                char c = str.charAt(index);
                builder.append(c);
                if (c == '`') log.warn("mono() function receives a backtick, it should not.");
            }
            builder.append("`");
        }

        return builder.toString();
    }

    public static String monoPrefix(@Nullable String prefix) {
        if (prefix == null || prefix.equals("")) return "none";
        else return Helper.mono(prefix);
    }

    public static String forceCaps(@NonNull String str) {
        if (str.length() == 0) return str;

        StringBuilder builder = new StringBuilder();
        builder.append(Character.toUpperCase(str.charAt(0)));
        for (int index = 1; index < str.length(); index++) {
            builder.append(Character.toLowerCase(str.charAt(index)));
        }

        return builder.toString();
    }

    public enum FlagType {
        LEADING, TRAILING
    }

    public static Map<String, String> parseArgs(FlagType type, List<String> args) {
        Map<String, String> parsed = new HashMap<>();

        switch (type) {
            case LEADING:
                String flag = null;
                boolean unassigned = false;

                for (String arg: args) {
                    if (arg.startsWith("-") && arg.length() > 1) {
                        if (unassigned) parsed.put(flag, "");

                        flag = arg.substring(1);
                        unassigned = true;
                    }
                    else if (flag != null && unassigned) {
                        parsed.put(flag, arg);
                        unassigned = false;
                    }
                }
                break;
            case TRAILING:
                for (String arg : args) {
                    parsed.put(String.valueOf(arg.charAt(arg.length() - 1)), arg.substring(0, arg.length() - 1));
                }
                break;
            default:
                log.warn("unknown argtype");
        }

        return parsed;
    }
}
