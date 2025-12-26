package dogwhale65.loombook.autocraft;

import dogwhale65.loombook.Loombook;
import dogwhale65.loombook.data.BannerPatternLayer;
import dogwhale65.loombook.data.SavedBanner;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.DyeColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;

import java.util.List;

/**
 * State machine for automatically crafting multi-layer banners.
 */
public class AutoCraftStateMachine {
    private static final int TICK_DELAY = 3;

    // Loom slot indices
    private static final int BANNER_SLOT = 0;
    private static final int DYE_SLOT = 1;
    private static final int PATTERN_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;
    private static final int INVENTORY_START = 4;
    private static final int INVENTORY_END = 40;

    private final LoomScreenHandler handler;
    private AutoCraftState state = AutoCraftState.IDLE;
    private SavedBanner targetBanner;
    private int currentLayerIndex = 0;
    private int ticksInState = 0;
    private String errorMessage = null;

    public enum AutoCraftState {
        IDLE,
        CHECKING_MATERIALS,
        PLACING_BANNER,
        PLACING_DYE,
        PLACING_PATTERN_ITEM,
        SELECTING_PATTERN,
        WAITING_FOR_OUTPUT,
        TAKING_OUTPUT,
        LAYER_COMPLETE,
        COMPLETE,
        ERROR
    }

    public AutoCraftStateMachine(LoomScreenHandler handler) {
        this.handler = handler;
    }

    public void start(SavedBanner banner) {
        this.targetBanner = banner;
        this.currentLayerIndex = 0;
        this.ticksInState = 0;
        this.errorMessage = null;
        
        // Validate all materials before starting
        String validationError = validateAllMaterials(banner);
        if (validationError != null) {
            error(validationError);
            return;
        }
        
        this.state = AutoCraftState.CHECKING_MATERIALS;
        Loombook.LOGGER.info("Starting auto-craft for banner with {} layers", banner.getLayers().size());
    }

    public void stop() {
        this.state = AutoCraftState.IDLE;
        this.targetBanner = null;
        this.currentLayerIndex = 0;
    }

    public boolean isActive() {
        return state != AutoCraftState.IDLE && state != AutoCraftState.COMPLETE && state != AutoCraftState.ERROR;
    }

