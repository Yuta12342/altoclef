// package adris.altoclef.tasks.movement;

// import adris.altoclef.AltoClef;
// import adris.altoclef.tasksystem.Task;
// import adris.altoclef.util.helpers.WorldHelper;
// import dev.babbaj.pathfinder.xz.e;
// import net.minecraft.client.world.ClientWorld;
// import net.minecraft.entity.Entity;
// import net.minecraft.entity.LivingEntity;
// import net.minecraft.util.hit.HitResult;
// import net.minecraft.util.math.BlockPos;
// import net.minecraft.util.math.Vec3d;
// import net.minecraft.world.RaycastContext;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.PriorityQueue;
// import java.util.Queue;

// public class HideFromEntityTask extends Task {

//     private final LivingEntity _targetEntity;
//     private final int _searchRadius;
//     private BlockPos _bestHidingSpot;

//     public HideFromEntityTask(LivingEntity targetEntity, int searchRadius) {
//         _targetEntity = targetEntity;
//         _searchRadius = searchRadius;
//     }

//     @Override
//     protected void onStart(AltoClef mod) {
//         // No initialization needed
//     }

//     @Override
//     protected Task onTick(AltoClef mod) {
//         if (_bestHidingSpot == null) {
//             _bestHidingSpot = findBestHidingSpot(mod);
//         }

//         if (_bestHidingSpot != null) {
//             setDebugState("Hiding from " + _targetEntity.getName().getString() + " at " + _bestHidingSpot);
//             return new GetToBlockTask(_bestHidingSpot);
//         }

//         setDebugState("No hiding spot found, Running!");
//         return new RunAwayFromEntitiesTask(_targetEntity, _searchRadius);
//     }

//     @Override
//     protected void onStop(AltoClef mod, Task interruptTask) {
//         // No cleanup needed
//     }

//     @Override
//     protected boolean isEqual(Task other) {
//         if (other instanceof HideFromEntityTask task) {
//             return task._targetEntity.equals(_targetEntity) && task._searchRadius == _searchRadius;
//         }
//         return false;
//     }

//     @Override
//     protected String toDebugString() {
//         return "Hiding from " + _targetEntity.getName().getString();
//     }

//     @Override
//     public boolean isFinished(AltoClef mod) {
//         return _bestHidingSpot != null && mod.getPlayer().getBlockPos().equals(_bestHidingSpot);
//     }

//     private BlockPos findBestHidingSpot(AltoClef mod) {
//         BlockPos playerPos = mod.getPlayer().getBlockPos();
//         Queue<BlockPos> toSearch = new PriorityQueue<>((a, b) -> Double.compare(a.getSquaredDistance(playerPos), b.getSquaredDistance(playerPos)));
//         List<BlockPos> visited = new ArrayList<>();
//         toSearch.add(playerPos);

//         while (!toSearch.isEmpty()) {
//             BlockPos current = toSearch.poll();
//             if (visited.contains(current)) continue;
//             visited.add(current);

//             if (isHidingSpot(mod, current)) {
//                 return current;
//             }

//             for (BlockPos neighbor : getAdjacentBlocks(current)) {
//                 if (!visited.contains(neighbor) && neighbor.isWithinDistance(playerPos, _searchRadius)) {
//                     toSearch.add(neighbor);
//                 }
//             }
//         }

//         return null;
//         }

//         private void checkAdjacentBlocks(BlockPos current, ClientWorld world, AltoClef mod, Queue<BlockPos> toSearch, List<BlockPos> visited) {
//         for (BlockPos neighbor : getAdjacentBlocks(current)) {
//             if (!visited.contains(neighbor) && neighbor.isWithinDistance(mod.getPlayer().getBlockPos(), _searchRadius)) {
//             HitResult result = world.raycast(new RaycastContext(
//                 new Vec3d(current.getX(), current.getY(), current.getZ()),
//                 new Vec3d(neighbor.getX(), neighbor.getY(), neighbor.getZ()),
//                 RaycastContext.ShapeType.COLLIDER,
//                 RaycastContext.FluidHandling.NONE,
//                 mod.getPlayer()
//             ));
//             if (result.getType() == HitResult.Type.MISS) {
//                 toSearch.add(neighbor);
//             }
//             }
//         }
//         }

//         public static List<BlockPos> getAdjacentBlocks(BlockPos pos) {
//         List<BlockPos> adjacentBlocks = new ArrayList<>();
//         adjacentBlocks.add(pos.north());
//         adjacentBlocks.add(pos.south());
//         adjacentBlocks.add(pos.east());
//         adjacentBlocks.add(pos.west());
//         adjacentBlocks.add(pos.up());
//         adjacentBlocks.add(pos.down());
//         return adjacentBlocks;
//     }
// }

//     private boolean isHidingSpot(AltoClef mod, BlockPos pos) {
//         Vec3d targetPos = _targetEntity.getPos();
//         Vec3d playerPos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

//         // Check if the block at pos can hide the player from the target entity's vision
//         return !mod.getWorld().rayTrace(new RayTraceContext(playerPos, targetPos, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, mod.getPlayer())).getType().isOf(HitResult.Type.MISS);
//     }
// }