package net.ozanarchy.ozanarchyTowns.events;

import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import net.ozanarchy.ozanarchyTowns.handlers.ChunkHandler;
import net.ozanarchy.ozanarchyTowns.util.ChunkVisuals;
import net.ozanarchy.ozanarchyTowns.util.Utils;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class MemberEvents implements Listener {
    private final DatabaseHandler db;
    private final OzanarchyTowns plugin;
    private final EconomyAPI economy;
    private final ChunkHandler chunkCache;
    private final String prefix = Utils.prefix();
    private final String noPerm = config.getString("messages.nopermission");
    private final String incorrectUsage = config.getString("messages.usage");

    public MemberEvents(DatabaseHandler data, OzanarchyTowns plugin, EconomyAPI economy, ChunkHandler chunkCache){
        this.db = data;
        this.plugin = plugin;
        this.economy = economy;
        this.chunkCache = chunkCache;
    }

    //Removing or Adding players to the town
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

    //Player Town Ranks
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

    //Town Bank
    public void giveTownMoney(Player p, String[] args){
        if(args.length < 2){
            p.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }
        double amount = Double.parseDouble(args[1]);
        if(amount < 1){
            p.sendMessage(Utils.getColor(prefix + config.getString("messages.invalidamount")));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Integer townId = db.getPlayerTownId(p.getUniqueId());
            if(townId == null){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            economy.remove(p.getUniqueId(), amount, success ->{
                if(success){
                    db.depositTownMoney(townId, amount);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        p.sendMessage(Utils.getColor(prefix + "&aDeposited &e$" + amount + " &ato the towns bank."));
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        p.sendMessage(Utils.getColor(prefix + config.getString("messages.notenough")));
                    });
                    return;
                }
            });
        });
    }
    public void withdrawTownMoney(Player p, String[] args){
        if(args.length < 2){
            p.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }
        double amount = Double.parseDouble(args[1]);
        if(amount < 1){
            p.sendMessage(Utils.getColor(prefix + config.getString("messages.invalidamount")));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Integer townId = db.getPlayerTownId(p.getUniqueId());
            if(townId == null){
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            boolean isAdmin = db.isTownAdmin(p.getUniqueId(), townId);
            if(isAdmin){
                economy.add(p.getUniqueId(), amount);
                db.withdrawTownMoney(townId, amount);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + "&aYou have withdrawn &e$" + amount + " &afrom the towns bank."));
                });
            }
        });
    }
    public void townBalance(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Integer townId = db.getPlayerTownId(p.getUniqueId());
            if (townId == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    p.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                });
                return;
            }
            double bal = db.getTownBalance(townId);
            Bukkit.getScheduler().runTask(plugin, () ->{
               p.sendMessage(Utils.getColor(prefix + "&aTowns Bank Balance &e$" + bal + "&a."));
            });
        });
    }

    //Particle Visualizer (Thanks CHATGPT <3)
    public void chunkVisualizer(Player p){
        Integer playerTown = db.getPlayerTownId(p.getUniqueId());
        int duration = config.getInt("visualizer.duration");
        int timer = duration * 2;
        String ownParticle = config.getString("visualizer.own");
        String wildParticle = config.getString("visualizer.wild");
        String enemyParticle = config.getString("visualizer.enemy");

        Particle own, wild, enemy;
        try {
            own = Particle.valueOf(ownParticle.toUpperCase());
            wild = Particle.valueOf(wildParticle.toUpperCase());
            enemy = Particle.valueOf(enemyParticle.toUpperCase());
        } catch (IllegalArgumentException e) {
            p.sendMessage(Utils.getColor(prefix + "&cInvalid particle configured."));
            return;
        }

        p.sendMessage(Utils.getColor(prefix + "&aChunk Visualizer Enabled for &e" + duration + " &aseconds."));
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ >= timer || !p.isOnline()) {
                    cancel();
                    return;
                }

                Chunk center = p.getLocation().getChunk();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Chunk c = center.getWorld().getChunkAt(
                                center.getX() + dx,
                                center.getZ() + dz
                        );

                        Integer chunkTown = chunkCache.getTownId(c);

                        if (chunkTown == null) {
                            ChunkVisuals.showChunk(p, c, wild);
                        } else if (chunkTown.equals(playerTown)) {
                            ChunkVisuals.showChunk(p, c, own);
                        } else {
                            ChunkVisuals.showChunk(p, c, enemy);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}
