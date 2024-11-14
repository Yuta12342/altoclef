package adris.altoclef.tasks.manhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
// import adris.altoclef.tasks.resources.CollectItemTask; // Removed unused import
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.WorldHelper;

public class PrepTable {

    private static class PrepSet {
        ItemTarget[] equipment;
        ItemTarget[] items;

        PrepSet(ItemTarget[] equipment, ItemTarget[] items) {
            this.equipment = equipment;
            this.items = items;
        }
    }

    private final Map<Integer, PrepSet> prepSets = new HashMap<>();

    public void addPrepSet(int index, ItemTarget[] equipment, ItemTarget[] items) {
        prepSets.put(index, new PrepSet(equipment, items));
    }

    public ItemTarget[] getEquipment(int index) {
        return prepSets.get(index).equipment;
    }

    public ItemTarget[] getItems(int index) {
        return prepSets.get(index).items;
    }

    public ItemTarget[] getAllItemsUpTo(int index) {
        List<ItemTarget> allItems = new ArrayList<>();
        for (int i = 0; i <= index; i++) {
            if (prepSets.containsKey(i)) {
                allItems.addAll(Arrays.asList(prepSets.get(i).items));
            }
        }
        return allItems.toArray(new ItemTarget[0]);
    }

    public ItemTarget[] getEquipmentAt(int index) {
        if (prepSets.containsKey(index)) {
            return prepSets.get(index).equipment;
        }
        return new ItemTarget[0];
    }
}