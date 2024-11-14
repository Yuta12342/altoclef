package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.manhunt.ManhuntTask;

public class ManhuntCommand extends Command {
    public ManhuntCommand() {
        super("manhunt", "Starts the Manhunt task to track and kill players.");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new ManhuntTask(), this::finish);
    }
}