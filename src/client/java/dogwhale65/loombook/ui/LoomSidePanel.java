package dogwhale65.loombook.ui;

import dogwhale65.loombook.Loombook;
import dogwhale65.loombook.autocraft.AutoCraftStateMachine;
import dogwhale65.loombook.data.BannerStorage;
import dogwhale65.loombook.data.SavedBanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Side panel UI for the loom screen showing saved banner patterns.
 */
public class LoomSidePanel {
    public static final int PANEL_WIDTH = 150;
    public static final int PANEL_HEIGHT = 166;
    private static final int ENTRY_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 14;
    private static final int SAVE_BUTTON_HEIGHT = 16;
    private static final int BUTTON_HEIGHT = 12;
    private static final int PADDING = 4;

    private final LoomScreen screen;
    private final LoomScreenHandler handler;
    private int x;
    private int y;
    private int scrollOffset = 0;

    private AutoCraftStateMachine autoCraft;
    private String importBuffer = "";
    private boolean showingImportPrompt = false;
    private String selectedBannerId = null;

    public LoomSidePanel(LoomScreen screen, LoomScreenHandler handler, int x, int y) {
        this.screen = screen;
        this.handler = handler;
        this.x = x;
        this.y = y;
        this.autoCraft = new AutoCraftStateMachine(handler);
        Loombook.LOGGER.info("LoomSidePanel created at x={}, y={}", x, y);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        // Draw panel background with high z to be on top
        context.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, 0xE0000000);
        context.drawBorder(x, y, PANEL_WIDTH, PANEL_HEIGHT, 0xFFFFFFFF);

        // Draw header
        context.drawText(textRenderer, Text.literal("Saved"), x + PADDING, y + PADDING, 0xFFFFFFFF, true);

        // Draw save button
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        boolean saveHovered = isInSaveButton(mouseX, mouseY);
        int saveButtonColor = saveHovered ? 0xFF4CAF50 : 0xFF2E7D32;
        context.fill(x + PADDING, saveButtonY, x + PANEL_WIDTH - PADDING, saveButtonY + SAVE_BUTTON_HEIGHT, saveButtonColor);
        context.drawText(textRenderer, Text.literal("+ Save"), x + PANEL_WIDTH / 2 - 15, saveButtonY + 4, 0xFFFFFFFF, true);

        // Draw import/export buttons
        int importButtonY = saveButtonY + SAVE_BUTTON_HEIGHT + PADDING;
        boolean importHovered = isInImportButton(mouseX, mouseY);
        int importButtonColor = importHovered ? 0xFF4169E1 : 0xFF1E40AF;
        context.fill(x + PADDING, importButtonY, x + PANEL_WIDTH / 2 - 2, importButtonY + BUTTON_HEIGHT, importButtonColor);
        context.drawText(textRenderer, Text.literal("Import"), x + PADDING + 2, importButtonY + 2, 0xFFFFFFFF, true);

        int exportButtonX = x + PANEL_WIDTH / 2 + 2;
        boolean exportHovered = isInExportButton(mouseX, mouseY);
        int exportButtonColor = exportHovered ? 0xFF4169E1 : 0xFF1E40AF;
        context.fill(exportButtonX, importButtonY, x + PANEL_WIDTH - PADDING, importButtonY + BUTTON_HEIGHT, exportButtonColor);
        context.drawText(textRenderer, Text.literal("Export"), exportButtonX + 2, importButtonY + 2, 0xFFFFFFFF, true);

        // Draw saved patterns
        List<SavedBanner> banners = BannerStorage.getInstance().getBanners();
        int listStartY = importButtonY + BUTTON_HEIGHT + PADDING;
        int visibleHeight = PANEL_HEIGHT - (listStartY - y) - PADDING;
        int maxVisible = visibleHeight / ENTRY_HEIGHT;

        // Draw count of saved banners in header
        context.drawText(textRenderer, Text.literal("(" + banners.size() + ")"), x + PANEL_WIDTH - 22, y + PADDING, 0xFFAAAAAA, true);