    public AutoCraftState getState() {
        return state;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void tick() {
        if (state == AutoCraftState.IDLE || state == AutoCraftState.COMPLETE || state == AutoCraftState.ERROR) {
            return;
        }

        ticksInState++;
        if (ticksInState < TICK_DELAY) {
            return;
        }
        ticksInState = 0;

        switch (state) {
            case CHECKING_MATERIALS -> checkMaterials();
            case PLACING_BANNER -> placeBanner();
            case PLACING_DYE -> placeDye();
            case PLACING_PATTERN_ITEM -> placePatternItem();
            case SELECTING_PATTERN -> selectPattern();
            case WAITING_FOR_OUTPUT -> waitForOutput();
            case TAKING_OUTPUT -> takeOutput();
            case LAYER_COMPLETE -> advanceLayer();
        }
    }

    private void checkMaterials() {
        // All materials have been validated in start(), just proceed
        state = AutoCraftState.PLACING_BANNER;
    }

    private void placeBanner() {
        ItemStack bannerInSlot = handler.getSlot(BANNER_SLOT).getStack();

        if (!bannerInSlot.isEmpty()) {
            // Banner already in slot
            state = AutoCraftState.PLACING_DYE;
            return;
        }

        // Find banner in inventory and move it
        Item bannerItem = targetBanner.getBaseBannerItem();
        int slotId;
        if (currentLayerIndex == 0) {
            slotId = findBlankBannerInInventory(bannerItem);
        } else {
            slotId = findItemInInventory(bannerItem);
        }

        if (slotId >= 0) {
            quickMoveToSlot(slotId, BANNER_SLOT);
            state = AutoCraftState.PLACING_DYE;
        } else {
            error("Cannot find banner in inventory");
        }
    }

    private void placeDye() {
        if (currentLayerIndex >= targetBanner.getLayers().size()) {
            state = AutoCraftState.COMPLETE;
            return;
        }

        BannerPatternLayer layer = targetBanner.getLayers().get(currentLayerIndex);
        Item dyeItem = SavedBanner.getDyeItem(layer.getDyeColorEnum());

        ItemStack dyeInSlot = handler.getSlot(DYE_SLOT).getStack();
        if (!dyeInSlot.isEmpty()) {
            // Check if it's the right dye
            if (dyeInSlot.getItem() == dyeItem) {
                state = AutoCraftState.PLACING_PATTERN_ITEM;
                return;
            }
            // Wrong dye, need to swap it out
            quickMoveToInventory(DYE_SLOT);
        }

        int slotId = findItemInInventory(dyeItem);
        if (slotId >= 0) {
            quickMoveToSlot(slotId, DYE_SLOT);
            state = AutoCraftState.PLACING_PATTERN_ITEM;
        } else {
            error("Cannot find " + layer.getDyeColorEnum().getId() + " dye");
        }
    }

    private void placePatternItem() {
        BannerPatternLayer layer = targetBanner.getLayers().get(currentLayerIndex);
        String patternId = layer.patternId();

        // Check if this pattern requires a pattern item
        Item patternItem = getRequiredPatternItem(patternId);
        if (patternItem == null) {
            // No pattern item needed, go straight to selecting pattern
            state = AutoCraftState.SELECTING_PATTERN;
            return;
        }

        ItemStack patternInSlot = handler.getSlot(PATTERN_SLOT).getStack();
        if (!patternInSlot.isEmpty()) {
            if (patternInSlot.getItem() == patternItem) {
                state = AutoCraftState.SELECTING_PATTERN;
                return;
            }
            quickMoveToInventory(PATTERN_SLOT);
        }

        int slotId = findItemInInventory(patternItem);
        if (slotId >= 0) {
            quickMoveToSlot(slotId, PATTERN_SLOT);
            state = AutoCraftState.SELECTING_PATTERN;
        } else {
            error("Cannot find required pattern item");
        }
    }

    private void selectPattern() {
        BannerPatternLayer layer = targetBanner.getLayers().get(currentLayerIndex);
        String patternId = layer.patternId();

        // Find the pattern index in the loom's pattern list
        List<?> patterns = handler.getBannerPatterns();
        int patternIndex = -1;
        for (int i = 0; i < patterns.size(); i++) {
            Object pattern = patterns.get(i);
            // Pattern entries are registry entries
            if (pattern.toString().contains(patternId) ||
                pattern.toString().contains(patternId.replace("minecraft:", ""))) {
                patternIndex = i;
                break;
            }
        }

        if (patternIndex >= 0) {
            // Click the pattern button
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.interactionManager != null) {
                client.interactionManager.clickButton(handler.syncId, patternIndex);
            }
            state = AutoCraftState.WAITING_FOR_OUTPUT;
        } else {
            // Pattern not found, might be basic pattern that's always available
            // Try to click based on pattern name
            error("Pattern not found: " + patternId);
        }
    }

    private void waitForOutput() {
        ItemStack output = handler.getSlot(OUTPUT_SLOT).getStack();
        if (!output.isEmpty()) {
            state = AutoCraftState.TAKING_OUTPUT;
        }
        // Otherwise keep waiting
    }

