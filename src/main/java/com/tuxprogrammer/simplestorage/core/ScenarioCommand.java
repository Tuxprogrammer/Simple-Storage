package com.tuxprogrammer.simplestorage.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

import com.tuxprogrammer.simplestorage.SimpleStorage;

/**
 * The {@code /simplestoragenh} server command.
 *
 * <p>
 * Usage: {@code /simplestoragenh test [all|list|<test class>]}. Runs the in-game scenario runner, logs every
 * report line, and sends the player only the final summary line (via lang keys).
 *
 * <p>
 * Side: server, development only.
 */
public final class ScenarioCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "simplestoragenh";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "simplestoragenh.command.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1 || !args[0].equals("test")) {
            sender.addChatMessage(new ChatComponentTranslation("simplestoragenh.command.usage"));
            return;
        }
        String selector = args.length > 1 ? args[1] : "all";
        String[] lines = InGameHooks.runScenarios(selector);
        for (String line : lines) {
            SimpleStorage.LOG.info("[scenario] {}", line);
        }
        String lastLine = lines[lines.length - 1];
        sender.addChatMessage(new ChatComponentTranslation("simplestoragenh.command.test.summary", lastLine));
    }
}
