package adris.altoclef.util.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import java.util.function.Predicate;

public class PlayerUtils {

    public static ServerPlayerEntity getClosestPlayerExcluding(World world, PlayerEntity entity, double maxDistance, PlayerEntity excludePlayer) {
        return getClosestPlayer(world, entity.getX(), entity.getY(), entity.getZ(), maxDistance, player -> !player.equals(excludePlayer));
    }

    public static ServerPlayerEntity getClosestPlayer(World world, double x, double y, double z, double maxDistance, Predicate<Entity> targetPredicate) {
        if (!(world instanceof ServerWorld)) {
            return null;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        double d = -1.0;
        ServerPlayerEntity closestPlayer = null;
        for (ServerPlayerEntity player : serverWorld.getPlayers()) {
            if (targetPredicate != null && !targetPredicate.test(player)) continue;
            double distance = player.squaredDistanceTo(x, y, z);
            if (!(maxDistance < 0.0) && !(distance < maxDistance * maxDistance) || d != -1.0 && !(distance < d)) continue;
            d = distance;
            closestPlayer = player;
        }
        return closestPlayer;
    }
}