        for (int i = 0; i < maxVisible && i + scrollOffset < banners.size(); i++) {
            SavedBanner banner = banners.get(i + scrollOffset);
            int entryY = listStartY + i * ENTRY_HEIGHT;

            boolean hovered = isInPatternEntry(mouseX, mouseY, entryY);
            boolean isSelected = banner.getId().equals(selectedBannerId);
            int bgColor = isSelected ? 0xFF4169E1 : (hovered ? 0x80FFFFFF : 0x40FFFFFF);
            context.fill(x + PADDING, entryY, x + PANEL_WIDTH - PADDING, entryY + ENTRY_HEIGHT - 2, bgColor);

            // Draw banner preview
            BannerPreviewRenderer.render(context, banner, handler, x + PADDING + 2, entryY + 2, 18);

            // Draw name (truncated to fit available space)
            String name = banner.getDisplayName();
            int maxNameWidth = PANEL_WIDTH - PADDING - 22 - 40; // Account for preview, padding, and buttons
            String displayName = name;
            while (displayName.length() > 0 && textRenderer.getWidth(displayName) > maxNameWidth) {
                displayName = displayName.substring(0, displayName.length() - 1);
            }
            if (!displayName.equals(name)) {
                displayName = displayName.substring(0, Math.max(0, displayName.length() - 2)) + "..";
            }
            context.drawText(textRenderer, Text.literal(displayName), x + PADDING + 22, entryY + 8, 0xFFFFFFFF, true);

            // Draw rename button
            int renameX = x + PANEL_WIDTH - PADDING - 28;
            boolean renameHovered = mouseX >= renameX && mouseX < renameX + 12 && mouseY >= entryY + 4 && mouseY < entryY + 18;
            int renameColor = renameHovered ? 0xFF4169E1 : 0xFFAAAAAA;
            context.drawText(textRenderer, Text.literal("E"), renameX + 2, entryY + 7, renameColor, true);

            // Draw delete button
            int deleteX = x + PANEL_WIDTH - PADDING - 12;
            boolean deleteHovered = mouseX >= deleteX && mouseX < deleteX + 12 && mouseY >= entryY + 4 && mouseY < entryY + 18;
            int deleteColor = deleteHovered ? 0xFFFF5555 : 0xFFAAAAAA;
            context.drawText(textRenderer, Text.literal("X"), deleteX + 2, entryY + 7, deleteColor, true);
        }

        // Draw craft button if a banner is selected
        if (selectedBannerId != null) {
            int craftButtonY = y + PANEL_HEIGHT - BUTTON_HEIGHT - PADDING;
            boolean craftHovered = isInCraftButton(mouseX, mouseY);
            int craftButtonColor = craftHovered ? 0xFF4CAF50 : 0xFF2E7D32;
            context.fill(x + PADDING, craftButtonY, x + PANEL_WIDTH - PADDING, craftButtonY + BUTTON_HEIGHT, craftButtonColor);
            context.drawText(textRenderer, Text.literal("Craft"), x + PANEL_WIDTH / 2 - 12, craftButtonY + 2, 0xFFFFFFFF, true);
        }

        // Draw scroll indicators if needed (only if no craft button is showing)
        if (scrollOffset > 0) {
            context.drawText(textRenderer, Text.literal("^"), x + PANEL_WIDTH / 2 - 2, listStartY - 10, 0xFFFFFFFF, true);
        }
        if (scrollOffset + maxVisible < banners.size() && selectedBannerId == null) {
            context.drawText(textRenderer, Text.literal("v"), x + PANEL_WIDTH / 2 - 2, y + PANEL_HEIGHT - 12, 0xFFFFFFFF, true);
        }

        // Draw auto-craft status or error below the panel
        if (autoCraft.isActive()) {
            context.drawText(textRenderer, Text.literal("Crafting..."), x + PANEL_WIDTH / 2 - 30, y + PANEL_HEIGHT + 4, 0xFFFFFF00, true);
        } else if (autoCraft.getState() == AutoCraftStateMachine.AutoCraftState.ERROR) {
            String err = autoCraft.getErrorMessage();
            if (err == null) err = "Error";
            int textWidth = textRenderer.getWidth(err);
            context.drawText(textRenderer, Text.literal(err), x + PANEL_WIDTH / 2 - textWidth / 2, y + PANEL_HEIGHT + 4, 0xFFFF5555, true);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int mx = (int) mouseX;
        int my = (int) mouseY;

        Loombook.LOGGER.info("Mouse clicked at x={}, y={}, panel bounds: x={}-{}, y={}-{}",
            mx, my, x, x + PANEL_WIDTH, y, y + PANEL_HEIGHT);

        // Check save button
        if (isInSaveButton(mx, my)) {
            Loombook.LOGGER.info("Save button clicked!");
            saveBannerFromOutput();
            return true;
        }

        // Check import button
        if (isInImportButton(mx, my)) {
            Loombook.LOGGER.info("Import button clicked!");
            MinecraftClient.getInstance().setScreen(new ImportBannerScreen(screen));
            return true;
        }

        // Check export button
        if (isInExportButton(mx, my)) {
            Loombook.LOGGER.info("Export button clicked!");
            exportSelectedBanner();
            return true;
        }

        // Check pattern entries
        List<SavedBanner> banners = BannerStorage.getInstance().getBanners();
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        int importButtonY = saveButtonY + SAVE_BUTTON_HEIGHT + PADDING;
        int listStartY = importButtonY + BUTTON_HEIGHT + PADDING;
        int visibleHeight = PANEL_HEIGHT - (listStartY - y) - PADDING;
        int maxVisible = visibleHeight / ENTRY_HEIGHT;

        // Check craft button
        if (selectedBannerId != null && isInCraftButton(mx, my)) {
            SavedBanner selected = BannerStorage.getInstance().getBannerById(selectedBannerId);
            if (selected != null) {
                autoCraft.start(selected);
            }
            return true;
        }

        for (int i = 0; i < maxVisible && i + scrollOffset < banners.size(); i++) {
            SavedBanner banner = banners.get(i + scrollOffset);
            int entryY = listStartY + i * ENTRY_HEIGHT;

            if (isInPatternEntry(mx, my, entryY)) {
                // Check if rename button clicked
                int renameX = x + PANEL_WIDTH - PADDING - 26;
                if (mx >= renameX && mx < renameX + 10 && my >= entryY + 6 && my < entryY + 16) {
                    MinecraftClient.getInstance().setScreen(new RenameBannerScreen(screen, banner.getId(), banner.getName()));
                    return true;
                }

                // Check if delete button clicked
                int deleteX = x + PANEL_WIDTH - PADDING - 10;
                if (mx >= deleteX && mx < deleteX + 10 && my >= entryY + 6 && my < entryY + 16) {
                    BannerStorage.getInstance().removeBanner(banner.getId());
                    if (selectedBannerId != null && selectedBannerId.equals(banner.getId())) {
                        selectedBannerId = null;
                    }
                    return true;
                }

                // Select banner
                selectedBannerId = banner.getId();
                return true;
            }
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!isInPanel((int) mouseX, (int) mouseY)) {
            return false;
        }

        List<SavedBanner> banners = BannerStorage.getInstance().getBanners();
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        int importButtonY = saveButtonY + SAVE_BUTTON_HEIGHT + PADDING;
        int listStartY = importButtonY + BUTTON_HEIGHT + PADDING;
        int visibleHeight = PANEL_HEIGHT - (listStartY - y) - PADDING;
        int maxVisible = visibleHeight / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, banners.size() - maxVisible);

