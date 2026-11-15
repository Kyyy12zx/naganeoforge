package com.leon.saintsdragons.common.config;

import com.leon.saintsdragons.common.SaintsDragonsCommon;
import com.leon.saintsdragons.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/** Migrates configuration files created by earlier Saint's Dragons layouts. */
public final class ConfigStorageLayout {
    private static boolean migrated;

    private ConfigStorageLayout() {
    }

    public static synchronized void migrateLegacyFiles() {
        if (migrated) {
            return;
        }
        migrated = true;

        Path root = Services.PLATFORM.getConfigDirectory();
        Path config = root.resolve("saintsdragons");
        copyLegacyFile(config.resolve("client.toml"),
                config.resolve("client/clientcommon.toml"));
        copyLegacyFile(config.resolve("spawning.toml"),
                config.resolve("server/spawning.toml"));
        copyLegacyFile(config.resolve("server.toml"),
                config.resolve("server/servercommon.toml"));
        copyLegacyFile(config.resolve("attributes.toml"),
                config.resolve("server/attributes.toml"));
        copyLegacyFile(config.resolve("camera_tuning.json"),
                config.resolve("client/dragon_rider_camera/camera_tuning.json"));
        copyLegacyDirectory(config.resolve("dragon_attributes"),
                config.resolve("server/dragon_attributes"));
    }

    private static void copyLegacyFile(Path source, Path target) {
        if (!Files.isRegularFile(source) || Files.exists(target)) {
            return;
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
            SaintsDragonsCommon.LOGGER.info("Copied legacy config {} to {}", source, target);
        } catch (IOException exception) {
            SaintsDragonsCommon.LOGGER.warn("Could not copy legacy config {} to {}", source, target, exception);
        }
    }

    private static void copyLegacyDirectory(Path source, Path target) {
        if (!Files.isDirectory(source)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(source)) {
            paths.filter(Files::isRegularFile).forEach(path ->
                    copyLegacyFile(path, target.resolve(source.relativize(path))));
        } catch (IOException exception) {
            SaintsDragonsCommon.LOGGER.warn("Could not inspect legacy config directory {}", source, exception);
        }
    }
}
