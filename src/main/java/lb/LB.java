package lb;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("lb")
public class LB {
    public static double inflatingNumber = 0; // A number that is added to the entity's hitbox
    public static boolean toggled; // Boolean value that indicates whether the hook is enabled.
    public LB() {
        MinecraftForge.EVENT_BUS.register(new HooksHandler());
        MinecraftForge.EVENT_BUS.register(this);
    }
}
