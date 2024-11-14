package adris.altoclef.tasks.resources;

import java.util.Optional;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.CraftInInventoryTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.*;
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
import adris.altoclef.tasks.construction.*;;






public class CollectDragonEgg extends ResourceTask {
/* 
    private enum State {
        MOVE_TO_POSITION,
        BREAK_BLOCK,
        PLACE_TORCH,
        BREAK_EGG
    }
    
    private State currentState = State.MOVE_TO_POSITION;
    */ 

    public CollectDragonEgg(ItemTarget[] itemTargets) {
        super(itemTargets);
        //TODO Auto-generated constructor stub
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
        return;
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // TODO Auto-generated method stub
        if (mod.getBlockTracker().anyFound(Blocks.DRAGON_EGG)) {
            Optional<BlockPos> eggPos = mod.getBlockTracker().getNearestTracking(mod.getPlayer().getPos(), Blocks.DRAGON_EGG);
            if (eggPos.isPresent()) {
                BlockPos egg = eggPos.get();
                Block blockUnderEgg = mod.getWorld().getBlockState(egg.down()).getBlock();
                Block blockTwoUnderEgg = mod.getWorld().getBlockState(egg.down(2)).getBlock();
    
                if (blockUnderEgg == Blocks.BEDROCK || blockUnderEgg == Blocks.END_PORTAL_FRAME) {
                    // If the block under the egg is unbreakable, right-click the egg
                    return new InteractWithBlockTask(egg);
                } else if (blockTwoUnderEgg != Blocks.TORCH) {
                    // If the block two under the egg is not a torch, place a torch
                    Block[] Torch = {Blocks.TORCH};
                    return new PlaceBlockTask(egg.down(2), Torch, false, true);
                } else if (blockUnderEgg != Blocks.AIR) {
                    // If the block under the egg is not air, break it
                    return new DestroyBlockTask(egg.down());
                }
            }
        }
        return null;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        return;
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
