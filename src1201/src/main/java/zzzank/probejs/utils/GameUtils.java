package zzzank.probejs.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.registry.RegistryInfo;
import zzzank.probejs.utils.registry.RegistryInfos;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;

@UtilityClass
public class GameUtils {

    @Nullable
    public RegistryAccess registryAccess() {
        val server = ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.registryAccess();
    }

    public long modHash() {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            Platform.getMods()
                .stream()
                .sorted(Comparator.comparing(Mod::getModId))
                .map(m -> m.getModId() + m.getVersion())
                .map(String::getBytes)
                .forEach(digest::update);
            return ByteBuffer.wrap(digest.digest()).getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public long registryHash() {
        try {
            val digest = MessageDigest.getInstance("SHA-256");
            val server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) {
                return -1;
            }

            RegistryInfos.values()
                .stream()
                .flatMap(RegistryInfo::objectIds)
                .map(ResourceLocation::toString)
                .sorted()
                .map(String::getBytes)
                .forEach(digest::update);

            ByteBuffer buffer = ByteBuffer.wrap(digest.digest());
            return buffer.getLong();
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    public void logThrowable(Throwable t, int maxStackStraceCount) {
        val traces = t.getStackTrace();
        val limit = maxStackStraceCount < 0
            ? traces.length
            : Math.min(maxStackStraceCount, traces.length);
        val builder = new StringBuilder();
        builder.append(t);
        for (int i = 0; i < limit; i++) {
            builder.append('\n').append("    at ").append(traces[i]);
        }
        ProbeJS.LOGGER.error(builder);
    }

    public void logThrowable(Throwable t) {
        logThrowable(t, -1);
    }
}
