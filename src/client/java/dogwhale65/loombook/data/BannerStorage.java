package dogwhale65.loombook.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dogwhale65.loombook.Loombook;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages persistence of saved banner patterns to a JSON file.
 */
public class BannerStorage {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static BannerStorage instance;

    private final Path configPath;
    private List<SavedBanner> banners = new ArrayList<>();

    public BannerStorage() {
        this.configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("loombook")
                .resolve("banners");
    }

    public static BannerStorage getInstance() {
        if (instance == null) {
            instance = new BannerStorage();
        }
        return instance;
    }

    public void load() {
        if (!Files.exists(configPath)) {
            Loombook.LOGGER.info("No saved banners directory found, starting fresh");
            return;
        }

        try {
            Files.list(configPath)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(p -> {
                        try (Reader reader = Files.newBufferedReader(p)) {
                            SavedBanner banner = GSON.fromJson(reader, SavedBanner.class);
                            if (banner != null) {
                                banners.add(banner);
                            }
                        } catch (IOException e) {
                            Loombook.LOGGER.error("Failed to load banner from {}", p, e);
                        }
                    });
            Loombook.LOGGER.info("Loaded {} saved banners", banners.size());
        } catch (IOException e) {
            Loombook.LOGGER.error("Failed to load saved banners directory", e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            Loombook.LOGGER.error("Failed to create banners directory", e);
        }
    }

    public void addBanner(SavedBanner banner) {
        banners.add(banner);
        saveBannerToFile(banner);
    }

    public void removeBanner(String id) {
        banners.removeIf(b -> b.getId().equals(id));
        try {
            Path bannerFile = configPath.resolve(id + ".json");
            Files.deleteIfExists(bannerFile);
        } catch (IOException e) {
            Loombook.LOGGER.error("Failed to delete banner file for id: {}", id, e);
        }
    }

    private void saveBannerToFile(SavedBanner banner) {
        try {
            Files.createDirectories(configPath);
            Path bannerFile = configPath.resolve(banner.getId() + ".json");
            try (Writer writer = Files.newBufferedWriter(bannerFile)) {
                GSON.toJson(banner, writer);
            }
            Loombook.LOGGER.debug("Saved banner {} to disk", banner.getId());
        } catch (IOException e) {
            Loombook.LOGGER.error("Failed to save banner to disk", e);
        }
    }

    public List<SavedBanner> getBanners() {
        return Collections.unmodifiableList(banners);
    }

    public SavedBanner getBannerById(String id) {
        return banners.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Exports a banner to JSON string format for sharing/backup
     */
    public String exportBannerToJson(String bannerId) {
        SavedBanner banner = getBannerById(bannerId);
        if (banner == null) {
            return null;
        }
        return GSON.toJson(banner);
    }

    /**
     * Imports a banner from JSON string format
     */
    public SavedBanner importBannerFromJson(String jsonString) {
        try {
            SavedBanner banner = GSON.fromJson(jsonString, SavedBanner.class);
            if (banner != null) {
                // Check if banner with this ID already exists
                if (getBannerById(banner.getId()) != null) {
                    // Generate new ID to avoid conflicts
                    banner.setId(java.util.UUID.randomUUID().toString());
                }
                addBanner(banner);
                return banner;
            }
        } catch (Exception e) {
            Loombook.LOGGER.error("Failed to import banner from JSON", e);
        }
        return null;
    }
}
