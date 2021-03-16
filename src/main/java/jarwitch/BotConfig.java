package jarwitch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class BotConfig {
    private static final Properties props = new Properties();
    private static final Logger log = LogManager.getLogger(BotConfig.class);

    static void load() {
        String config_path = System.getenv("JARWITCH_CONFIG");
        if (config_path == null) config_path = "config.properties";
        try (InputStream input = new FileInputStream(config_path)) {
            props.load(input);
        } catch (IOException ex){
            ex.printStackTrace();
            log.error("failed to load config.properties", ex);
            System.exit(1);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
