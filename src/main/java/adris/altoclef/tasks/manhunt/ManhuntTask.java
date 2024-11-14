package adris.altoclef.tasks.manhunt;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.SearchChunksExploreTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.PlayerUtils;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import dev.babbaj.pathfinder.xz.s;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import java.util.List;
import adris.altoclef.tasks.movement.GetToChunkTask;
import adris.altoclef.tasks.movement.SearchChunksExploreTask;

import com.mojang.authlib.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Arrays;

public class ManhuntTask extends Task {

    private enum Phase {
        TRACK,
        ATTACK,
        DEFEND,
        PREPARE
    }

    private Phase currentPhase = Phase.TRACK;
    private ServerPlayerEntity targetPlayer;
    private int preparationState = 0;
    private final int maxPreparationState = 3; // Define the maximum preparation state
    private TimerGame preparationTimer;
    private Item[] buildingBlocks;
    private TimerGame goldenAppleCooldown;
    public static ManhuntTask instance;

    private MinecraftServer server;

    boolean goingToEnd = false;

    private PrepTable prepTable = new PrepTable();

    @Override
    protected void onStart(AltoClef mod) {
        instance = this;
        // if (mod.getPlayer().getCommandSource().hasPermissionLevel(2)) {
        // Debug.logError("Manhunt mode cannot be enabled without certain
        // permissions.");
        // this.stop(mod, null);
        // return;
        // }

        // Define the building blocks
        buildingBlocks = mod.getModSettings().getThrowawayItems(mod, true);

        // Start the preparation timer
        preparationTimer = new TimerGame(300);
        goldenAppleCooldown = new TimerGame(5);

        // Add preparation sets to the preparation table
        prepTable.addPrepSet(0,
                new ItemTarget[] { new ItemTarget(Items.IRON_HELMET), new ItemTarget(Items.IRON_CHESTPLATE),
                        new ItemTarget(Items.IRON_LEGGINGS), new ItemTarget(Items.IRON_BOOTS) },
                new ItemTarget[] { new ItemTarget(Items.IRON_SWORD) });
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (server == null) {
            server = mod.getPlayer().getServer();
        }
        // Armor.
        Item[] bestArmor = getBestArmor(mod);
        if (bestArmor != null) {
            for (Item armorPiece : bestArmor) {
                if (armorPiece == null) {
                    continue;
                }
                boolean isEquipped = false;
                for (int i = 0; i < 4; i++) {
                    if (mod.getPlayer().getInventory().getArmorStack(i).getItem() == armorPiece) {
                        isEquipped = true;
                        break;
                    }
                }
                if (!isEquipped) {
                    return new EquipArmorTask(armorPiece);
                }
            }
        }

        if ((mod.getPlayer().getHealth() < 5) || mod.getPlayer().getStatusEffects().stream()
                .anyMatch(effect -> effect.getEffectType().getCategory() == StatusEffectCategory.HARMFUL)) {
            currentPhase = Phase.DEFEND;
        }

        switch (currentPhase) {
            case TRACK:
                return trackPlayer(mod);
            case ATTACK:
                return attackPlayer(mod);
            case DEFEND:
                return defendSelf(mod);
            case PREPARE:
                return prepare(mod);
        }
        if (preparationTimer.elapsed()) {
            currentPhase = Phase.PREPARE;
            preparationTimer.reset();
        }
        return null;
    }

    public static Dimension getPlayerDimension(PlayerEntity player) {
        if (player.getWorld().getDimension().ultrawarm())
            return Dimension.NETHER;
        if (player.getWorld().getDimension().natural())
            return Dimension.OVERWORLD;
        return Dimension.END;
    }

