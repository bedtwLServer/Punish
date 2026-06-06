package com.bedtwlserver.punish.core.command;

import com.bedtwlserver.punish.core.Punish;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class CommandBase implements CommandExecutor, TabCompleter {

    protected Punish plugin = Punish.instance;

    protected abstract void execute(@NonNull CommandSender sender, @NonNull String label, String @NonNull [] args);

    protected abstract List<String> getTabCompletions(@NonNull CommandSender sender, String @NonNull [] args);

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        this.execute(sender, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String @NonNull [] args) {
        return complete(
                args,
                args.length - 1,
                getTabCompletions(sender, args)
        );
    }

    protected List<String> complete(
            String[] args,
            int index,
            Iterable<String> source
    ) {
        List<String> result = new ArrayList<>();

        if (args.length <= index) {
            return result;
        }

        String input = args[index].toLowerCase();

        for (String value : source) {
            if (value.toLowerCase().startsWith(input)) {
                result.add(value);
            }
        }

        return result;
    }

    protected String color(String str) {
        return Punish.instance.color(str);
    }

}
