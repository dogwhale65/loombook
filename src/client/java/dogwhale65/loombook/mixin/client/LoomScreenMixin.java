package dogwhale65.loombook.mixin.client;

import dogwhale65.loombook.ui.LoomSidePanel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.LoomScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends AbstractContainerScreen<LoomMenu> {

    @Unique
    private LoomSidePanel loombook$sidePanel;

    public LoomScreenMixin(LoomMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void loombook$onInit(CallbackInfo ci) {
        // Position the panel to the right of the loom UI
        int panelX = this.leftPos + this.imageWidth + 4;
        int panelY = this.topPos;
        loombook$sidePanel = new LoomSidePanel((LoomScreen)(Object)this, this.menu, panelX, panelY);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void loombook$onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (loombook$sidePanel != null) {
            // Tick the auto-craft state machine during render for smooth updates
            loombook$sidePanel.tick();
            loombook$sidePanel.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void loombook$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (loombook$sidePanel != null && loombook$sidePanel.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void loombook$onMouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if (loombook$sidePanel != null && loombook$sidePanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            cir.setReturnValue(true);
        }
    }
}
