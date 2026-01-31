package net.ozanarchy.ozanarchyTowns.events;

import net.ozanarchy.ozanarchyEconomy.api.EconomyAPI;
import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import net.ozanarchy.ozanarchyTowns.Utils;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class TownEvents implements Listener {
    private final DatabaseHandler db;
    private final OzanarchyTowns plugin;
    private final EconomyAPI economy;
    private final String prefix = Utils.prefix();
    private final String notEnough = config.getString("messages.notenough");
    private final String noPerm = config.getString("messages.nopermission");
    private final String incorrectUsage = config.getString("messages.incorrectusage");

    public TownEvents(DatabaseHandler data, OzanarchyTowns plugin, EconomyAPI economy){
        this.db = data;
        this.plugin = plugin;
        this.economy = economy;
    }

    public void claimLand(Player p){
        UUID uuid = p.getUniqueId();
        double cost = config.getInt("claims.cost");

        economy.remove(uuid, cost, success -> {
            if(!success){
                p.sendMessage(Utils.getColor(prefix + notEnough));
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Chunk chunk = p.getLocation().getChunk();
                if(db.getChunkClaimed(chunk)){
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        p.sendMessage(Utils.getColor(prefix + config.getString("messages.chunkowned")));
                    });
                    economy.add(uuid, cost);
                    return;
                }

                db.saveClaim(chunk, db.getPlayerTownId(uuid));
                Bukkit.getScheduler().runTask(plugin, () -> {
                   p.sendMessage(Utils.getColor(prefix + config.getString("messages.chunkclaimed")));
                });
            });
        });
    }

    public void createTown(Player p, String[] args) {
        String townName = args[1];
        if (!townName.matches("^[A-Za-z0-9_]{3,16}$")) {
            p.sendMessage(Utils.getColor(prefix + "Town name must be 3–16 characters (letters, numbers, _)"));
            return;
        }
        if(args.length != 2){
            p.sendMessage(Utils.getColor(prefix + incorrectUsage));
            return;
        }
        if(!p.hasPermission("oztowns.create")){
            p.sendMessage(Utils.getColor(prefix + noPerm));
        }


        UUID uuid = p.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
           if(db.getPlayerTownId(uuid) != null){
               Bukkit.getScheduler().runTask(plugin, () ->{
                  p.sendMessage(Utils.getColor(prefix + config.getString("messages.alreadyinatown")));
               });
               return;
           }

           if(db.townExists(townName)){
               Bukkit.getScheduler().runTask(plugin, () -> {
                  p.sendMessage(Utils.getColor(prefix + config.getString("messages.townnametaken")));
               });
               return;
           }

            economy.remove(uuid, config.getDouble("towns.cost"), success -> {
                if (!success) {
                    p.sendMessage(Utils.getColor(prefix + notEnough));
                }
            });

           try {
               int townId = db.createTown(townName, uuid);
               db.addMember(townId, uuid, "MAYOR");
               Bukkit.getScheduler().runTask(plugin, () -> {
                   p.sendMessage(Utils.getColor(prefix + "&aTown &f" + townName + " &acreated successfully."));
               });
           } catch (SQLException e){
               e.printStackTrace();
               Bukkit.getScheduler().runTask(plugin, () -> {
                  p.sendMessage(Utils.getColor(prefix + config.getString("messages.failedtomaketown")));
               });
           }
        });

    }

    public void abandonTown(UUID uuid){
        Player p = Bukkit.getPlayer(uuid);
        Integer townId = db.getPlayerTownId(uuid);
        if(townId == null){
            Bukkit.getScheduler().runTask(plugin, () ->{
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.notownexists")));
            });
            return;
        }
        if(!db.isMayor(uuid, townId)){
            p.sendMessage(Utils.getColor(prefix + config.getString("messages.notmayor")));
            return;
        }

        db.deleteClaim(townId);
        db.deleteMembers(townId);
        db.deleteTown(townId);
        p.sendMessage(Utils.getColor(prefix + config.getString("messages.towndeleted")));
        return;
    }

    public void removeChunk(Player p){
        Chunk chunk = p.getLocation().getChunk();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            UUID uuid = p.getUniqueId();
            Integer townId = db.getPlayerTownId(uuid);
            if (townId == null){
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.notown")));
                return;
            }
            if (!db.isTownAdmin(uuid, townId)){
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.nottownadmin")));
                return;
            }
            Integer claimTown = db.getChunkTownId(chunk);
            if(claimTown == null){
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.chunknotowned")));
                return;
            }
            if(claimTown != townId){
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.chunkowned")));
                return;
            }

            boolean success = db.unClaimChunk(chunk, townId);
            if(success){
                p.sendMessage(Utils.getColor(prefix + config.getString("messages.chunkremoved")));
            } else {
                p.sendMessage(Utils.getColor(prefix + "&cFailed to unclaim the chunk."));
            }
        });
    }
}
