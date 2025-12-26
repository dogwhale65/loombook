package dogwhale65.loombook.ui;

import dogwhale65.loombook.Loombook;
import dogwhale65.loombook.data.BannerPatternLayer;
import dogwhale65.loombook.data.SavedBanner;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Renders banner preview thumbnails in the side panel.
 */
public class BannerPreviewRenderer {

    public static void render(DrawContext context, SavedBanner banner, LoomScreenHandler handler, int x, int y, int size) {
        // Create a banner item stack with patterns applied
        ItemStack bannerStack = createBannerWithPatterns(banner);

        // Render the item using DrawContext
        context.drawItem(bannerStack, x, y);
    }

    /**
     * Creates a banner ItemStack with all patterns from the SavedBanner applied.
     */
    private static ItemStack createBannerWithPatterns(SavedBanner banner) {
        ItemStack stack = new ItemStack(banner.getBaseBannerItem());
        
        List<BannerPatternLayer> layers = banner.getLayers();
        if (!layers.isEmpty()) {
            try {
                BannerPatternsComponent.Builder builder = new BannerPatternsComponent.Builder();
                
                Registry<Object> registry = getBannerPatternRegistry();
                
                if (registry != null) {
                    for (BannerPatternLayer layer : layers) {
                        try {
                            String patternIdStr = layer.patternId();
                            Identifier patternId = Identifier.tryParse(patternIdStr);
                            
                            if (patternId != null) {
                                Optional<RegistryEntry.Reference<Object>> entry = registry.getEntry(patternId);
                                
                                if (entry.isPresent()) {
                                    // We need to cast to the specific type expected by the builder
                                    @SuppressWarnings("unchecked")
                                    RegistryEntry<Object> castedEntry = entry.get();
                                    builder.add((RegistryEntry) castedEntry, layer.getDyeColorEnum());
                                } else {
                                    Loombook.LOGGER.debug("Pattern not found in registry: {}", patternId);
                                }
                            }
                        } catch (Exception e) {
                            Loombook.LOGGER.debug("Error processing banner pattern: {}", layer.patternId(), e);
                        }
                    }
                }
                
                stack.set(DataComponentTypes.BANNER_PATTERNS, builder.build());
            } catch (Exception e) {
                Loombook.LOGGER.debug("Error creating banner patterns component", e);
            }
        }
        
        return stack;
    }

    @SuppressWarnings("unchecked")
    private static Registry<Object> getBannerPatternRegistry() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            try {
                // Try getOptional first
                return (Registry<Object>) (Object) client.world.getRegistryManager().getOptional(RegistryKeys.BANNER_PATTERN).orElse(null);
            } catch (Exception e) {
                Loombook.LOGGER.debug("Failed to get registry from world", e);
            }
        }
        return null;
    }

    public static SavedBanner extractBannerData(ItemStack stack) {
        if (stack.isEmpty()) return null;

        DyeColor baseColor = getBannerColor(stack);
        if (baseColor == null) return null;

        List<BannerPatternLayer> layers = new ArrayList<>();
        BannerPatternsComponent patterns = stack.get(DataComponentTypes.BANNER_PATTERNS);

        if (patterns != null) {
            for (var layer : patterns.layers()) {
                String patternId = layer.pattern().getIdAsString();
                DyeColor dyeColor = layer.color();
                layers.add(BannerPatternLayer.of(patternId, dyeColor));
            }
        }

        return new SavedBanner(null, baseColor, layers);
    }

    private static DyeColor getBannerColor(ItemStack stack) {
        var item = stack.getItem();
        if (item == Items.WHITE_BANNER) return DyeColor.WHITE;
        if (item == Items.ORANGE_BANNER) return DyeColor.ORANGE;
        if (item == Items.MAGENTA_BANNER) return DyeColor.MAGENTA;
        if (item == Items.LIGHT_BLUE_BANNER) return DyeColor.LIGHT_BLUE;
        if (item == Items.YELLOW_BANNER) return DyeColor.YELLOW;
        if (item == Items.LIME_BANNER) return DyeColor.LIME;
        if (item == Items.PINK_BANNER) return DyeColor.PINK;
        if (item == Items.GRAY_BANNER) return DyeColor.GRAY;
        if (item == Items.LIGHT_GRAY_BANNER) return DyeColor.LIGHT_GRAY;
        if (item == Items.CYAN_BANNER) return DyeColor.CYAN;
        if (item == Items.PURPLE_BANNER) return DyeColor.PURPLE;
        if (item == Items.BLUE_BANNER) return DyeColor.BLUE;
        if (item == Items.BROWN_BANNER) return DyeColor.BROWN;
        if (item == Items.GREEN_BANNER) return DyeColor.GREEN;
        if (item == Items.RED_BANNER) return DyeColor.RED;
        if (item == Items.BLACK_BANNER) return DyeColor.BLACK;
        return null;
    }
}
