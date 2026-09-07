package zzzank.probejs.features.parchment;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.features.parchment.data.ParchmentData;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * @author ZZZank
 */
public abstract class ParchmentMappingLoader {

    public static volatile boolean ENABLED = true;

    private static final String MAVEN_REPO_URL = "https://maven.parchmentmc.org/";
    private static final String MAVEN_GROUP_ID = "org.parchmentmc.data";
    private static volatile InjectParchment TRANSFORMER;

    /**
     * Load parchment data from a zip file.
     * The zip should contain a "parchment.json" file.
     *
     * @param zipPath Path to the zip file
     * @throws IOException if reading fails
     */
    public static synchronized InjectParchment getOrInitTransformer(Path zipPath) throws IOException {
        if (TRANSFORMER != null) {
            return TRANSFORMER;
        }

        if (!Files.exists(zipPath) || !Files.isRegularFile(zipPath)) {
            return null;
        }

        // open the zip as file system, cool
        try (var fileSystem = FileSystems.newFileSystem(zipPath);
             var reader = Files.newBufferedReader(fileSystem.getPath("parchment.json"))) {
            TRANSFORMER = new InjectParchment(ProbeJS.GSON.fromJson(reader, ParchmentData.class));
        }
        return TRANSFORMER;
    }

    public static ClassTransformer getTransformer() {
        if (TRANSFORMER == null) {
            throw new IllegalStateException("Parchment data not loaded");
        }
        return TRANSFORMER;
    }

    /**
     * Download parchment mapping zip for a specific Minecraft version.
     *
     * @param mcVersion Minecraft version (e.g., "1.16.5")
     * @param parchmentVersion Parchment version (e.g., "2022.03.06")
     * @param targetDir Directory to store the downloaded mapping
     * @return Path to the downloaded zip file, or null if download failed
     */
    public static Path downloadMapping(String mcVersion, String parchmentVersion, Path targetDir) throws IOException {
        String artifactId = "parchment-" + mcVersion;
        String packaging = "zip";

        String url = String.format(
            "%s%s/%s/%s/%s-%s.%s",
            MAVEN_REPO_URL,
            MAVEN_GROUP_ID.replace('.', '/'),
            artifactId,
            parchmentVersion,
            artifactId,
            parchmentVersion,
            packaging
        );

        ProbeJS.LOGGER.info("Downloading parchment mapping from: {}", url);

        Path zipPath = targetDir.resolve(String.format("%s-%s.%s", artifactId, parchmentVersion, packaging));

        // Skip download if zip already exists
        if (Files.exists(zipPath)) {
            ProbeJS.LOGGER.info("Parchment mapping already exists at: {}", zipPath);
            return zipPath;
        }

        // Create target directory if needed
        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Download zip file
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, zipPath, StandardCopyOption.REPLACE_EXISTING);
            ProbeJS.LOGGER.info("Downloaded parchment mapping to: {}", zipPath);
        }

        return zipPath;
    }
}
