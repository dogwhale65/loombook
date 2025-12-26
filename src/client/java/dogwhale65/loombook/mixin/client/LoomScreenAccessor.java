package dogwhale65.loombook.mixin.client;

import net.minecraft.client.gui.screen.ingame.LoomScreen;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoomScreen.class)
public interface LoomScreenAccessor {
    @Accessor("banner")
    ItemStack getBanner();

    @Accessor("dye")
    ItemStack getDye();

    @Accessor("pattern")
    ItemStack getPattern();

    @Accessor("canApplyDyePattern")
    boolean getCanApplyDyePattern();

    @Accessor("hasTooManyPatterns")
    boolean getHasTooManyPatterns();

    @Accessor("scrollPosition")
    float getScrollPosition();

    @Accessor("scrollPosition")
    void setScrollPosition(float position);

    @Accessor("bannerPatterns")
    BannerPatternsComponent getBannerPatterns();
}
