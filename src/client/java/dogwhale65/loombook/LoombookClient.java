package dogwhale65.loombook;

import dogwhale65.loombook.data.BannerStorage;
import net.fabricmc.api.ClientModInitializer;

public class LoombookClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Loombook.LOGGER.info("Initializing Loombook client");
		BannerStorage.getInstance().load();
	}
}