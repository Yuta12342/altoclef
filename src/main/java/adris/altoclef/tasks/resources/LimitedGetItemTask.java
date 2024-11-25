package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasksystem.Task;
import net.minecraft.item.Item;
import adris.altoclef.Settings;

import java.util.concurrent.atomic.AtomicInteger;

public class LimitedGetItemTask extends Task {

    private static final int MAX_INSTANCES = new Settings().getAllowedItemGetRecursionDepth();
    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger(0);

    private final Item _item;
    private final int _count;
    private Task _getItemTask;

    public LimitedGetItemTask(Item item, int count) {
        _item = item;
        _count = count;
    }

    @Override
    protected void onStart(AltoClef mod) {
        if (INSTANCE_COUNT.incrementAndGet() > MAX_INSTANCES) {
            Debug.logWarning("Too many attempts to get a tool! Must be stuck. Stopping all tasks.");
            mod.getTaskRunner().disable();
        } else {
            _getItemTask = TaskCatalogue.getItemTask(_item, _count);
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (_getItemTask != null && !_getItemTask.isFinished(mod)) {
            setDebugState("Attempting to get " + _count + " " + _item.getName().getString());
            return _getItemTask;
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        if (_getItemTask != null) {
            _getItemTask.stop(mod, interruptTask);
        }
        INSTANCE_COUNT.decrementAndGet();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof LimitedGetItemTask task) {
            return task._item == _item && task._count == _count;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "LimitedGetItemTask for " + _count + " " + _item.getName().getString();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _getItemTask != null && _getItemTask.isFinished(mod);
    }
}