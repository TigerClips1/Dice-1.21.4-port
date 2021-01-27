/*
 * This file is part of the Dice plugin by EasyMFnE.
 * 
 * Dice is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 * 
 * Dice is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU General Public License for details.
 * 
 * You should have received a copy of the GNU General Public License v3 along
 * with Dice. If not, see <http://www.gnu.org/licenses/>.
 */
package net.easymfne.dice;

import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.PluginManager;

/**
 * Configuration helper class, with methods for accessing the configuration.
 *
 * @author Eric Hildebrand
 */
public class Config {

    private final Dice plugin;
    public String message_broadcast,
            message_broadcast_multi,
            message_private,
            natColors_normal,
            natColors_fail,
            natColors_critfail,
            natColors_crit,
            natColors_critcrit;
    public int broadcast_range,
            default_count,
            default_sides,
            maximum_count,
            maximum_sides;
    public boolean broadcast_crossworld,
            logging,
            broadcast_useChannel,
            natColors_enabled;
    
    public boolean useLegendChat;

    /**
     * Instantiate the class and give it a reference back to the plugin itself.
     *
     * @param plugin The Dice plugin
     */
    public Config(Dice plugin) {
        this.plugin = plugin;
    }

    /**
     * Load config values from file
     */
    void load() {
        plugin.reloadConfig();
        Configuration cfg = plugin.getConfig();
        message_broadcast = cfg.getString("messages.broadcast", "&c[&fDice&c] &f{PLAYER} rolled {RESULT} &7({COUNT}d{SIDES})");
        message_broadcast_multi = cfg.getString("messages.broadcastMulti", "&c[&fDice&c] &f{PLAYER} rolled {COUNT}d{SIDES} and got &a{TOTAL} &7({RESULT}&7)");
        message_private = cfg.getString("messages.private", "&4[&fDice&4] &fYou rolled {RESULT} &7({COUNT}d{SIDES})");
        broadcast_range = cfg.getInt("broadcast.range", -1);
        default_count = cfg.getInt("default.count", 1);
        default_sides = cfg.getInt("default.sides", 6);
        maximum_count = cfg.getInt("maximum.count", 6);
        maximum_sides = cfg.getInt("maximum.sides", 20);
        broadcast_crossworld = cfg.getBoolean("broadcast.crossworld", false);
        broadcast_useChannel = cfg.getBoolean("broadcast.useChannel", true);
        logging = cfg.getBoolean("logging", false);
        natColors_enabled = cfg.getBoolean("natColors.enable", true);
        natColors_normal = ChatColor.translateAlternateColorCodes('&', cfg.getString("natColors.normal", "&e"));
        natColors_critfail = ChatColor.translateAlternateColorCodes('&', cfg.getString("natColors.fail", "&4"));
        natColors_fail = ChatColor.translateAlternateColorCodes('&', cfg.getString("natColors.fail", "&c"));
        natColors_crit = ChatColor.translateAlternateColorCodes('&', cfg.getString("natColors.crit", "&a"));
        natColors_critcrit = ChatColor.translateAlternateColorCodes('&', cfg.getString("natColors.crit", "&2"));

        // plugin check
        PluginManager pm = plugin.getServer().getPluginManager();
        if(pm.getPlugin("LegendChat") != null) {
            useLegendChat = true;
        } else {
            broadcast_useChannel = false;
        }
    }

    /**
     * Get the desired broadcast message template. The default case looks like:
     * [Dice] EasyMFnE rolled 2, 3, 6, 1, 1 (5d6)
     *
     * @return The broadcast message template
     */
    public String getBroadcastMessage() {
        return message_broadcast;
    }

    /**
     * @return The allowable broadcast range, defaulting to -1
     */
    public int getBroadcastRange() {
        return broadcast_range;
    }

    /**
     * @return The default number of dice to roll, defaulting to 1.
     */
    public int getDefaultCount() {
        return default_count;
    }

    /**
     * @return The default number of sides on the dice, defaulting to 6.
     */
    public int getDefaultSides() {
        return default_sides;
    }

    /**
     * @return The maximum number of dice that can be rolled at once.
     */
    public int getMaximumCount() {
        return maximum_count;
    }

    /**
     * @return The maximum number of sides on a die.
     */
    public int getMaximumSides() {
        return maximum_sides;
    }

    /**
     * Get the desired private message template. The default case looks like:
     * [Dice] You rolled 2, 3, 6, 1, 1 (5d6)
     *
     * @return The private message template
     *
     */
    public String getPrivateMessage() {
        return message_private;
    }

    /**
     * @return Do dice broadcasts travel between worlds?
     */
    public boolean isCrossworld() {
        return broadcast_crossworld;
    }

    /**
     * @return Are we logging all dice rolls?
     */
    public boolean isLogging() {
        return logging;
    }

}
