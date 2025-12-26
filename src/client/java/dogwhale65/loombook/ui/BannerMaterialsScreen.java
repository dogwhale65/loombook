package dogwhale65.loombook.ui;

import dogwhale65.loombook.Loombook;
import dogwhale65.loombook.data.BannerPatternLayer;
import dogwhale65.loombook.data.SavedBanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen showing required materials to craft a banner pattern.
 */
public class BannerMaterialsScreen extends Screen {
    private static final int CONTENT_WIDTH = 250;
    private static final int CONTENT_HEIGHT = 300;
    private static final int PADDING = 10;

    private final Screen previousScreen;
    private final SavedBanner banner;
    private int scrollOffset = 0;

    public BannerMaterialsScreen(Screen previousScreen, SavedBanner banner) {
        super(Text.literal("Banner Materials"));
        this.previousScreen = previousScreen;
        this.banner = banner;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // Draw content background
        int contentX = (this.width - CONTENT_WIDTH) / 2;
        int contentY = (this.height - CONTENT_HEIGHT) / 2;
        context.fill(contentX, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT, 0xFF1F1F1F);
        
        // Draw border
        context.fill(contentX, contentY, contentX + 1, contentY + CONTENT_HEIGHT, 0xFF4169E1); // Left
        context.fill(contentX + CONTENT_WIDTH - 1, contentY, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT, 0xFF4169E1); // Right
        context.fill(contentX, contentY, contentX + CONTENT_WIDTH, contentY + 1, 0xFF4169E1); // Top
        context.fill(contentX, contentY + CONTENT_HEIGHT - 1, contentX + CONTENT_WIDTH, contentY + CONTENT_HEIGHT, 0xFF4169E1); // Bottom

        // Draw title
        context.drawText(this.textRenderer, Text.literal("Required Materials"), contentX + PADDING, contentY + PADDING, 0xFF4169E1, true);

        // Draw crafting order header
        context.drawText(this.textRenderer, Text.literal("Crafting Order:"), contentX + PADDING, contentY + PADDING + 15, 0xFF4169E1, true);

        // Draw banner base color
        String baseColorName = banner.getBaseColorEnum().asString();
        ItemStack baseStack = new ItemStack(banner.getBaseBannerItem());
        context.drawItem(baseStack, contentX + PADDING, contentY + PADDING + 28);
        context.drawText(this.textRenderer, Text.literal("1. " + baseColorName + " Banner"), contentX + PADDING + 20, contentY + PADDING + 31, 0xFFFFFFFF, true);

        // Draw materials list with step numbers
        int materialY = contentY + PADDING + 50;
        int maxHeight = CONTENT_HEIGHT - PADDING * 2 - 50;
        
        List<MaterialEntry> materials = getMaterialsList();
        int stepNumber = 2;
        
        for (int i = scrollOffset; i < materials.size() && materialY < contentY + CONTENT_HEIGHT - 30; i++) {
            MaterialEntry entry = materials.get(i);
            
            // Draw item
            context.drawItem(entry.stack, contentX + PADDING, materialY - 2);
            
            // Draw text with step number
            String text = stepNumber + ". " + entry.name;
            if (entry.quantity > 1) {
                text = stepNumber + ". " + entry.quantity + "x " + entry.name;
            }
            context.drawText(this.textRenderer, Text.literal(text), contentX + PADDING + 20, materialY, 0xFFFFFFFF, true);
            materialY += 18;
            stepNumber++;
        }

        // Draw close button
        int buttonY = contentY + CONTENT_HEIGHT - 25;
        int buttonX = (this.width / 2) - 25;
        boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20;
        int buttonColor = buttonHovered ? 0xFF4169E1 : 0xFF1E40AF;
        context.fill(buttonX, buttonY, buttonX + 50, buttonY + 20, buttonColor);
        String closeText = "Close";
        int closeTextWidth = this.textRenderer.getWidth(closeText);
        context.drawText(this.textRenderer, Text.literal(closeText), buttonX + (50 - closeTextWidth) / 2, buttonY + 6, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int contentX = (this.width - CONTENT_WIDTH) / 2;
        int contentY = (this.height - CONTENT_HEIGHT) / 2;
        int buttonY = contentY + CONTENT_HEIGHT - 25;
        int buttonX = (this.width / 2) - 25;

        // Close button
        if (mouseX >= buttonX && mouseX < buttonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20) {
            this.client.setScreen(previousScreen);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int contentX = (this.width - CONTENT_WIDTH) / 2;
        int contentY = (this.height - CONTENT_HEIGHT) / 2;
        int textStartX = contentX + PADDING;
        int textStartY = contentY + PADDING + 40;
        int textWidth = CONTENT_WIDTH - (PADDING * 2);
        int textHeight = CONTENT_HEIGHT - PADDING * 2 - 50;

        // Check if mouse is over the materials list
        if (mouseX >= textStartX && mouseX < textStartX + textWidth &&
            mouseY >= textStartY && mouseY < textStartY + textHeight) {
            
            scrollOffset -= (int) verticalAmount;
            List<MaterialEntry> materials = getMaterialsList();
            int maxScroll = Math.max(0, materials.size() - (textHeight / 18));
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            
            return true;
        }
        
        return false;
    }

    private List<MaterialEntry> getMaterialsList() {
        List<MaterialEntry> materials = new ArrayList<>();

        // Add each layer in order with its pattern and dye
        for (BannerPatternLayer layer : banner.getLayers()) {
            String patternId = layer.patternId();
            DyeColor dyeColor = layer.getDyeColorEnum();
            
            // Get pattern name
            String patternName = extractPatternName(patternId);
            
            // Get dye color name
            String colorName = dyeColor.asString();
            String displayColor = colorName.substring(0, 1).toUpperCase() + colorName.substring(1);
            
            // Create display text: "Pattern Name (Dye Color)"
            String displayName = patternName + " (" + displayColor + " Dye)";
            
            // Check if this pattern requires an item
            ItemStack patternStack = getPatternItem(patternId);
            if (patternStack != null && !patternStack.isEmpty()) {
                // Pattern requires an item
                materials.add(new MaterialEntry(patternStack, displayName, 1));
            } else {
                // Built-in pattern - show dye instead
                ItemStack dyeStack = new ItemStack(SavedBanner.getDyeItem(dyeColor));
                materials.add(new MaterialEntry(dyeStack, displayName, 1));
            }
        }

        return materials;
    }

    private String extractPatternName(String patternId) {
        // Extract the pattern name from the ID
        String[] parts = patternId.split(":");
        String name = parts.length > 1 ? parts[1] : patternId;
        
        // Convert snake_case to Title Case
        String[] words = name.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }
        return result.toString();
    }

    private ItemStack getPatternItem(String patternId) {
        // Try to find the pattern item in the registry
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
                var itemRegistry = client.world.getRegistryManager().getOptional(RegistryKeys.ITEM);
                
                if (itemRegistry.isPresent()) {
                    Registry<net.minecraft.item.Item> registry = (Registry<net.minecraft.item.Item>) itemRegistry.get();
                    
                    // Convert pattern ID to item ID (e.g., "minecraft:globe" -> "minecraft:globe_banner_pattern")
                    String[] parts = patternId.split(":");
                    String patternName = parts.length > 1 ? parts[1] : patternId;
                    String itemId = parts[0] + ":" + patternName + "_banner_pattern";
                    
                    Identifier itemIdentifier = Identifier.tryParse(itemId);
                    if (itemIdentifier != null && registry.containsId(itemIdentifier)) {
                        net.minecraft.item.Item item = registry.get(itemIdentifier);
                        if (item != null) {
                            return new ItemStack(item);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Loombook.LOGGER.debug("Failed to get pattern item for {}", patternId, e);
        }
        
        return ItemStack.EMPTY; // Built-in patterns don't need items
    }

    @Override
    public void close() {
        this.client.setScreen(previousScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private static class MaterialEntry {
        ItemStack stack;
        String name;
        int quantity;

        MaterialEntry(ItemStack stack, String name, int quantity) {
            this.stack = stack;
            this.name = name;
            this.quantity = quantity;
        }
    }
}
