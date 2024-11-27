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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * The class that handles all console and player commands for the plugin.
 *
 * @author Eric Hildebrand
 */
public class RollCommand implements CommandExecutor, TabCompleter {

    private final Dice plugin;
    private final Random random = new Random();
    protected final Set<String> colors = new HashSet(Arrays.asList(
            "BLACK", "DARK_BLUE", "DARK_GREEN", "DARK_AQUA", "DARK_RED",
            "DARK_PURPLE", "GOLD", "GRAY", "DARK_GRAY", "BLUE", "GREEN",
            "AQUA", "RED", "LIGHT_PURPLE", "YELLOW", "WHITE", "MAGIC",
            "BOLD", "STRIKETHROUGH", "UNDERLINE", "ITALIC", "RESET")
    );
    private final HashMap<Player, Long> lastCall = new HashMap<>();
	private final static Pattern diceParts = Pattern.compile("^(([0-9]+)?d)?([0-9]+)((\\+|\\-)([0-9]+))?$");
	
    /**
     * Instantiate by getting a reference to the plugin instance, creating a new
     * Random, and registering this class to handle the '/roll' command.
     *
     * @param plugin Reference to Dice plugin instance
     */
    public RollCommand(Dice plugin) {
        this.plugin = plugin;
        plugin.getCommand("roll").setExecutor(this);
        plugin.getCommand("roll").setTabCompleter(this);
    }

    /**
     * Broadcast the results of a dice roll to the players of the server.
     * Configuration can be set so that messages are only set within the world
     * that the player resides, and also within a certain distance of them. Dice
     * rolled by non-players (e.g. the Console) are sent to all players.
     *
     * @param sender The user rolling the dice
     * @param message The fully-formatted message to display
     */
    private void broadcast(CommandSender sender, String message) {
        if (message == null) {
            return;
        }
        Player p1 = (sender instanceof Player ? (Player) sender : null);

        if (plugin.getPluginConfig().isLogging()) {
            plugin.getLogger().info(message);
        }

        if (p1 != null && plugin.getPluginConfig().broadcast_useChannel) {
            if (plugin.getPluginConfig().useLegendChat) {
                br.com.devpaulo.legendchat.channels.types.Channel ch
                        = br.com.devpaulo.legendchat.api.Legendchat.getPlayerManager().getPlayerFocusedChannel(p1);
                ChatColor color2 = ChatColor.WHITE;

                final String colorStr = ch.getStringColor().toUpperCase();
                if (colors.contains(colorStr)) {
                    color2 = ChatColor.valueOf(colorStr);
                }

                ch.sendMessage(p1, message.replace("{CHANNEL}", color2.toString()), LegendChatListener.magicFormat, false);
            }
            // TODO? add more plugins?
        } else {
            message = message.replace("{CHANNEL}", "");
            double dSquared = square(plugin.getPluginConfig().getBroadcastRange());
            for (Player p2 : plugin.getServer().getOnlinePlayers()) {
                if (plugin.getPluginConfig().isCrossworld()
                        || p1 == null
                        || p1.getWorld() == p2.getWorld()) {
                    if (plugin.getPluginConfig().getBroadcastRange() < 0
                            || p1 == null
                            || getDSquared(p1, p2) < dSquared) {
                        p2.sendMessage(message);
                    }
                }
            }
        }
    }

    /**
     * Release the '/roll' command from its ties to this class.
     */
    public void close() {
        plugin.getCommand("roll").setExecutor(null);
        plugin.getCommand("roll").setTabCompleter(null);
    }

    /**
     * Format and return a String that will be used to display the roll results.
     * This method replaces tags: {PLAYER}, {RESULT}, {COUNT}, {SIDES}, {TOTAL}.
     * This method also replaces '&' style color codes with proper ChatColors.
     *
     * @param sender The user that rolled the dice
     * @param roll The results of the roll, as an array
     * @param sides The number of sides on the dice
	 * @param mod Modifier for the end result
     * @return The fancy-formatted message
     */
    private String formatString(CommandSender sender, Integer[] roll, int sides, int mod) {
        String result;
        if (Perms.broadcast(sender)) {
            if (roll.length > 1) {
                result = plugin.getPluginConfig().message_broadcast_multi;
            } else {
                result = plugin.getPluginConfig().getBroadcastMessage();
            }
        } else {
            result = plugin.getPluginConfig().getPrivateMessage();
        }
        if (result == null || result.length() == 0) {
            return null;
        }
        result = result
                .replace("{PLAYER}", sender.getName())
                .replace("{NICKNAME}", formatName(sender))
                .replace("{RESULT}", plugin.getPluginConfig().natColors_enabled ? formatResults(roll, sides, mod) : String.valueOf(roll[0] + mod))
                .replace("{COUNT}", String.valueOf(roll.length))
                .replace("{SIDES}", String.valueOf(sides))
                .replace("{MOD}", mod == 0 ? "" : (mod > 0 ? "+" : "") + String.valueOf(mod))
                .replace("{TOTAL}", plugin.getPluginConfig().natColors_enabled ? formatResultTotal(roll, sides, mod) : String.valueOf(sum(roll) + mod));
        return ChatColor.translateAlternateColorCodes('&', result);
    }
    
