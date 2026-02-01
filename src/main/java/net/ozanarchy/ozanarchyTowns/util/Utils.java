package net.ozanarchy.ozanarchyTowns.util;

import org.bukkit.ChatColor;

import static net.ozanarchy.ozanarchyTowns.OzanarchyTowns.config;

public class Utils {
    public static String getColor(String message) {
        if (message == null)
            return "";

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String prefix(){
        return config.getString("prefix");
    }
}
