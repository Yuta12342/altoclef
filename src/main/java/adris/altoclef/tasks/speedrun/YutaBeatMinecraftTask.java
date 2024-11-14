package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.SleepThroughNightTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.item.Item;
import adris.altoclef.util.ItemTarget;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

import static adris.altoclef.commands.DepositCommand.getAllNonEquippedOrToolItemsAsTarget;
import static adris.altoclef.tasks.speedrun.MarvionBeatMinecraftTask.dragonIsDead;
import static net.minecraft.client.MinecraftClient.getInstance;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class YutaBeatMinecraftTask extends Task {

    public boolean _GoingForItems = false;
    private Item[] _ImportantItems = { Items.SHIELD, Items.IRON_SWORD, Items.STONE_PICKAXE, Items.STONE_AXE,
            Items.WOODEN_PICKAXE };
    private Task _goodGear;
    private Task _Marvion = new MarvionBeatMinecraftTask();
    private Task _resourceGet = new GetBuildingMaterialsTask(99999999);
    private boolean resourceGet = false;
    private boolean justStarted = true;
    public boolean dieded = false;
    private boolean tooMuchShit = false;
    private ItemTarget[] BloatedInventory = null;
    public static boolean resetStoreTask = false;
    private boolean hasStartedTheSpeedrun = false;
    private boolean sleepNow = false;
    private final TimerGame stockUpTimer = new TimerGame(50);
    private final TimerGame startupTimer = new TimerGame(80);
    private final TimerGame getMaterialTimer = new TimerGame(80);
    private boolean stuckInChestLoop = false;
    private boolean getChest = true;
    private boolean placingChest = false;
    private final TimerGame bedPlace = new TimerGame(5);
    private final TimerGame screenTime = new TimerGame(20);
    private boolean checkItems;

    // Method to add an item to the inventory
    private ItemTarget[] addToInventory(ItemTarget[] inventory, ItemTarget item) {
        ItemTarget[] newInventory = Arrays.copyOf(inventory, inventory.length + 1);
        newInventory[inventory.length] = item;
        return newInventory;
    }

    protected void onStart(AltoClef mod) {

    }

    protected Task onTick(AltoClef mod) {
        boolean hasProtection = mod.getItemStorage().hasItem(Items.SHIELD);
        boolean hasGoodWeapon = mod.getItemStorage().hasItem(Items.IRON_SWORD);
        boolean HasGoodPickaxe = mod.getItemStorage().hasItem(Items.STONE_PICKAXE);
        boolean HasGoodAxe = mod.getItemStorage().hasItem(Items.STONE_AXE);
        boolean HasBackupPickaxe = mod.getItemStorage().hasItem(Items.WOODEN_PICKAXE);
        // BlockPos playerPos = getPlayer().getBlockPos();

        // if (!hasGoodWeapon && !hasProtection && !HasBackupPickaxe && !HasGoodAxe &&
        // !HasGoodPickaxe) {
        // setDebugState("Grabbing some important items, to prevent travel issues...");
        // ItemTarget GoodStuff = new ItemTarget(_ImportantItems);
        // _goodGear = TaskCatalogue.getItemTask(GoodStuff);
        // _GoingForItems = true;
        // return _goodGear;
        // }
        // else {
        // _goodGear = null;
        // _GoingForItems = false;
        // }
        /*
         * if (getInstance().crosshairTarget == EndermanEntity &&
         * mod.getItemStorage().hasItem(Items.ENDER_EYE))
         * setDebugState("May wanna not look them in the eye...");
         * return new SafeRandomShimmyTask();
         * 
         */

        if (!(WorldHelper.getCurrentDimension() == Dimension.END)) {
            if (MinecraftClient.getInstance().currentScreen instanceof DeathScreen) {
                setDebugState("Ouch...");
                return new SleepThroughNightTask();
            }

            if (resourceGet && getMaterialTimer.elapsed()) {
                resourceGet = false;
            }

            if (resourceGet && mod.getItemStorage().hasEmptyInventorySlot() && !tooMuchShit
                    && !WorldHelper.canSleep()) {
                if (!mod.getItemStorage().hasItem(Items.WOODEN_SHOVEL)) {
                    setDebugState("Getting Materials... but getting a Shovel, just in case. ("
                            + (80 - getMaterialTimer.getDuration()) + "seconds)");
                    return TaskCatalogue.getItemTask(Items.WOODEN_SHOVEL, 1);
                }
                setDebugState("Getting materials for " + (80 - getMaterialTimer.getDuration()) + " seconds");
                return _resourceGet;
            }

            if (startupTimer.elapsed() && justStarted) {
                getMaterialTimer.reset();
                resourceGet = true;
                justStarted = false;
            }

            if (!WorldHelper.canSleep()) {
                bedPlace.reset();

            }

            if (sleepNow && bedPlace.elapsed()) {
                setDebugState("Making sure we sleep...");
                return new SleepThroughNightTask();
            }
            // if ()
            if (!WorldHelper.canSleep() && sleepNow)
                sleepNow = false;
            if (WorldHelper.canSleep() && WorldHelper.getCurrentDimension() == Dimension.OVERWORLD
                    && mod.getItemStorage().hasItem(ItemHelper.BED)) {
                sleepNow = true;
                setDebugState("Sleeping immediately, instead of wasting time.");
                return new PlaceBlockNearbyTask(ItemHelper.itemsToBlocks(ItemHelper.BED));
            }

            /*
             * if (!hasStartedTheSpeedrun) {
             * if (!mod.getItemStorage().hasItem(Items.CHEST)) {
             * setDebugState("Starting off with a few Chests...");
             * return TaskCatalogue.getItemTask(Items.CHEST, 1);
             * }else{
             * Debug.
             * logMessage("Done getting chests for potential Inventory clutter management."
             * );
             * hasStartedTheSpeedrun = true;
             * 
             * }
             * }
             */

            if (getChest) {
                if (!mod.getItemStorage().hasItem(Items.CHEST)) {
                    setDebugState("Getting a chest, instead of travelling to an existing one...");
                    return TaskCatalogue.getItemTask(Items.CHEST, 1);
                } else {
                    getChest = false;
                    stockUpTimer.reset();
                }
            }

            if (stuckInChestLoop) {
                if (mod.getItemStorage().hasItem(Items.CHEST)) {
                    placingChest = true;
                    setDebugState("Nearest chest may be too far, or we may be stuck, placing Chest.");
                    return new PlaceBlockNearbyTask(Block.getBlockFromItem(Items.CHEST));
                } else {
                    if (placingChest) {
                        placingChest = false;
                        stuckInChestLoop = false;

                    } else {
                        getChest = true;
                    }

                }
            }
            if (tooMuchShit && mod.getItemStorage().hasItem(Items.CHEST) && !stuckInChestLoop) {
                stockUpTimer.forceElapse();
            }

            if ((stockUpTimer.elapsed()) && tooMuchShit) {
                Debug.logWarning("Creating a Chest to speed things up.");
                stuckInChestLoop = true;

            }

            if (!mod.getItemStorage().hasEmptyInventorySlot() || tooMuchShit) {
                // Check if the current inventory is different from the previously stored
                // BloatedInventory
                ItemTarget[] currentInventory = getAllNonEquippedOrToolItemsAsTarget(mod);
                if (BloatedInventory == null || !Arrays.equals(BloatedInventory, currentInventory)) {
                    if (!tooMuchShit || checkItems) {
                        if (!tooMuchShit) {
                            stockUpTimer.reset();
                            getChest = true;
                        }

                        BloatedInventory = currentInventory;
                        // Check for lower quality pickaxes and swords
                        Item[] pickaxes = new Item[] {
                                Items.WOODEN_PICKAXE,
                                Items.STONE_PICKAXE,
                                Items.IRON_PICKAXE,
                                Items.DIAMOND_PICKAXE,
                                Items.NETHERITE_PICKAXE
                        };

                        Item[] swords = new Item[] {
                                Items.WOODEN_SWORD,
                                Items.STONE_SWORD,
                                Items.IRON_SWORD,
                                Items.DIAMOND_SWORD,
                                Items.NETHERITE_SWORD
                        };

                        Item[] filteredPickaxes = Arrays.stream(pickaxes)
                                .filter(pickaxe -> mod.getItemStorage().hasItem(pickaxe))
                                .toArray(Item[]::new);

                        Item[] filteredSwords = Arrays.stream(swords)
                                .filter(sword -> mod.getItemStorage().hasItem(sword))
                                .toArray(Item[]::new);

                        for (Item pickaxe : pickaxes) {
                            if (mod.getItemStorage().hasItem(pickaxe)) {
                                BloatedInventory = addToInventory(BloatedInventory, new ItemTarget(pickaxe, 1));
                            }
                        }

                        for (Item sword : swords) {
                            if (mod.getItemStorage().hasItem(sword)) {
                                BloatedInventory = addToInventory(BloatedInventory, new ItemTarget(sword, 1));
                            }
                        }
                        currentInventory = BloatedInventory;
                        checkItems = false;
                    }

                    tooMuchShit = true;

                    if (resetStoreTask) {
                        if (BloatedInventory != currentInventory) {
                            stockUpTimer.reset();
                            checkItems = true;
                        }

                        resetStoreTask = false;
                        Debug.logMessage("Checking for any extra pickups.");
                    }
                }
            }

            if (tooMuchShit) {
                if (!Arrays.stream(getAllNonEquippedOrToolItemsAsTarget(mod)).toList().isEmpty()) {
                    // if (mod.getItemStorage().hasItem(Items.CHEST) &&
                    // mod.getBlockTracker().getNearestTracking().BlockPos) {
                    //
                    // }
                    setDebugState("Too much! (Preventing Item Get Loops) - " + (50 - stockUpTimer.getDuration()) + "s");
                    return new StoreInAnyContainerTask(false, BloatedInventory);
                } else {
                    // If there are no items remaining from BloatedInventory, resume normal
                    // functions
                    tooMuchShit = false;
                    BloatedInventory = null;
                    Debug.logMessage("Done storing items.");
                }
            }

            if (!mod.getItemStorage().hasItem(Items.STONE_SWORD) && !mod.getItemStorage().hasItem(Items.IRON_SWORD)
                    && !mod.getItemStorage().hasItem(Items.DIAMOND_SWORD)) {
                setDebugState("Getting the easiest Sword for Combat, while not being too cheap.");
                return TaskCatalogue.getItemTask(Items.STONE_SWORD, 2);
            }
            if (!hasProtection) {
                setDebugState("Getting Protection!");
                return TaskCatalogue.getItemTask(Items.SHIELD, 2);
            }

            if (!hasGoodWeapon && !mod.getItemStorage().hasItem(Items.DIAMOND_SWORD)) {
                setDebugState("The best way to fight, is with... swords. Getting at least 1 Sword...");
                return TaskCatalogue.getItemTask(Items.IRON_SWORD, 2);
            }
            if (!HasGoodPickaxe && !mod.getItemStorage().hasItem(Items.IRON_PICKAXE)
                    && !mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE)) {
                setDebugState("Making sure I can move through the ground smoothly, no matter what!");
                if (WorldHelper.getCurrentDimension() == Dimension.NETHER) {
                    setDebugState("Just to be safe, going back for tools...");
                    return new DefaultGoToDimensionTask(Dimension.OVERWORLD);
                }
                return TaskCatalogue.getItemTask(Items.STONE_PICKAXE, 2);
            }
            if (!HasGoodAxe) {
                setDebugState("...");
                return TaskCatalogue.getItemTask(Items.STONE_AXE, 1);
            }
            if (!HasBackupPickaxe) {
                setDebugState("Just making sure there is always a way to move through the ground...");
                return TaskCatalogue.getItemTask(Items.WOODEN_PICKAXE, 1);
            }
            if (!mod.getItemStorage().hasItem(ItemHelper.BED) && !mod.getItemStorage().hasItem((Items.SHEARS)))
                if (WorldHelper.canSleep()) {
                    setDebugState("Going to sleep, but first, prepping shears to make it easier to get Wool. :D");
                    return TaskCatalogue.getItemTask(Items.SHEARS, 1);
                }
        }
        setDebugState("Playing the Game!");
        return _Marvion;

    }

    protected void onStop(AltoClef mod, Task interruptTask) {
        Debug.logMessage("Stopped playing the game!");
    }

    public boolean isFinished(AltoClef mod) {
        // Check if the current screen is the CreditsScreen
        if (getInstance().currentScreen instanceof CreditsScreen) {
            return true;
        }

        // Check if the dragon is dead in the Overworld
        if (WorldHelper.getCurrentDimension() == Dimension.OVERWORLD && dragonIsDead) {
            return true;
        }
        return false;
    }

    @Override
    public void interrupt(AltoClef mod, Task interruptTask) {
        super.interrupt(mod, interruptTask);
        if (resourceGet && getMaterialTimer.getDuration() != 0) {
            // getMaterialTimer.reset();
        }
    }

    protected boolean isEqual(Task other) {
        Debug.logMessage("Speedrun already in motion!");
        setDebugState("Remembering what I was doing...");
        return other instanceof YutaBeatMinecraftTask;
    }

    protected String toDebugString() {
        return "Playing the Game with a bit more smarts (ft Marvion)";
    }
}
