package adris.altoclef.tasks.resources;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.Arrays;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import dev.babbaj.pathfinder.xz.e;
import dev.babbaj.pathfinder.xz.s;
import dev.babbaj.pathfinder.xz.u;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TorchBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import adris.altoclef.tasksystem.Task;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.entity.EntityType;
import adris.altoclef.tasks.construction.*;
import adris.altoclef.tasks.movement.IdleTask;
import adris.altoclef.TaskCatalogue;

public class CollectDragonEgg extends ResourceTask {
    /*
     * private enum State {
     * MOVE_TO_POSITION,
     * BREAK_BLOCK,
     * PLACE_TORCH,
     * BREAK_EGG
     * }
     * 
     * private State currentState = State.MOVE_TO_POSITION;
     */

    private final int _count;
    private static boolean _isDroppingTheEgg = false;
    private static TimerGame _eggTimer = new TimerGame(1);
    private Predicate<Block> _blockPredicate = block -> block == Blocks.BEDROCK || block == Blocks.END_PORTAL_FRAME
            || block.getHardness() > 6000;
    private static Optional<BlockPos> eggPos = Optional.empty();

    // public CollectDragonEgg(ItemTarget[] itemTargets) {
    // super(itemTargets);
    // if (itemTargets.length != 1) {
    // throw new IllegalArgumentException("CollectDragonEgg must have exactly 1
    // target.");
    // }
    // }

    public CollectDragonEgg(int targetCount) {
        super(Items.DRAGON_EGG, targetCount);
        _count = targetCount;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        // No cleanup needed
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof CollectDragonEgg;
    }

    @Override
    protected String toDebugString() {
        return "Collecting Dragon Egg";
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
        // eggPos = Optional.empty();
        return;
    }


