package com.fincompare.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileCache {
    private static final Logger logger = LoggerFactory.getLogger(FileCache.class);

    private final Path cacheDirectory;

    public FileCache() {
        // Use system temp directory for caching
        this.cacheDirectory = Paths.get(System.getProperty("java.io.tmpdir"), "fincompare-cache");

        try {
            Files.createDirectories(cacheDirectory);
            logger.info("Cache directory initialized at: {}", cacheDirectory);
        } catch (IOException e) {
            logger.error("Failed to create cache directory", e);
        }
    }

    /**
     * Get cached content if available
     */
    public String get(String key) {
        String cacheKey = generateCacheKey(key);
        Path cacheFile = cacheDirectory.resolve(cacheKey);

        if (Files.exists(cacheFile)) {
            try {
                String content = Files.readString(cacheFile);
                logger.info("Cache hit for key: {}", key);
                return content;
            } catch (IOException e) {
                logger.warn("Failed to read cache file: {}", cacheFile, e);
            }
        }

        logger.debug("Cache miss for key: {}", key);
        return null;
    }

    /**
     * Put content into cache
     */
    public void put(String key, String content) {
        String cacheKey = generateCacheKey(key);
        Path cacheFile = cacheDirectory.resolve(cacheKey);

        try {
            Files.writeString(cacheFile, content);
            logger.info("Cached content for key: {}", key);
        } catch (IOException e) {
            logger.error("Failed to write cache file: {}", cacheFile, e);
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        String cacheKey = generateCacheKey(key);
        Path cacheFile = cacheDirectory.resolve(cacheKey);
        return Files.exists(cacheFile);
    }

    /**
     * Clear all cached files
     */
    public void clearCache() {
        try {
            Files.walk(cacheDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete cache file: {}", path);
                        }
                    });
            logger.info("Cache cleared");
        } catch (IOException e) {
            logger.error("Failed to clear cache", e);
        }
    }

    /**
     * Generate a cache key using MD5 hash
     */
    private String generateCacheKey(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(key.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple key if MD5 not available
            return key.replaceAll("[^a-zA-Z0-9]", "_");
        }
    }
}
