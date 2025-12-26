package dogwhale65.loombook.data;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a saved banner design with all its pattern layers.
 */
public class SavedBanner {
    private String id;
    private String name;
    private String baseColor;
    private List<BannerPatternLayer> layers;
    private long createdAt;

    public SavedBanner() {
        this.id = UUID.randomUUID().toString();
        this.layers = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public SavedBanner(String name, DyeColor baseColor, List<BannerPatternLayer> layers) {
        this();
        this.name = name;
        this.baseColor = baseColor.getId();
        this.layers = new ArrayList<>(layers);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseColor() {
        return baseColor;
    }

    public DyeColor getBaseColorEnum() {
        return DyeColor.byId(baseColor, DyeColor.WHITE);
    }

    public void setBaseColor(String baseColor) {
        this.baseColor = baseColor;
    }

    public List<BannerPatternLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<BannerPatternLayer> layers) {
        this.layers = layers;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDisplayName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return "Banner " + id.substring(0, 4);
    }

    public Item getBaseBannerItem() {
        DyeColor color = getBaseColorEnum();
        return switch (color) {
            case WHITE -> Items.WHITE_BANNER;
            case ORANGE -> Items.ORANGE_BANNER;
            case MAGENTA -> Items.MAGENTA_BANNER;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_BANNER;
            case YELLOW -> Items.YELLOW_BANNER;
            case LIME -> Items.LIME_BANNER;
            case PINK -> Items.PINK_BANNER;
            case GRAY -> Items.GRAY_BANNER;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_BANNER;
            case CYAN -> Items.CYAN_BANNER;
            case PURPLE -> Items.PURPLE_BANNER;
            case BLUE -> Items.BLUE_BANNER;
            case BROWN -> Items.BROWN_BANNER;
            case GREEN -> Items.GREEN_BANNER;
            case RED -> Items.RED_BANNER;
            case BLACK -> Items.BLACK_BANNER;
        };
    }

    public static Item getDyeItem(DyeColor color) {
        return switch (color) {
            case WHITE -> Items.WHITE_DYE;
            case ORANGE -> Items.ORANGE_DYE;
            case MAGENTA -> Items.MAGENTA_DYE;
            case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
            case YELLOW -> Items.YELLOW_DYE;
            case LIME -> Items.LIME_DYE;
            case PINK -> Items.PINK_DYE;
            case GRAY -> Items.GRAY_DYE;
            case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
            case CYAN -> Items.CYAN_DYE;
            case PURPLE -> Items.PURPLE_DYE;
            case BLUE -> Items.BLUE_DYE;
            case BROWN -> Items.BROWN_DYE;
            case GREEN -> Items.GREEN_DYE;
            case RED -> Items.RED_DYE;
            case BLACK -> Items.BLACK_DYE;
        };
    }
}