    @Override
    protected Task onResourceTick(AltoClef mod) {

        // if mod.getItemStorage().hasItem(Items.BOW)

        if (_eggTimer != null) {
            if (_eggTimer.elapsed()) {
                _isDroppingTheEgg = false;
            }
        }

        if (_isDroppingTheEgg) {
            if (mod.getEntityTracker().itemDropped(Items.DRAGON_EGG)) {
                _isDroppingTheEgg = false;
            }
            if (eggPos.isPresent() && mod.getWorld().getBlockState(eggPos.get().down()).isAir()) {
                setDebugState("Waiting for the egg to drop.");
                // Debug.logMessage("Waiting for the egg to drop.");
                return null;
            }
        }

        // mod.getEntityTracker().trackEntity(EntityType.FALLING_BLOCK);
        if (mod.getEntityTracker().entityFound(EntityType.FALLING_BLOCK.getClass())) {
            setDebugState("Waiting for the falling egg to settle.");
            return null;
        }
        if (!_isDroppingTheEgg && _eggTimer.elapsed()) {
            if (!mod.getBlockTracker().isTracking(Blocks.DRAGON_EGG)
                    && !mod.getBlockTracker().anyFound(Blocks.DRAGON_EGG)
                    && (eggPos.isEmpty()
                            || ifNotNull(!mod.getWorld().getBlockState(eggPos.get()).isOf(Blocks.DRAGON_EGG)))) {
                mod.getBlockTracker().trackBlock(Blocks.DRAGON_EGG);
                setDebugState("Tracking the egg.");
            }
        }
        if (!_isDroppingTheEgg) {
        eggPos = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.DRAGON_EGG);
        }
        // if (!eggPos.isEmpty()) {
        //     mod.getBlockTracker().stopTracking(Blocks.DRAGON_EGG);
        // }
        setDebugState("No egg seems to exist...");
        if (mod.getBlockTracker().anyFound(Blocks.DRAGON_EGG)) {
            setDebugState("Found egg");
            // setDebugState("Found egg at " + eggPos.get());
            if (eggPos.isPresent()) {
                BlockPos egg = eggPos.get();
                Block blockUnderEgg = mod.getWorld().getBlockState(egg.down()).getBlock();
                Block blockTwoUnderEgg = mod.getWorld().getBlockState(egg.down(2)).getBlock();
                Block[] blocksToBreak = { blockTwoUnderEgg, blockUnderEgg };

                if (Arrays.stream(blocksToBreak).anyMatch(block -> block == Blocks.BEDROCK
                        || blockUnderEgg == Blocks.END_PORTAL_FRAME || blockUnderEgg.getHardness() > 6000)) {
                    setDebugState("Moving the egg.");
                    // If the block under the egg is unbreakable, right-click the egg
                    return new InteractWithBlockTask(egg);
                } else {
                    if (WorldHelper.getCurrentDimension() != Dimension.END) {

                        MiningRequirement highestRequirement = MiningRequirement.HAND;
                        for (Block block : blocksToBreak) {
                            MiningRequirement requirement = MiningRequirement.getMinimumRequirementForBlock(block);
                            if (requirement.ordinal() > highestRequirement.ordinal()) {
                                highestRequirement = requirement;
                            }
                        }
                        Item tool = highestRequirement.getMinimumPickaxe();
                        if (!mod.getItemStorage().hasItem(tool)) {
                            setDebugState(
                                    "Collecting tool to break blocks with highest requirement: " + highestRequirement);
                            return TaskCatalogue.getItemTask(tool, 1);
                        }
                    }
                }
                if (blockTwoUnderEgg != Blocks.TORCH && !_isDroppingTheEgg && blockUnderEgg != Blocks.AIR) {
                    // If the block two under the egg is not a torch, place a torch
                    setDebugState("Placing a torch to break the egg with.");
                    BlockPos torchPos = egg.down(2);
                    BlockPos blockBelowTorch = torchPos.down();
                    int blocksDown = 0;
                    while (mod.getWorld().getBlockState(blockBelowTorch).isAir()) {
                        blocksDown++;
                        blockBelowTorch = blockBelowTorch.down();
                    }
                    
                    if (mod.getItemStorage().getItemCount(Items.DIRT) < blocksDown) {
                        setDebugState("Collecting " + blocksDown + " dirt blocks to place below the torch.");
                        return TaskCatalogue.getItemTask(Items.DIRT, blocksDown);
                    }
                    
                    BlockPos lowestPos = torchPos.down(blocksDown);
                    for (int i = 0; i < blocksDown; i++) {
                        if (mod.getWorld().getBlockState(lowestPos).isAir() && mod.getItemStorage().hasItem(Items.TORCH)) {
                            return new PlaceBlockTask(lowestPos, Blocks.DIRT);
                        }
                        lowestPos = lowestPos.up();
                    }
                    
                    if (!mod.getItemStorage().hasItem(Items.TORCH)) {
                        setDebugState("Collecting a torch.");
                        return TaskCatalogue.getItemTask(Items.TORCH, 1);
                    }
                    BlockState blockBelowTorchState = mod.getWorld().getBlockState(torchPos.down());
                    if (blockBelowTorchState.isSolidBlock(mod.getWorld(), torchPos.down()) && blockBelowTorchState.getBlock() != Blocks.TORCH) {
                        mod.getExtraBaritoneSettings().avoidBlockBreak(torchPos.down());
                        return new PlaceBlockTask(torchPos, Blocks.TORCH);
                    } else {
                        return new DestroyBlockTask(torchPos.down());
                    }
                } else if (blockUnderEgg != Blocks.AIR) {
                    _isDroppingTheEgg = true;
                    // Debug.logMessage("Dropping the egg.");
                    _eggTimer.reset();
                    setDebugState("Breaking the block under the egg.");
                    // If the block under the egg is not air, break it
                    return new DestroyBlockTask(egg.down());
                }
                else if (blockUnderEgg == Blocks.AIR) {
                    
                    setDebugState("Waiting for the egg to drop.");
                    // If the block under the egg is air, break the egg
                    return new IdleTask();
                }
            }
        }
        return null;
    }

    private boolean isEgg(Block block) {
        try {
            return block == Blocks.DRAGON_EGG;
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean ifNotNull(Predicate<Object> predicate) {
        try {
            return predicate != null && predicate.test(null);
        } catch (NullPointerException e) {
            return false;
        }
    }

    private boolean ifNotNull(boolean test) {
        try {
            return test;
        } catch (NullPointerException e) {
            return false;
        }
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.DRAGON_EGG);
        eggPos = Optional.empty();
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectDragonEgg;
    }

    @Override
    protected String toDebugStringName() {
        // TODO Auto-generated method stub
        return "Getting the Egg";
    }
}
