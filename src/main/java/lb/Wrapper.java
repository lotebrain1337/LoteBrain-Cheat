package lb;

import lb.hooks.GameRendererHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

import java.util.logging.Logger;

import static net.minecraft.client.Minecraft.getInstance;

public interface Wrapper {
    Minecraft minecraft = getInstance(); // Abbreviated object of getInstance method.
    GameRendererHook GAME_RENDERER_HOOK = new GameRendererHook(); // GameRendererHook object that replaces GameRenderer(some methods).
    GameRenderer oldGameRenderer = minecraft.gameRenderer; // Old gameRenderer, to properly disable the hook.
    Logger LOGGER = Logger.getGlobal(); // Logger for console information.
}
