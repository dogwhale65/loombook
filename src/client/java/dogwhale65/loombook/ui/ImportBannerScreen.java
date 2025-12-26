package dogwhale65.loombook.ui;

import dogwhale65.loombook.Loombook;
import dogwhale65.loombook.data.BannerStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Screen for importing banner JSON data with a text editor.
 */
public class ImportBannerScreen extends Screen {
    private static final int EDITOR_WIDTH = 300;
    private static final int EDITOR_HEIGHT = 200;
    private static final int PADDING = 10;

    private final Screen previousScreen;
    private StringBuilder textBuffer = new StringBuilder();
    private int cursorPos = 0;
    private int scrollOffset = 0;

    public ImportBannerScreen(Screen previousScreen) {
        super(Text.literal("Import Banner"));
        this.previousScreen = previousScreen;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw semi-transparent background
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        // Draw editor background
        int editorX = (this.width - EDITOR_WIDTH) / 2;
        int editorY = (this.height - EDITOR_HEIGHT) / 2;
        context.fill(editorX, editorY, editorX + EDITOR_WIDTH, editorY + EDITOR_HEIGHT, 0xFF1F1F1F);
        context.drawBorder(editorX, editorY, EDITOR_WIDTH, EDITOR_HEIGHT, 0xFFFFFFFF);

        // Draw title
        context.drawText(this.textRenderer, Text.literal("Paste Banner JSON or /give Command"),
            this.width / 2 - 120, editorY - 20, 0xFFFFFFFF, true);

        // Draw text content with scrolling
        int textStartX = editorX + PADDING;
        int textStartY = editorY + PADDING;
        int textWidth = EDITOR_WIDTH - (PADDING * 2);
        int textHeight = EDITOR_HEIGHT - (PADDING * 2) - 25;

        // Enable scissor test to clip text to editor bounds
        context.enableScissor(textStartX, textStartY, textStartX + textWidth, textStartY + textHeight);

        String text = textBuffer.toString();
        String[] lines = text.split("\n", -1);
        
        int lineY = textStartY - scrollOffset;
        for (int i = 0; i < lines.length; i++) {
            if (lineY > textStartY + textHeight) break;
            if (lineY + 10 > textStartY) {
                context.drawText(this.textRenderer, Text.literal(lines[i]), textStartX, lineY, 0xFFFFFFFF, true);
            }
            lineY += 10;
        }

        // Draw cursor
        if (cursorPos >= 0 && cursorPos <= text.length()) {
            String beforeCursor = text.substring(0, cursorPos);
            int cursorX = textStartX + this.textRenderer.getWidth(beforeCursor.split("\n")[beforeCursor.split("\n").length - 1]);
            int cursorLineNum = beforeCursor.split("\n", -1).length - 1;
            int cursorY = textStartY + (cursorLineNum * 10) - scrollOffset;
            
            if (cursorY >= textStartY && cursorY <= textStartY + textHeight) {
                context.fill(cursorX, cursorY, cursorX + 1, cursorY + 10, 0xFFFFFFFF);
            }
        }

        context.disableScissor();

        // Draw buttons
        int buttonY = editorY + EDITOR_HEIGHT + PADDING;
        
        // OK button
        int okButtonX = (this.width / 2) - 60;
        boolean okHovered = mouseX >= okButtonX && mouseX < okButtonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20;
        int okColor = okHovered ? 0xFF4CAF50 : 0xFF2E7D32;
        context.fill(okButtonX, buttonY, okButtonX + 50, buttonY + 20, okColor);
        context.drawText(this.textRenderer, Text.literal("OK"), okButtonX + 15, buttonY + 6, 0xFFFFFFFF, true);

        // Cancel button
        int cancelButtonX = (this.width / 2) + 10;
        boolean cancelHovered = mouseX >= cancelButtonX && mouseX < cancelButtonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20;
        int cancelColor = cancelHovered ? 0xFFFF5555 : 0xFFCC0000;
        context.fill(cancelButtonX, buttonY, cancelButtonX + 50, buttonY + 20, cancelColor);
        context.drawText(this.textRenderer, Text.literal("Cancel"), cancelButtonX + 5, buttonY + 6, 0xFFFFFFFF, true);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int editorX = (this.width - EDITOR_WIDTH) / 2;
        int editorY = (this.height - EDITOR_HEIGHT) / 2;
        int buttonY = editorY + EDITOR_HEIGHT + PADDING;

        // OK button
        int okButtonX = (this.width / 2) - 60;
        if (mouseX >= okButtonX && mouseX < okButtonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20) {
            importBanner();
            return true;
        }

        // Cancel button
        int cancelButtonX = (this.width / 2) + 10;
        if (mouseX >= cancelButtonX && mouseX < cancelButtonX + 50 && mouseY >= buttonY && mouseY < buttonY + 20) {
            this.client.setScreen(previousScreen);
            return true;
        }

        // Click in text area
        int textStartX = editorX + PADDING;
        int textStartY = editorY + PADDING;
        int textWidth = EDITOR_WIDTH - (PADDING * 2);
        int textHeight = EDITOR_HEIGHT - (PADDING * 2) - 25;

        if (mouseX >= textStartX && mouseX < textStartX + textWidth && 
            mouseY >= textStartY && mouseY < textStartY + textHeight) {
            // Set cursor position (simplified - just go to end for now)
            cursorPos = textBuffer.length();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        String text = textBuffer.toString();
        boolean isCtrlPressed = (modifiers & 2) != 0; // GLFW_MOD_CONTROL

        // Ctrl+V for paste
        if (isCtrlPressed && keyCode == 86) { // V key
            String clipboard = this.client.keyboard.getClipboard();
            if (clipboard != null && !clipboard.isEmpty()) {
                textBuffer.insert(cursorPos, clipboard);
                cursorPos += clipboard.length();
            }
            return true;
        }

        if (keyCode == 259) { // Backspace
            if (cursorPos > 0) {
                textBuffer.deleteCharAt(cursorPos - 1);
                cursorPos--;
            }
            return true;
        } else if (keyCode == 261) { // Delete
            if (cursorPos < text.length()) {
                textBuffer.deleteCharAt(cursorPos);
            }
            return true;
        } else if (keyCode == 262) { // Right arrow
            if (cursorPos < text.length()) {
                cursorPos++;
            }
            return true;
        } else if (keyCode == 263) { // Left arrow
            if (cursorPos > 0) {
                cursorPos--;
            }
            return true;
        } else if (keyCode == 265) { // Up arrow
            // Move cursor up one line
            int lineStart = text.lastIndexOf('\n', cursorPos - 1);
            if (lineStart == -1) lineStart = 0;
            int prevLineStart = text.lastIndexOf('\n', lineStart - 1);
            if (prevLineStart == -1) prevLineStart = 0;
            int colInLine = cursorPos - lineStart - 1;
            cursorPos = Math.max(prevLineStart, prevLineStart + colInLine);
            return true;
        } else if (keyCode == 264) { // Down arrow
            // Move cursor down one line
            int lineStart = text.lastIndexOf('\n', cursorPos - 1);
            if (lineStart == -1) lineStart = 0;
            int nextLineStart = text.indexOf('\n', cursorPos);
            if (nextLineStart == -1) return true;
            int nextNextLineStart = text.indexOf('\n', nextLineStart + 1);
            if (nextNextLineStart == -1) nextNextLineStart = text.length();
            int colInLine = cursorPos - lineStart - 1;
            cursorPos = Math.min(nextNextLineStart, nextLineStart + 1 + colInLine);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr <= 126) { // Printable ASCII
            textBuffer.insert(cursorPos, chr);
            cursorPos++;
            return true;
        } else if (chr == '\n' || chr == '\r') { // Enter
            textBuffer.insert(cursorPos, '\n');
            cursorPos++;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int editorX = (this.width - EDITOR_WIDTH) / 2;
        int editorY = (this.height - EDITOR_HEIGHT) / 2;
        int textStartX = editorX + PADDING;
        int textStartY = editorY + PADDING;
        int textWidth = EDITOR_WIDTH - (PADDING * 2);
        int textHeight = EDITOR_HEIGHT - (PADDING * 2) - 25;

        // Check if mouse is over the text editor
        if (mouseX >= textStartX && mouseX < textStartX + textWidth &&
            mouseY >= textStartY && mouseY < textStartY + textHeight) {
            
            // Scroll by 10 pixels per scroll
            scrollOffset -= (int) (verticalAmount * 10);
            
            // Calculate max scroll
            String text = textBuffer.toString();
            String[] lines = text.split("\n", -1);
            int totalHeight = lines.length * 10;
            int maxScroll = Math.max(0, totalHeight - textHeight);
            
            // Clamp scroll offset
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            
            return true;
        }
        
        return false;
    }

    private void importBanner() {
        String input = textBuffer.toString().trim();
        if (input.isEmpty()) {
            Loombook.LOGGER.warn("Import text is empty");
            return;
        }

        BannerStorage storage = BannerStorage.getInstance();
        if (storage.importBannerFromJson(input) != null) {
            Loombook.LOGGER.info("Banner imported successfully");
            this.client.setScreen(previousScreen);
        } else {
            String format = input.startsWith("/give") ? "/give command" : "JSON";
            Loombook.LOGGER.error("Failed to import banner - invalid {}", format);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(previousScreen);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