    private Task getToEndTask(AltoClef mod) {
        // Skip to the End.
        setDebugState("A Player is in the End, going to the End, immediately.");
        if (!mod.getItemStorage().hasItem(Items.WATER_BUCKET)) {
            return TaskCatalogue.getItemTask(Items.WATER_BUCKET, 1);
        }

        if (mod.getItemStorage().getItemCount(Items.COBBLESTONE) < 40) {
            return TaskCatalogue.getItemTask(Items.COBBLESTONE, 40);
        }
        if (!goingToEnd) {
            goingToEnd = true;
            mod.getPlayer().networkHandler.sendCommand("execute in minecraft:the_end run tp @s 0 100 0");
        }
        return null;
    }

    private Task dimensionCheck(AltoClef mod, PlayerEntity targetPlayer) {
        // Check if the player is in the same dimension
        Dimension playerDimension = WorldHelper.getCurrentDimension();
        Dimension targetDimension = getPlayerDimension(targetPlayer);

        if (playerDimension != targetDimension) {
            setDebugState("Player is in a different dimension, going to the player...");
            if (targetDimension != Dimension.END) {
                return new DefaultGoToDimensionTask(targetDimension);
            } else {
                return getToEndTask(mod);
            }
        }
        return null;
    }

    private Task dimensionChecker(AltoClef mod) {
        // Check if the player is in the same dimension
        Dimension playerDimension = WorldHelper.getCurrentDimension();
        List<Dimension> targetDimensions = new ArrayList<>();

        for (PlayerEntity player : mod.getWorld().getPlayers()) {
            targetDimensions.add(getPlayerDimension(player));
        }

        if (targetDimensions.contains(Dimension.END)) {
            return getToEndTask(mod);
        }

        for (Dimension targetDimension : targetDimensions) {
            if (playerDimension != targetDimension) {
                setDebugState("Player is in a different dimension, going to the player...");
                return new DefaultGoToDimensionTask(targetDimension);
            }
        }
        return null;
    }

    public void onServerTick(IntegratedServer server) {
        this.server = server;
        if (server.getPlayerManager() == null)
            server.setPlayerManager(null);
        // Debug.logMessage("Server ticked");
    }

