package dogwhale65.loombook.ui;

import dogwhale65.loombook.data.SavedBanner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Renders banner thumbnails using proper 3D item rendering.
 */
public class BannerThumbnailRenderer {

    /**
     * Renders a banner thumbnail at the specified position and size.
     */
    public static void render(GuiGraphics context, SavedBanner banner, int x, int y, int size) {
        // Create a simple item stack from the banner's base color
        ItemStack bannerStack = new ItemStack(banner.getBaseBannerItem());
        
        // Render the item in 3D
        context.renderItem(bannerStack, x, y);
    }
}
