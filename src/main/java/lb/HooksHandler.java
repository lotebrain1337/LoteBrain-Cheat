package lb;

import lb.hooks.GameRendererHook;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static lb.LB.*;
public class HooksHandler implements Wrapper {

    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) {
        if (!(minecraft.gameRenderer instanceof GameRendererHook) && toggled) {
            setGameRenderer(GAME_RENDERER_HOOK);
            LOGGER.info("GameRenderer GAME_RENDERER_HOOK swap.");
        } else if(!toggled && minecraft.gameRenderer instanceof GameRendererHook) {
            setGameRenderer(oldGameRenderer);
            LOGGER.info("GameRenderer old swap.");
        }
    }
    public void setGameRenderer(GameRenderer gameRenderer) {
        try {
            Field gameRendererField = minecraft.getClass().getDeclaredField("gameRenderer");
            gameRendererField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(gameRendererField, gameRendererField.getModifiers() & Modifier.FINAL);
            gameRendererField.set(minecraft, gameRenderer);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    @SubscribeEvent
    public void keyInput(InputEvent.KeyInputEvent e) {
        if (e.getAction() == 1) {
            int key = e.getKey();
            switch (key) {
                case GLFW.GLFW_KEY_UP:
                    inflatingNumber += 0.5;
                    break;
                case GLFW.GLFW_KEY_DOWN:
                    inflatingNumber -= 0.5;
                    break;
                case GLFW.GLFW_KEY_INSERT:
                    toggled = !toggled;
                    break;
            }
        }
    }
}
