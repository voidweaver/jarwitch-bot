package jarwitch.commands;

import jarwitch.Command;
import jarwitch.Helper;
import jarwitch.Main;
import jarwitch.data.CommandInfo;
import jarwitch.data.InvocationInfo;
import jarwitch.data.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import org.bson.Document;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Set implements Command {
    final InvocationInfo context;

    public static final CommandInfo commandInfo = CommandInfo.builder()
            .name("set")
            .description("modifies bot settings")
            .category("settings")
            .syntax("set", "displays the overview of all settings along with their values")
            .syntax("set SETTING", "displays a specific setting and its value")
            .syntax("set SETTING VALUE", "sets a value (true/false) to a setting")
            .strictPermissions(Permission.MANAGE_SERVER)
            .examples(new String[]{"set", "set warnUnknown true"})
            .build();

    final static Map<String, String> HELPTEXTS = new LinkedHashMap<>(){{
        put("warnUnknown", "Send a warning message when the bot receives an unknown command.");
        put("strict", "Checks command requester's permissions (to prevent trolls on bigger servers)");
    }};

    public static final Document DEFAULT_SETTINGS = new Document() {{
        put("warnUnknown", false);
        put("strict", false);
    }};

    public Set(InvocationInfo context) {
        this.context = context;
    }

    @Override
    public void run() {
        Message message = context.message;
        String nickname = message.isFromGuild() ? message.getGuild().getSelfMember().getEffectiveName() : Main.jda.getSelfUser().getName();
        String avatarUrl = Main.jda.getSelfUser().getEffectiveAvatarUrl();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(nickname + " settings", null, avatarUrl);

        List<String> args = context.args;
        Settings settings = context.settings;
        if (args.size() < 2) {
            builder.setDescription("To modify a setting, use %s".formatted(Helper.mono(context.prefix, commandInfo.name, " SETTING true/false")));

            for (Map.Entry<String, String> e : HELPTEXTS.entrySet()) {
                boolean isEnabled = settings.getUserPref(e.getKey());

                builder.addField((isEnabled ? ":white_check_mark:  " : ":white_square_button:  ") + e.getKey(), e.getValue(), false);
            }
        } else if (args.size() == 2) {
            builder.setDescription("To modify a setting, use %s".formatted(Helper.mono(context.prefix, commandInfo.name, " SETTING true/false")));

            String settingName = args.get(1);
            if (settings.isInvalidPref(settingName)) {
                message.getChannel()
                        .sendMessage(Helper.errorEmbed("Invalid setting " + Helper.mono(args.get(1)), null))
                        .queue();
                return;
            }

            boolean isEnabled = settings.getUserPref(settingName);
            builder.addField((isEnabled ? ":white_check_mark:  " : ":white_square_button:  ") + settingName, HELPTEXTS.get(settingName), false);
        } else {
            String settingName = args.get(1);
            if (settings.isInvalidPref(settingName)) {
                message.getChannel()
                        .sendMessage(Helper.errorEmbed("Invalid setting " + Helper.mono(args.get(1)), null))
                        .queue();
                return;
            }

            String bool = args.get(2).toLowerCase();
            boolean value;

            switch (bool) {
                case "false", "f", "0", "no" -> value = false;
                case "true", "t", "1", "yes" -> value = true;
                default -> {
                    message.getChannel()
                            .sendMessage(Helper.errorEmbed("Invalid value " + Helper.mono(args.get(2)), null))
                            .queue();
                    return;
                }
            }

            settings.setUserPref(settingName, value);

            builder.setTitle("Setting %s set to %s".formatted(settingName, value ? ":white_check_mark:  true" : ":white_square_button:  false"));
        }

        message.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public void resume(InvocationInfo info) {}
}