        scrollOffset -= (int) verticalAmount;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    public void tick() {
        autoCraft.tick();
    }

    private void saveBannerFromOutput() {
        Loombook.LOGGER.info("Attempting to save banner from output slot");

        // Get the output slot (slot 3 in loom)
        var outputStack = handler.getSlot(3).getStack();
        Loombook.LOGGER.info("Output slot stack: {}, isEmpty: {}", outputStack, outputStack.isEmpty());

        if (outputStack.isEmpty()) {
            Loombook.LOGGER.info("Output slot is empty, nothing to save");
            return;
        }

        SavedBanner banner = BannerPreviewRenderer.extractBannerData(outputStack);
        Loombook.LOGGER.info("Extracted banner: {}", banner);

        if (banner != null) {
            BannerStorage.getInstance().addBanner(banner);
            Loombook.LOGGER.info("Banner saved! Total banners: {}", BannerStorage.getInstance().getBanners().size());
        } else {
            Loombook.LOGGER.info("Failed to extract banner data");
        }
    }

    private boolean isInPanel(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + PANEL_WIDTH && mouseY >= y && mouseY < y + PANEL_HEIGHT;
    }

    private boolean isInSaveButton(int mouseX, int mouseY) {
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        return mouseX >= x + PADDING && mouseX < x + PANEL_WIDTH - PADDING
                && mouseY >= saveButtonY && mouseY < saveButtonY + SAVE_BUTTON_HEIGHT;
    }

    private boolean isInImportButton(int mouseX, int mouseY) {
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        int importButtonY = saveButtonY + SAVE_BUTTON_HEIGHT + PADDING;
        return mouseX >= x + PADDING && mouseX < x + PANEL_WIDTH / 2 - 2
                && mouseY >= importButtonY && mouseY < importButtonY + BUTTON_HEIGHT;
    }

    private boolean isInExportButton(int mouseX, int mouseY) {
        int saveButtonY = y + HEADER_HEIGHT + PADDING;
        int importButtonY = saveButtonY + SAVE_BUTTON_HEIGHT + PADDING;
        int exportButtonX = x + PANEL_WIDTH / 2 + 2;
        return mouseX >= exportButtonX && mouseX < x + PANEL_WIDTH - PADDING
                && mouseY >= importButtonY && mouseY < importButtonY + BUTTON_HEIGHT;
    }

    private boolean isInCraftButton(int mouseX, int mouseY) {
        int craftButtonY = y + PANEL_HEIGHT - BUTTON_HEIGHT - PADDING;
        return mouseX >= x + PADDING && mouseX < x + PANEL_WIDTH - PADDING
                && mouseY >= craftButtonY && mouseY < craftButtonY + BUTTON_HEIGHT;
    }

    private boolean isInPatternEntry(int mouseX, int mouseY, int entryY) {
        return mouseX >= x + PADDING && mouseX < x + PANEL_WIDTH - PADDING
                && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT - 2;
    }

    private void exportSelectedBanner() {
        if (selectedBannerId == null) {
            Loombook.LOGGER.info("No banner selected to export");
            return;
        }

        String json = BannerStorage.getInstance().exportBannerToJson(selectedBannerId);
        
        if (json != null) {
            // Copy to clipboard
            MinecraftClient.getInstance().keyboard.setClipboard(json);
            Loombook.LOGGER.info("Banner exported to clipboard");
        }
    }

    public void importBannerFromClipboard(String jsonString) {
        SavedBanner imported = BannerStorage.getInstance().importBannerFromJson(jsonString);
        if (imported != null) {
            Loombook.LOGGER.info("Banner imported successfully: {}", imported.getId());
        } else {
            Loombook.LOGGER.error("Failed to import banner from JSON");
        }
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public AutoCraftStateMachine getAutoCraft() {
        return autoCraft;
    }
}
