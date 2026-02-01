package net.ozanarchy.ozanarchyTowns.commands;

import net.ozanarchy.ozanarchyTowns.util.Utils;
import net.ozanarchy.ozanarchyTowns.events.MemberEvents;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class TownBankCommands implements CommandExecutor {
    private final MemberEvents mEvents;
    private String prefix = Utils.prefix();
    private String noPerm = config.getString("messages.nopermission");
    private String incorrectUsage = config.getString("messages.incorrectusage");

    public TownBankCommands(MemberEvents mEvents) {
        this.mEvents = mEvents;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player p)) return true;
        if(args.length < 1){
            p.sendMessage(Utils.getColor(prefix + incorrectUsage));
        }
        switch (args[0].toLowerCase()) {
            case "deposit" -> {
                mEvents.giveTownMoney(p, args);
                return true;
            }
            case "withdraw" -> {
                mEvents.withdrawTownMoney(p, args);
                return true;
            }
            case "balance", "bal" -> {
                mEvents.townBalance(p);
                return true;
            }
            case "help", "commands" -> {
                helpCommand(p);
            }
        }
        return true;
    }

    private void helpCommand(Player p){
        p.sendMessage(Utils.getColor(prefix + "&e&lBank Help Menu"));
        for (String line : config.getStringList("bankhelp")){
            p.sendMessage(Utils.getColor(line));
        }
    }
}
