package net.ozanarchy.ozanarchyTowns.commands;

import net.ozanarchy.ozanarchyTowns.Utils;
import net.ozanarchy.ozanarchyTowns.events.MemberEvents;
import net.ozanarchy.ozanarchyTowns.events.TownEvents;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class TownsCommand implements CommandExecutor {
    private final DatabaseHandler db;
    private final TownEvents events;
    private final MemberEvents mEvents;
    private String prefix = Utils.prefix();
    private String noPerm = config.getString("messages.nopermission");
    private String incorrectUsage = config.getString("messages.incorrectusage");

    public TownsCommand(DatabaseHandler db, TownEvents events, MemberEvents mEvents) {
        this.db = db;
        this.events = events;
        this.mEvents = mEvents;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if(!p.hasPermission("oztowns.commands")){
            p.sendMessage(Utils.getColor(prefix + noPerm));
            return true;
        }

        if(db == null){
            p.sendMessage(Utils.getColor(prefix + "&cDatabase Error, Please seek an Admin."));
            return true;
        }

        if (args.length < 1){
            p.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return true;
        }

        switch (args[0].toLowerCase()){
            case "help" -> {
                helpCommand(p);
                return true;
            }
            case "claim" -> {
                if(p.hasPermission("oztowns.claim")){
                    events.claimLand(p);
                    return true;
                } else {
                    p.sendMessage(Utils.getColor(prefix + noPerm));
                    return true;
                }
            }
            case "create" -> {
                events.createTown(p, args);
                return true;
            }
            case "abandon" -> {
                if(args.length <2 || !args[1].toLowerCase().equals("confirm")){
                    p.sendMessage(Utils.getColor(prefix + "&aYou are trying to &c&lDELETE&a your town please do &f/towns abandon confirm"));
                    return true;
                }
                events.abandonTown(p.getUniqueId());
                return true;
            }
            case "unclaim" -> {
                events.removeChunk(p);
                return true;
            }
            case "add" -> {
                mEvents.addMember(p, args);
                return true;
            }
            case "remove" -> {
                mEvents.removeMember(p, args);
                return true;
            }
            case "promote" -> {
                mEvents.promotePlayer(p, args);
            }
            case "demote" -> {
                mEvents.demotePlayer(p, args);
            }
            case "leave" -> {
                mEvents.leaveTown(p);
            }
        }

        return true;
    }

    private void helpCommand(Player p){
        p.sendMessage(Utils.getColor(prefix + "&e&lHelp Menu"));
        for (String line : config.getStringList("help")){
            p.sendMessage(Utils.getColor(line));
        }
    }
}
