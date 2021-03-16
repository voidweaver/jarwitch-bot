package jarwitch.data;

import net.dv8tion.jda.api.Permission;

import java.util.*;

public class CommandInfo {
    public static class Builder {
        private String name, description, category;
        private String[] examples;
        private final List<Pair<String, String>> syntaxes = new ArrayList<>();
        // Permissions MESSAGE_READ and MESSAGE_WRITE are omitted
        private final EnumSet<Permission> selfPermissions = EnumSet.noneOf(Permission.class),
                strictPermissions = EnumSet.noneOf(Permission.class);

        private boolean voiceRequired = false,
                guildAllowed = true,
                dmAllowed = true;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder examples(String[] examples) {
            this.examples = examples;
            return this;
        }

        public Builder syntax(String syntax, String helpText) {
            this.syntaxes.add(Pair.of(syntax, helpText));
            return this;
        }

        public Builder selfPermissions(Permission ...permissions) {
            this.selfPermissions.addAll(Arrays.asList(permissions));
            return this;
        }

        public Builder strictPermissions(Permission ...permissions) {
            this.strictPermissions.addAll(Arrays.asList(permissions));
            return this;
        }

        public Builder strictPermissions(Collection<Permission> permissions) {
            this.strictPermissions.addAll(permissions);
            return this;
        }

        public Builder voiceRequired(boolean required) {
            this.voiceRequired = required;
            return this;
        }

        public Builder allowedInGuild(boolean allowed) {
            this.guildAllowed = allowed;
            return this;
        }

        public Builder allowedInDM(boolean allowed) {
            this.dmAllowed = allowed;
            return this;
        }

        public CommandInfo build() {
            return new CommandInfo(this);
        }
    }

    public final String name, description, category;
    public final String[] examples;
    public final List<Pair<String, String>> syntaxes;
    public final EnumSet<Permission> selfPermissions, callerPermissions;

    public final boolean voiceRequired, guildAllowed, dmAllowed;

    private CommandInfo(Builder builder) {
         name = builder.name;
         description = builder.description;
         category = builder.category;
         examples = builder.examples;
         syntaxes = builder.syntaxes;
         selfPermissions = builder.selfPermissions;
         callerPermissions = builder.strictPermissions;

         voiceRequired = builder.voiceRequired;
         guildAllowed = builder.guildAllowed;
         dmAllowed = builder.dmAllowed;
    }

    public static Builder builder() {
        return new Builder();
    }
}
