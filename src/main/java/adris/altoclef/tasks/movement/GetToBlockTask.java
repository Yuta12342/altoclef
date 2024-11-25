package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.resources.LimitedGetItemTask;
import adris.altoclef.tasksystem.ITaskRequiresGrounded;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.WorldHelper;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.pathing.path.PathExecutor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class GetToBlockTask extends CustomBaritoneGoalTask implements ITaskRequiresGrounded {

    private final BlockPos _position;
    private final boolean _preferStairs;
    private final Dimension _dimension;

    private Item _requiredTool;
    private Task _toolTask;

    public GetToBlockTask(BlockPos position, boolean preferStairs) {
        this(position, preferStairs, null);
    }

    public GetToBlockTask(BlockPos position, Dimension dimension) {
        this(position, false, dimension);
    }

    public GetToBlockTask(BlockPos position, boolean preferStairs, Dimension dimension) {
        _dimension = dimension;
        _position = position;
        _preferStairs = preferStairs;
    }

    public GetToBlockTask(BlockPos position) {
        this(position, false);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (_dimension != null && WorldHelper.getCurrentDimension() != _dimension) {
            return new DefaultGoToDimensionTask(_dimension);
        }

        // Check if we need a tool
        if (mod.getItemStorage().hasItem(_requiredTool)) {
            _toolTask = null;
        }

        if (_toolTask != null) {
            if (!_toolTask.isFinished(mod)) {
            setDebugState("Getting tool.");
            return _toolTask;
            }
        }

        // Check Baritone's path for blocks that require specific tools
        PathExecutor pathExecutor = mod.getClientBaritone().getPathingBehavior().getCurrent();
        if (pathExecutor != null) {
            for (BlockPos pos : pathExecutor.getPath().positions()) {
                Block block = mod.getWorld().getBlockState(pos).getBlock();
                MiningRequirement requirement = MiningRequirement.getMinimumRequirementForBlock(block);
                if (requirement != MiningRequirement.HAND) {
                    Item requiredTool = requirement.getMinimumPickaxe();
                    if (!hasToolOrBetter(mod, requiredTool, requirement)) {
                        setDebugState("Getting required tool: " + requiredTool.getName().getString());
                        Debug.logMessage("Getting tool for block: " + block);
                        _toolTask = new LimitedGetItemTask(requiredTool, 1);
                        _requiredTool = requiredTool;
                        return TaskCatalogue.getItemTask(requiredTool, 1);
                    } else {
                        _toolTask = null;
                    }
                }
            }
        }

        return super.onTick(mod);
    }

    @Override
    protected void onStart(AltoClef mod) {
        super.onStart(mod);
        if (_preferStairs) {
            mod.getBehaviour().push();
            mod.getBehaviour().setPreferredStairs(true);
        }
    }


    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        super.onStop(mod, interruptTask);
        if (_preferStairs) {
            mod.getBehaviour().pop();
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetToBlockTask task) {
            return task._position.equals(_position) && task._preferStairs == _preferStairs && task._dimension == _dimension;
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return super.isFinished(mod) && (_dimension == null || _dimension == WorldHelper.getCurrentDimension());
    }

    @Override
    protected String toDebugString() {
        return "Getting to block " + _position + (_dimension != null ? " in dimension " + _dimension : "");
    }


    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalBlock(_position);
    }

    @Override
    protected void onWander(AltoClef mod) {
        super.onWander(mod);
        mod.getBlockTracker().requestBlockUnreachable(_position);
    }
}