    private Task trackPlayer(AltoClef mod) {
        // Logic to track the nearest player
        try {
            List<ServerPlayerEntity> allPlayers;
            PlayerEntity self = server.getPlayerManager().getPlayer(mod.getPlayer().getUuid());
            allPlayers = server.getPlayerManager().getPlayerList();
            double closestDistance = Double.MAX_VALUE;

            for (ServerPlayerEntity player : allPlayers) {
                if (player != self) {
                    double distance = player.squaredDistanceTo(mod.getPlayer());
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        targetPlayer = player;
                    }
                }
            }
            if (targetPlayer != null) {
                currentPhase = Phase.ATTACK;
            } else {
                setDebugState("No players found, checking dimensions...");
                return dimensionChecker(mod);
            }
        } catch (Exception e) {
            setDebugState("Waiting for Server... " + e.getMessage());
            Debug.logMessage("Waiting for Server... " + e.getMessage());
            // this.stop(mod);
            return null;
        }
        return null;
    }

    private Task WanderChunkTask(BlockPos chunkPos) {
        Dimension targetDimension = getPlayerDimension(targetPlayer);
        if (WorldHelper.getCurrentDimension() != targetDimension) {
            setDebugState("Player is in a different dimension, going to the player...");
            return new DefaultGoToDimensionTask(targetDimension);
        }
        setDebugState("Finding Player at " + chunkPos);
        return new GetToChunkTask(new ChunkPos(chunkPos));
    }

    private Task attackPlayer(AltoClef mod) {
        if (targetPlayer == null || targetPlayer.isDead()) {
            currentPhase = Phase.TRACK;
            return null;
        }

        // Logic to attack the player
        BlockPos targetPos = targetPlayer.getBlockPos();
        // Check if health is low and switch to DEFEND phase

        if (mod.getEntityTracker().isPlayerLoaded(targetPlayer.getName().getString())) {
            setDebugState("Killing player: " + targetPlayer.getName().getString() + " at " + targetPos);
            return new KillPlayerTask(targetPlayer.getName().getString());
        } else {
            BlockPos chunkPos = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            if (mod.getPlayer().getChunkPos().equals(new ChunkPos(chunkPos))) {
                setDebugState("Wandering in chunk: " + chunkPos); } else {setDebugState("Going to Chunk: " + chunkPos);}
                return WanderChunkTask(chunkPos);
            }
        }
                

    private Task defendSelf(AltoClef mod) {
        // Logic to defend itself, such as eating food or using a shield
        if (mod.getItemStorage().hasItem(Items.GOLDEN_APPLE) && goldenAppleCooldown.elapsed()) {
            mod.getPlayer().eatFood(mod.getWorld(), Items.GOLDEN_APPLE.getDefaultStack());
            goldenAppleCooldown.reset();
        }

        if (mod.getPlayer().getStatusEffects().stream()
                .anyMatch(effect -> effect.getEffectType().getCategory() == StatusEffectCategory.HARMFUL))
            if (mod.getItemStorage().hasItem(Items.MILK_BUCKET)) {
                mod.getPlayer().eatFood(mod.getWorld(), Items.MILK_BUCKET.getDefaultStack());
            }

        // Switch back to ATTACK phase if health is restored
        if (mod.getPlayer().getHealth() > 15) {
            currentPhase = Phase.ATTACK;
        }

        return null;
    }

    private Item[] getBestArmor(AltoClef mod) {
        Item[] leatherArmor = new Item[] { Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS,
                Items.LEATHER_BOOTS };
        Item[] goldArmor = new Item[] { Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS,
                Items.GOLDEN_BOOTS };
        Item[] chainmailArmor = new Item[] { Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE,
                Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS };
        Item[] ironArmor = new Item[] { Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS,
                Items.IRON_BOOTS };
        Item[] diamondArmor = new Item[] { Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS,
                Items.DIAMOND_BOOTS };
        Item[] netheriteArmor = new Item[] { Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
                Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS };

        Item[][] armorSets = new Item[][] { leatherArmor, goldArmor, chainmailArmor, ironArmor, diamondArmor,
                netheriteArmor };

        Item[] bestArmor = new Item[4];

        for (int i = armorSets.length - 1; i >= 0; i--) {
            for (int j = 0; j < armorSets[i].length; j++) {
                if (mod.getItemStorage().hasItem(armorSets[i][j])) {
                    bestArmor[j] = armorSets[i][j];
                }
            }
        }

        // Check if no armor was found
        boolean hasArmor = Arrays.stream(bestArmor).anyMatch(item -> item != null);
        return hasArmor ? bestArmor : null;
    }

    private Task prepare(AltoClef mod) {
        // Equip the best available armor
        // equipBestArmor(mod);

        // Logic to prepare by collecting items
        switch (preparationState) {
            case 0:
                if (!mod.getItemStorage().hasItem(Items.IRON_SWORD)) {
                    return TaskCatalogue.getItemTask(Items.IRON_SWORD, 1);
                }
                preparationState++;
                break;
            case 1:
                if (!mod.getItemStorage().hasItem(Items.SHIELD)) {
                    return TaskCatalogue.getItemTask(Items.SHIELD, 1);
                }
                preparationState++;
                break;
            case 2:
                if (!mod.getItemStorage().hasItem(Items.GOLDEN_APPLE)) {
                    return TaskCatalogue.getItemTask(Items.GOLDEN_APPLE, 5);
                }
                preparationState++;
                break;
            default:
                preparationState = maxPreparationState;
                break;
        }

        // If preparation is complete, switch back to TRACK phase
        if (preparationState >= maxPreparationState) {
            currentPhase = Phase.TRACK;
            preparationState = 0;
        }

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        // Cleanup code here
        if (preparationTimer != null) {
            preparationTimer.reset();
        }
        if (goldenAppleCooldown != null) {
            goldenAppleCooldown.reset();
        }
        instance = null;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ManhuntTask;
    }

    @Override
    protected String toDebugString() {
        return "Manhunt Mode";
    }
}