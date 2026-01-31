package net.ozanarchy.ozanarchyTowns.events;

import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import net.ozanarchy.ozanarchyTowns.Utils;
import net.ozanarchy.ozanarchyTowns.handlers.ChunkHandler;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class ProtectionListener implements Listener {
    private final OzanarchyTowns plugin;
    private final DatabaseHandler db;
    private final ChunkHandler chunkCache;

    public ProtectionListener(OzanarchyTowns plugin, DatabaseHandler db, ChunkHandler chunkCache) {
        this.plugin = plugin;
        this.db = db;
        this.chunkCache = chunkCache;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent e){
        protection(e.getPlayer(), e.getBlock().getChunk(), e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent e){
        protection(e.getPlayer(), e.getBlock().getChunk(), e);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractContainer(InventoryOpenEvent e){
        if (!(e.getPlayer() instanceof Player p)) return;
        InventoryHolder holder = e.getInventory().getHolder();
        Block block = null;

        if (holder instanceof Container c) {
            block = c.getBlock();
        } else if (holder instanceof DoubleChest dc) {
            block = dc.getLocation().getBlock();
        }
        if (block == null) return;

        Chunk chunk = block.getChunk();

        Integer townId = db.getChunkTownId(chunk);
        if (townId == null) return;

        if (p.hasPermission("oztowns.admin.protectionbypass")) return;

        boolean allowed = db.canBuild(p.getUniqueId(), chunk);

        if (allowed) return;


        e.setCancelled(true);
        p.sendMessage(Utils.getColor(config.getString("prefix") + config.getString("messages.missingcontainerperm")));
    }

    private void protection(Player p, Chunk chunk, Cancellable event){
        if (p.hasPermission("oztowns.admin.protectionbypass")) return;

        Integer claimTown = chunkCache.getTownId(chunk);
        if (claimTown == null) return;
        Integer playerTown = db.getPlayerTownId(p.getUniqueId());
        boolean allowed = playerTown != null &&
                            db.isTownAdmin(p.getUniqueId(), playerTown) &&
                            claimTown.equals(playerTown);
        if(!allowed) {
            event.setCancelled(true);
            p.sendMessage(Utils.getColor(config.getString("prefix") +
                    config.getString("messages.missinginteractperm")));
        }
    }
}
