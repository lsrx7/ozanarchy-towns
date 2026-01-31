package net.ozanarchy.ozanarchyTowns.events;

import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import net.ozanarchy.ozanarchyTowns.Utils;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class MemberEvents implements Listener {
    private final DatabaseHandler db;
    private final OzanarchyTowns plugin;
    private final String prefix = Utils.prefix();
    private final String noPerm = config.getString("messages.nopermission");
    private final String incorrectUsage = config.getString("messages.usage");

    public MemberEvents(DatabaseHandler data, OzanarchyTowns plugin){
        this.db = data;
        this.plugin = plugin;
    }

    public void addMember(Player requester, String[] args){
        if(args.length < 2){
            requester.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null){
            requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotfound")));
            return;
        }

        UUID requesterUUID = requester.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Integer townId = db.getPlayerTownId(requesterUUID);

           if (townId == null){
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
               });
               return;
           }
           if (!db.isTownAdmin(requesterUUID, townId)) {
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + config.getString("messages.nottownadmin")));
               });
               return;
           }
           if (db.getPlayerTownId(targetUUID) != null){
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playeralreadyintown")));
               });
               return;
           }
           boolean success = db.addMember(townId, targetUUID, "MEMBER");
           Bukkit.getScheduler().runTask(plugin, () -> {
              if(success){
                  requester.sendMessage(Utils.getColor(prefix + "&aAdded &f" + target.getName() + " &ato the town."));
                  target.sendMessage(Utils.getColor(prefix + "&aYou have joined the town."));
              } else {
                  requester.sendMessage(Utils.getColor(prefix + "&cFailed to add the player."));
              }
           });
        });
    }
    public void removeMember(Player requester, String[] args){
        if(args.length <2){
            requester.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null){
            requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotfound")));
            return;
        }

        UUID requesterUUID = requester.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Integer townId = db.getPlayerTownId(requesterUUID);

           if (townId == null){
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
               });
               return;
           }
           if (!db.isTownAdmin(requesterUUID, townId)) {
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + config.getString("messages.nottownadmin")));
               });
               return;
           }
           Integer targetTown = db.getPlayerTownId(targetUUID);
           if (targetTown == null || !targetTown.equals(townId)){
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + "&cThe player is not in your town."));
               });
               return;
           }
           if(db.isMayor(targetUUID, townId)) {
               Bukkit.getScheduler().runTask(plugin, () -> {
                   requester.sendMessage(Utils.getColor(prefix + "&cYou cannot remove the Mayor."));
               });
               return;
           }

           boolean success = db.removeMember(targetUUID, townId);
           Bukkit.getScheduler().runTask(plugin, () -> {
              if(success){
                  requester.sendMessage(Utils.getColor(prefix + "&aRemoved &f" + target.getName() + " &ato the town."));
                  target.sendMessage(Utils.getColor(prefix + "&cYou have been removed from the town."));
              } else {
                  requester.sendMessage(Utils.getColor(prefix + "&cFailed to remove the player."));
              }
           });
        });
    }
    public void leaveTown(Player p){
        UUID uuid = p.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
           Integer townId = db.getPlayerTownId(uuid);
            if (townId == null){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            if(db.isMayor(uuid, townId)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + "&cThe mayor can not leave the town."));
                });
                return;
            }

            boolean success = db.removeMember(uuid, townId);

            Bukkit.getScheduler().runTask(plugin, () ->{
               if (success) {
                   p.sendMessage(Utils.getColor(prefix + config.getString("messages.lefttown")));
               } else {
                   p.sendMessage(Utils.getColor(prefix + "&cError leaving the town."));
               }
            });

        });
    }
    public void promotePlayer(Player requester, String[] args){
        if(args.length <2){
            requester.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotfound")));
            return;
        }

        UUID requesterUUID = requester.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           Integer townId = db.getPlayerTownId(requesterUUID);
            if (townId == null){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            if (!db.isMayor(requesterUUID, townId)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notmayor")));
                });
                return;
            }
            Integer targetTown = db.getPlayerTownId(targetUUID);
            if (targetTown == null || !targetTown.equals(townId)){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotinyourtown")));
                });
                return;
            }
            if(!db.isMemberRank(targetUUID, townId)){
                Bukkit.getScheduler().runTask(plugin, () ->{
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.cantpromote")));
                });
                return;
            }
            boolean success = db.setRole(targetUUID, townId, "OFFICER");
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success){
                    requester.sendMessage(Utils.getColor(prefix + "&aPromoted &f" + target.getName() + " &ato rank Officer."));
                    target.sendMessage(Utils.getColor(prefix + "&aYou have been promoted to rank Officer."));
                } else {
                    requester.sendMessage(Utils.getColor(prefix + "&cFailed to promote the player."));
                }
            });
        });
    }
    public void demotePlayer(Player requester, String[] args){
        if(args.length <2){
            requester.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotfound")));
            return;
        }

        UUID requesterUUID = requester.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Integer townId = db.getPlayerTownId(requesterUUID);
            if (townId == null){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            if (!db.isMayor(requesterUUID, townId)) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.notmayor")));
                });
                return;
            }
            if (requesterUUID.equals(targetUUID)) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        requester.sendMessage(Utils.getColor(prefix + "&cYou cannot demote yourself."))
                );
                return;
            }
            Integer targetTown = db.getPlayerTownId(targetUUID);
            if (targetTown == null || !targetTown.equals(townId)){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.playernotinyourtown")));
                });
                return;
            }
            if(db.isMemberRank(targetUUID, townId)){
                Bukkit.getScheduler().runTask(plugin, () ->{
                    requester.sendMessage(Utils.getColor(prefix + config.getString("messages.cantdemote")));
                });
                return;
            }
            boolean success = db.setRole(targetUUID, townId, "MEMBER");
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(success){
                    requester.sendMessage(Utils.getColor(prefix + "&aDemoted &f" + target.getName() + " &ato rank Member."));
                    target.sendMessage(Utils.getColor(prefix + "&aYou have been demoted to rank Member."));
                } else {
                    requester.sendMessage(Utils.getColor(prefix + "&cFailed to demote the player."));
                }
            });
        });
    }
}