    protected String formatString(String sender, Integer[] roll, int sides, int mod) {
        String result;
        if (roll.length > 1) {
            result = plugin.getPluginConfig().message_broadcast_multi;
        } else {
            result = plugin.getPluginConfig().getBroadcastMessage();
        }
        if (result == null || result.length() == 0) {
            return null;
        }
        result = result
                .replace("{PLAYER}", sender)
                .replace("{NICKNAME}", sender)
                .replace("{RESULT}", plugin.getPluginConfig().natColors_enabled ? formatResults(roll, sides, mod) : String.valueOf(roll[0] + mod))
                .replace("{COUNT}", String.valueOf(roll.length))
                .replace("{SIDES}", String.valueOf(sides))
                .replace("{MOD}", mod == 0 ? "" : (mod > 0 ? "+" : "") + String.valueOf(mod))
                .replace("{TOTAL}", plugin.getPluginConfig().natColors_enabled ? formatResultTotal(roll, sides, mod) : String.valueOf(sum(roll) + mod));
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private String formatResultTotal(Integer[] roll, int max, int mod) {
        int median = (int) Math.floor(max / 3.) * roll.length;
        max *= roll.length;
        int total = sum(roll) + mod;

        if (total <= roll.length) {
            return plugin.getPluginConfig().natColors_critfail + total;
        } else if (total >= max) {
            return plugin.getPluginConfig().natColors_critcrit + total;
        } else if (total < median) {
            return plugin.getPluginConfig().natColors_fail + total;
        } else if (total > max - median) {
            return plugin.getPluginConfig().natColors_crit + total;
        }
        return plugin.getPluginConfig().natColors_normal + total;
    }

    private String formatResults(Integer[] roll, int max, int mod) {
        int median = (int) Math.floor(max / 3.);
		roll[0] += mod;
        if (roll.length == 1) {
            if (roll[0] == 1) {
                return plugin.getPluginConfig().natColors_critfail + roll[0];
            } else if (roll[0] == max) {
                return plugin.getPluginConfig().natColors_critcrit + roll[0];
            } else if (roll[0] < median) {
                return plugin.getPluginConfig().natColors_fail + roll[0];
            } else if (roll[0] > max - median) {
                return plugin.getPluginConfig().natColors_crit + roll[0];
            }
            return plugin.getPluginConfig().natColors_normal + roll[0];
        }
        StringBuilder sb = new StringBuilder();
        for (int i = roll.length - 1; i >= 0; --i) {
            if (roll[i] == 1) {
                sb.append(plugin.getPluginConfig().natColors_critfail).append(roll[i]);
            } else if (roll[i] == max) {
                sb.append(plugin.getPluginConfig().natColors_critcrit).append(roll[i]);
            } else if (roll[i] < median) {
                sb.append(plugin.getPluginConfig().natColors_fail).append(roll[i]);
            } else if (roll[i] > max - median) {
                sb.append(plugin.getPluginConfig().natColors_crit).append(roll[i]);
            } else {
                sb.append(plugin.getPluginConfig().natColors_normal).append(roll[i]);
            }
            if (i != 0) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private String formatName(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getDisplayName().replaceAll("[^A-Za-z0-9_]", "");
        }
        return sender.getName();
    }

    /**
     * Get the squared distance between two players.
     *
     * @param p1 Player one
     * @param p2 Player two
     * @return The distance^2
     */
    private int getDSquared(Player p1, Player p2) {
        int dx = p1.getLocation().getBlockX() - p2.getLocation().getBlockX();
        int dy = p1.getLocation().getBlockY() - p2.getLocation().getBlockY();
        int dz = p1.getLocation().getBlockZ() - p2.getLocation().getBlockZ();
        return dx * dx + dy * dy + dz * dz;
    }

    /**
     * Show the results of a roll to a player privately.
     *
     * @param sender The user rolling the dice
     * @param message The fully-formatted message to display
     */
    private void message(CommandSender sender, String message) {
        if (message == null) {
            return;
        }
        sender.sendMessage(message);
    }

    /**
     * This method handles user commands. Usage: "/roll <help,reload>" which
     * either shows help or reloads config. Usage: "/roll [count] [d<sides>]"
     * where the order of the arguments does not matter, but the number of sides
     * must be prefixed with 'd'.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")
                    || args[0].equalsIgnoreCase("?")) {
                showHelp(sender);
                return true;
            }
            if (Perms.canReload(sender) && args[0].equalsIgnoreCase("reload")) {
                plugin.reload();
                sender.sendMessage("Configuration reloaded");
                return true;
            }
        }

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            long now = System.currentTimeMillis();
            Long last = lastCall.get(p);
            if (last != null && now - last < 5600) {
                p.playSound(p.getEyeLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1, 1.2F);
                p.sendMessage(ChatColor.RED + "Wait at least five seconds between dice rolls!");
                lastCall.put(p, last + 1500); // impatient bugger, aren't you? :P
                return true;
            }
            lastCall.put(p, now);
        }

        int count = plugin.getPluginConfig().getDefaultCount();
        int sides = plugin.getPluginConfig().getDefaultSides();
		int mod = 0;
		
		if (args.length > 0) {
			// parse command:
			Matcher m = diceParts.matcher(args[0]);
			if(m.find()) {
				if (m.group(1) == null) {
					// did not find a 'd' in the argument, so group 3 is the count
					count = Integer.parseInt(m.group(3));
				} else {
					sides = Integer.parseInt(m.group(3));
					if (m.group(2) != null) {
						count = Integer.parseInt(m.group(2));
					}
				}
				if (m.group(6) != null) {
					mod = Integer.parseInt(m.group(6));
					if (m.group(5).charAt(0) == '-') {
						mod *= -1;
					}
				}
			}
		}
	
        /* Check for arguments representing dice count * /
        if (args.length > 0 && Perms.canRollMultiple(sender)) {
            for (String arg : args) {
                if (arg.matches("^[0-9]+$")) {
                    count = Integer.parseInt(arg);
                    break;
                } else if (arg.matches("^[0-9]+d[0-9]+")) {
                    int d = arg.indexOf('d');
                    count = Integer.parseInt(arg.substring(0, d));
                    break;
                }
            }
        }

        /* Check for arguments representing dice sides * /
        if (args.length > 0 && Perms.canRollAnyDice(sender)) {
            for (String arg : args) {
                if (arg.matches("^.*d[0-9]+$")) {
                    int d = arg.indexOf('d');
                    sides = Integer.parseInt(arg.substring(d + 1));
                    break;
                }
            }
        }*/

        /* Check the loaded or parsed values against the defined maximums. */
        if (count > plugin.getPluginConfig().getMaximumCount()) {
            sender.sendMessage(ChatColor.RED
                    + "You can't roll that many dice at once!");
            return false;
        }
        if (sides > plugin.getPluginConfig().getMaximumSides()) {
            sender.sendMessage(ChatColor.RED
                    + "You can't roll dice with that many sides!");
            return false;
        }

        /* Roll the dice and handle the outcome */
        roll(sender, Math.max(1, count), Math.max(2, sides), mod);
        return true;
    }

    /**
     * Roll a set of dice for a user, and either broadcast the results publicly
     * or send them privately, depending on the user's permissions.
     *
     * @param sender The user rolling the dice
     * @param count The number of dice to roll
     * @param sides The number of sides per die
     * @param mod Modifier for the end result
     */
    private void roll(CommandSender sender, int count, int sides, int mod) {
        Integer[] result = new Integer[count];
        for (int i = 0; i < count; ++i) {
            result[i] = random.nextInt(sides) + 1;
        }

        String finalOut = formatString(sender, result, sides, mod);

        // send out a custom event
        DiceRolled event = new DiceRolled(finalOut, result);
        // Call the event
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (Perms.broadcast(sender)) {
            broadcast(sender, finalOut);
        } else {
            message(sender, finalOut.replace("{CHANNEL}", ""));
        }
    }

    /**
     * Show personalized usage help to the user, taking into account his or her
     * permissions.
     *
     * @param sender The user to help
     */
    private void showHelp(CommandSender sender) {
        /* Treat the pair of booleans as 2^0 and 2^1 bits */
        int perms = (Perms.canRollMultiple(sender) ? 1 : 0)
                + (Perms.canRollAnyDice(sender) ? 2 : 0);
        switch (perms) {
            case 1:
                sender.sendMessage(ChatColor.RED + "Usage: /roll [count]");
                return;
            case 2:
                sender.sendMessage(ChatColor.RED + "Usage: /roll [d<sides>]");
                return;
            case 3:
                sender.sendMessage(ChatColor.RED + "Usage: /roll [count] [d<sides>]");
                return;
            default:
                sender.sendMessage(ChatColor.RED + "Usage: /roll");
        }
    }

    /**
     * Square an input. Useful for decluttering the code.
     *
     * @param input The number to be squared
     * @return The result
     */
    private int square(int input) {
        return input * input;
    }

    /**
     * Calculate the sum of an array of numbers.
     *
     * @param roll The array of numbers
     * @return The sum
     */
    private int sum(Integer[] roll) {
        int t = 0;
        for (int i : roll) {
            t += i;
        }
        return t;
    }

    @Override
    public List<String> onTabComplete(CommandSender cs, Command cmnd, String string, String[] args) {
        List<String> res = null;

        if (args.length == 1) {
            res = new ArrayList();
            res.add("1d6");
            res.add("help");
            if (Perms.canReload(cs)) {
                res.add("reload");
            }
        } else if (args.length == 2 && args[0].matches("d[0-9]*")) {
            res.add("[count]");
        }

        if (res != null && !res.isEmpty()) {
            res = res.stream()
                    .filter(e -> e != null && e.startsWith(args[args.length - 1]))
                    .collect(Collectors.toList());
        }
        return res;
    }

}
