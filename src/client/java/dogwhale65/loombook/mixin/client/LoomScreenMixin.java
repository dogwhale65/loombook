package dogwhale65.loombook.mixin.client;

import dogwhale65.loombook.ui.LoomSidePanel;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LoomScreen.class)
public abstract class LoomScreenMixin extends HandledScreen<LoomScreenHandler> {

    @Unique
    private LoomSidePanel loombook$sidePanel;

    public LoomScreenMixin(LoomScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void loombook$onInit(CallbackInfo ci) {
        // Position the panel to the right of the loom UI
        int panelX = this.x + this.backgroundWidth + 4;
        int panelY = this.y;
        loombook$sidePanel = new LoomSidePanel((LoomScreen)(Object)this, this.handler, panelX, panelY);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void loombook$onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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
