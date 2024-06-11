package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.YutaBeatMinecraftTask;

public class YutaCommand extends Command {
    public YutaCommand() {
        super("yuta", "Beats the game, but with percautions and the help of Marvion!");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new YutaBeatMinecraftTask(), this::finish);
    }
}