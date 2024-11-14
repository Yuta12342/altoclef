package adris.altoclef.tasks.manhunt;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;

public class ServerTickListener implements ServerTickEvents.EndTick {
    public void onEndTick(MinecraftServer integratedServer) {
        if (ManhuntTask.instance != null) {
            ManhuntTask.instance.onServerTick((IntegratedServer) integratedServer);
        }
        // System.console().writer().println("Integrated server ticked");
    }
}