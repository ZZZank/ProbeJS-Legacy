package zzzank.probejs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zzzank.probejs.utils.JsonUtils;

import java.nio.file.Path;

/**
 * @author ZZZank
 */
public class ProbeJS {
    public static final String MOD_ID = "probejs";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder()
        .serializeSpecialFloatingPointValues()
        .setLenient()
        .disableHtmlEscaping()
        .registerTypeHierarchyAdapter(Path.class, JsonUtils.PathConverter.INSTANCE)
        .create();
    public static final Gson GSON_WRITER = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();
}
