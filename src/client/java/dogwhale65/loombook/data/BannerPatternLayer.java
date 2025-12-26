package dogwhale65.loombook.data;

import net.minecraft.util.DyeColor;

/**
 * Represents a single pattern layer on a banner.
 * Stores the pattern identifier and dye color used.
 */
public record BannerPatternLayer(
    String patternId,
    String dyeColor
) {
    public DyeColor getDyeColorEnum() {
        return DyeColor.byId(dyeColor, DyeColor.WHITE);
    }

    public static BannerPatternLayer of(String patternId, DyeColor color) {
        return new BannerPatternLayer(patternId, color.getId());
    }
}
