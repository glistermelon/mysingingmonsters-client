package com.glisterbyte.singingmonsters.common;

import net.harawata.appdirs.AppDirs;
import net.harawata.appdirs.AppDirsFactory;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

public final class GlobalConfig {

    private GlobalConfig() { }

    private static final Logger logger = LoggerFactory.getLogger(GlobalConfig.class);

    public static final String ANDROID_PACKAGE = "com.bigbluebubble.singingmonsters.full";
    public static final String GAME_VERSION_STRING = "5.4.0";

    // The access key is hardcoded into each version of the game client.
    public static final String ACCESS_KEY = "62621a06-351a-4d88-aff2-ab938e32a8f7";

    public static String getResourceAsString(String resource) {

        InputStream input = GlobalConfig.class.getResourceAsStream(resource);
        Validate.notNull(input, "Input stream for resource %s was expected to be non-null", resource);

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        try {
            for (int length; (length = input.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return result.toString(StandardCharsets.UTF_8);

    }

    public static long defaultWsTimeout = 15;

    public static final String APP_NAME;
    public static final String APP_VERSION;
    public static final String APP_AUTHOR;

    static {
        Properties props = new Properties();
        try {
            InputStream inputStream = GlobalConfig.class.getResourceAsStream("/app.properties");
            if (inputStream != null) props.load(inputStream);
        }
        catch (IOException ex) {
            logger.warn("Failed to load client app properties from resources; using placeholder properties");
        }
        APP_NAME = props.getProperty("app.name", "mysingingmonsters-client");
        APP_VERSION = props.getProperty("app.version", "unknown-version");
        APP_AUTHOR = props.getProperty("app.author", "unknown-author");
    }

    public static final Path cacheDir;

    static {
        AppDirs appDirs = AppDirsFactory.getInstance();
        String pathString = appDirs.getUserCacheDir(GlobalConfig.APP_NAME, GlobalConfig.APP_VERSION, GlobalConfig.APP_AUTHOR);
        Path path = Path.of(pathString);
        cacheDir = path;
        logger.info("Using cache directory '{}'", path);
    }

}