    private void takeOutput() {
        // Take the output
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null && client.player != null) {
            // Shift-click to move to inventory, or if more layers, we'll put back in banner slot
            if (currentLayerIndex + 1 < targetBanner.getLayers().size()) {
                // More layers to go - take output and put in banner slot
                // First pick up the output
                client.interactionManager.clickSlot(handler.syncId, OUTPUT_SLOT, 0, SlotActionType.PICKUP, client.player);
                // Then place in banner slot (clear banner slot first if needed)
                ItemStack bannerSlotStack = handler.getSlot(BANNER_SLOT).getStack();
                if (!bannerSlotStack.isEmpty()) {
                    // Banner slot should be empty after crafting, but just in case
                    client.interactionManager.clickSlot(handler.syncId, BANNER_SLOT, 0, SlotActionType.QUICK_MOVE, client.player);
                }
                client.interactionManager.clickSlot(handler.syncId, BANNER_SLOT, 0, SlotActionType.PICKUP, client.player);
            } else {
                // Last layer - move to inventory
                client.interactionManager.clickSlot(handler.syncId, OUTPUT_SLOT, 0, SlotActionType.QUICK_MOVE, client.player);
                // Return pattern items to inventory
                returnPatternItemsToInventory();
            }
        }
        state = AutoCraftState.LAYER_COMPLETE;
    }

    private void advanceLayer() {
        currentLayerIndex++;
        if (currentLayerIndex >= targetBanner.getLayers().size()) {
            state = AutoCraftState.COMPLETE;
            Loombook.LOGGER.info("Auto-craft complete!");
        } else {
            state = AutoCraftState.CHECKING_MATERIALS;
        }
    }

    private void error(String message) {
        this.errorMessage = message;
        this.state = AutoCraftState.ERROR;
        Loombook.LOGGER.warn("Auto-craft error: {}", message);
    }

    private int findItemInInventory(Item item) {
        for (int i = INVENTORY_START; i < INVENTORY_END; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private int findBlankBannerInInventory(Item item) {
        for (int i = INVENTORY_START; i < INVENTORY_END; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                BannerPatternsComponent patterns = stack.get(DataComponentTypes.BANNER_PATTERNS);
                if (patterns == null || patterns.layers().isEmpty()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void quickMoveToSlot(int fromSlot, int toSlot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null && client.player != null) {
            // Pick up from source
            client.interactionManager.clickSlot(handler.syncId, fromSlot, 0, SlotActionType.PICKUP, client.player);
            // Place in destination
            client.interactionManager.clickSlot(handler.syncId, toSlot, 0, SlotActionType.PICKUP, client.player);
        }
    }

    private void quickMoveToInventory(int slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null && client.player != null) {
            client.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.QUICK_MOVE, client.player);
        }
    }

    private Item getRequiredPatternItem(String patternId) {
        // These patterns require a banner pattern item
        if (patternId.contains("globe")) return Items.GLOBE_BANNER_PATTERN;
        if (patternId.contains("creeper")) return Items.CREEPER_BANNER_PATTERN;
        if (patternId.contains("skull")) return Items.SKULL_BANNER_PATTERN;
        if (patternId.contains("flower")) return Items.FLOWER_BANNER_PATTERN;
        if (patternId.contains("mojang")) return Items.MOJANG_BANNER_PATTERN;
        if (patternId.contains("piglin")) return Items.PIGLIN_BANNER_PATTERN;
        if (patternId.contains("flow")) return Items.FLOW_BANNER_PATTERN;
        if (patternId.contains("guster")) return Items.GUSTER_BANNER_PATTERN;
        return null;
    }

    /**
     * Validates that all required materials are available before starting the craft
     * Returns an error message if validation fails, null if all materials are available
     */
    private String validateAllMaterials(SavedBanner banner) {
        // Check for base banner
        Item bannerItem = banner.getBaseBannerItem();
        int bannerSlotId = findBlankBannerInInventory(bannerItem);
        if (bannerSlotId < 0 && handler.getSlot(BANNER_SLOT).getStack().isEmpty()) {
            return "Missing base banner";
        }

        // Check for all dyes and pattern items
        for (int i = 0; i < banner.getLayers().size(); i++) {
            BannerPatternLayer layer = banner.getLayers().get(i);
            
            // Check dye
            Item dyeItem = SavedBanner.getDyeItem(layer.getDyeColorEnum());
            int dyeSlotId = findItemInInventory(dyeItem);
            if (dyeSlotId < 0 && handler.getSlot(DYE_SLOT).getStack().isEmpty()) {
                return "Missing " + layer.getDyeColorEnum().getId() + " dye for pattern " + (i + 1);
            }

            // Check pattern item if required
            Item patternItem = getRequiredPatternItem(layer.patternId());
            if (patternItem != null) {
                int patternSlotId = findItemInInventory(patternItem);
                if (patternSlotId < 0 && handler.getSlot(PATTERN_SLOT).getStack().isEmpty()) {
                    return "Missing " + patternItem.getName().getString() + " for pattern " + (i + 1);
                }
            }
        }

        return null; // All materials available
    }

    /**
     * Returns pattern items to inventory after crafting is complete
     */
    private void returnPatternItemsToInventory() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.interactionManager != null && client.player != null) {
            ItemStack patternSlot = handler.getSlot(PATTERN_SLOT).getStack();
            if (!patternSlot.isEmpty()) {
                // Move pattern item back to inventory
                client.interactionManager.clickSlot(handler.syncId, PATTERN_SLOT, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }
}
