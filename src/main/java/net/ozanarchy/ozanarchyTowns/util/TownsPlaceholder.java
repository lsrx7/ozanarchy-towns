package net.ozanarchy.ozanarchyTowns.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.ozanarchy.ozanarchyTowns.OzanarchyTowns;
import net.ozanarchy.ozanarchyTowns.handlers.DatabaseHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TownsPlaceholder extends PlaceholderExpansion {

    private final OzanarchyTowns plugin;
    private final DatabaseHandler db;

    public TownsPlaceholder(OzanarchyTowns plugin, DatabaseHandler db) {
        this.plugin = plugin;
        this.db = db;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oztowns";
    }

    @Override
    public @NotNull String getAuthor() {
        return "OzAnarchy";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) return null;

        // Only handle %oztowns_town%
        if (params.equalsIgnoreCase("town")) {
            java.util.UUID uuid = offlinePlayer.getUniqueId();
            Integer townId = db.getPlayerTownId(uuid);
            if (townId == null) return null;
            return db.getTownName(townId);
        }
        return null;
    }
}
