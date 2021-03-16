package jarwitch.data;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.lang.NonNull;
import jarwitch.BotConfig;
import jarwitch.commands.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Settings {
    private static final Logger log = LogManager.getLogger(Settings.class);

    public static class DB {
        private static boolean connected = false;
        private static final MongoCredential creds = MongoCredential.createCredential(BotConfig.get("db.username"), BotConfig.get("db.name"), BotConfig.get("db.password").toCharArray());

        private static MongoCollection<Document> guilds;

        public static void connect() {
            ServerAddress address = new ServerAddress(
                    BotConfig.get("db.hostname"), Integer.parseInt(BotConfig.get("db.port"))
            );

            MongoClientSettings.Builder builder = MongoClientSettings.builder().applyToClusterSettings(clusterBuilder ->
                    clusterBuilder.hosts(Collections.singletonList(address)));

            if (BotConfig.get("db.authorizationEnabled").equals("true")) {
                builder.credential(creds);
            }

            MongoClient client = MongoClients.create(builder.build());

            MongoDatabase db = client.getDatabase(BotConfig.get("db.name"));
            guilds = db.getCollection("prefs");

            connected = true;

            log.info("Connected to database at " + address.toString());
        }

        static void insert(@NonNull Document doc) {
            guilds.insertOne(doc);

            log.trace("Inserted document " + doc.toString());
        }

        private static Document get(@NonNull String snowflake) {
            return guilds.find(Filters.eq("snowflake", snowflake)).first();
        }

        private static void update(@NonNull String snowflake, @NonNull String key, @NonNull Object value) {
            CompletableFuture.runAsync(() -> guilds.updateOne(Filters.eq("snowflake", snowflake), Updates.set(key, value)));
        }
    }

    private final String snowflake;
    private Document doc;

    public Settings(String snowflake, String name, boolean isGuild) {
        this.snowflake = snowflake;

        if (!DB.connected) DB.connect();
        doc = DB.get(snowflake);

        if (doc == null) {
            doc = new Document()
                    .append("snowflake", snowflake)
                    .append("name", name)
                    .append("type", isGuild ? "guild" : "dm")
                    .append("prefix", isGuild ? BotConfig.get("defaultPrefix") : BotConfig.get("defaultDMPrefix"))
                    .append("settings", new Document());
            DB.insert(doc);
        }
    }

    public String getSnowflake() {
        return snowflake;
    }

    public String getPrefix() {
        return doc.getString("prefix");
    }

    public void setPrefix(String prefix) {
        DB.update(snowflake, "prefix", prefix);
    }

    public boolean isInvalidPref(String key) {
        return !Set.DEFAULT_SETTINGS.containsKey(key);
    }

    public boolean getUserPref(String key) { return getUserPref(key, true); }

    public boolean getUserPref(String key, boolean isEffective) {
        Boolean result = doc.get("settings", Document.class).getBoolean(key);
        if (result == null && isEffective) {
            result = Set.DEFAULT_SETTINGS.getBoolean(key);
        } else if (result == null) {
            log.warn("key %s does not exist in settings".formatted(key));
        }

        return result != null && result;
    }

    public void setUserPref(String key, boolean value) {
        doc.put(key, value);
        DB.update(snowflake, "settings." + key, value);
    }
}
