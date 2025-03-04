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

import github.scarsz.discordsrv.DiscordSRV;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is the main class of the Dice plugin, responsible for its own setup,
 * logging, reloading, and shutdown. Maintains instances of Config and
 * RollCommand.
 *
 * @author Eric Hildebrand
 * @version 1.0
 */
public class Dice extends JavaPlugin {

    private Config config = new Config(this);
    protected RollCommand rollCommand = null;
    private DiscordChatListener discordChat = null;

    /*
     * Strings used in the fancyLog() methods.
     */
    private final String logPrefix = ChatColor.RED + "[Dice] ";
    private final String logColor = ChatColor.WHITE.toString();

    /**
     * Log a message to the console using color, with a specific logging Level.
     * If there is no console open, log the message without any coloration.
     *
     * @param level Level at which the message should be logged
     * @param message The message to be logged
     */
    protected void fancyLog(Level level, String message) {
        if (getServer().getConsoleSender() != null) {
            getServer().getConsoleSender().sendMessage(
                    logPrefix + logColor + message);
        } else {
            getServer().getLogger().log(level,
                    ChatColor.stripColor(logPrefix + message));
        }
    }

    /**
     * Log a message to the console using color, defaulting to the Info level.
     * If there is no console open, log the message without any coloration.
     *
     * @param message The message to be logged
     */
    protected void fancyLog(String message) {
        fancyLog(Level.INFO, message);
    }

    protected Config getPluginConfig() {
        return config;
    }

    /**
     * Unregister and null the command handler, then null the configuration
     * instance, before shutting down and displaying the milliseconds it took.
     */
    @Override
    public void onDisable() {
        long start = System.currentTimeMillis();
        fancyLog("=== DISABLE START ===");
        rollCommand.close();
        rollCommand = null;
        config = null;
        if(discordChat != null) {
            DiscordSRV.api.unsubscribe(discordChat);
            discordChat = null;
        }
        fancyLog("=== DISABLE COMPLETE ("
                + (System.currentTimeMillis() - start)
                + "ms) ===");
    }

    /**
     * Load configuration from file, creating it from default if needed.
     * Instantiate the configuration helper and register and command handler,
     * displaying the number of milliseconds it took.
     */
    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        fancyLog("=== ENABLE START ===");
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
            fancyLog("Saved default config.yml");
        }

        config.load();
        if (config.broadcast_useChannel) {
            getServer().getPluginManager().registerEvents(new LegendChatListener(this), this);
        }
        if (getServer().getPluginManager().getPlugin("DiscordSRV") != null) {
//            try {
//                Field f = github.scarsz.discordsrv.api.ApiManager.class.getDeclaredField("apiListeners");
//                f.setAccessible(true);
//                for (Object o : ((List) f.get(DiscordSRV.api)).toArray()) {
//                    if (o.getClass().getSimpleName().equalsIgnoreCase("DiscordChatListener")) {
//                        DiscordSRV.api.unsubscribe(o);
//                    }
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(Dice.class.getName()).log(Level.SEVERE, null, ex);
//            }
            DiscordSRV.api.subscribe(discordChat = new DiscordChatListener(this));
        }
        rollCommand = new RollCommand(this);
        fancyLog("=== ENABLE COMPLETE ("
                + (System.currentTimeMillis() - start)
                + "ms) ===");
    }

    /**
     * Reload the plugin's configuration from disk and show how long it took.
     */
    public void reload() {
        long start = System.currentTimeMillis();
        fancyLog("=== RELOAD START ===");
        config.load();
        fancyLog("=== RELOAD COMPLETE ("
                + (System.currentTimeMillis() - start)
                + "ms) ===");
    }

}