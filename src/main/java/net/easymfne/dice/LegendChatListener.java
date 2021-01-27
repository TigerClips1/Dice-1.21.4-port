package net.easymfne.dice;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class LegendChatListener implements Listener {

    private final Dice plugin;
    final static String magicFormat = ChatColor.MAGIC.toString() + ChatColor.COLOR_CHAR + ChatColor.COLOR_CHAR + ChatColor.COLOR_CHAR + "DICE";

    public LegendChatListener(Dice plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChatMessageEvent(ChatMessageEvent event) {
        if (event.getBukkitFormat().equals(magicFormat)) {
            String format = event.getFormat();
            int i = format.indexOf("{nick}");
            if(i != -1) {
                i += "{nick}".length();
                int j = format.indexOf(" ", i);
                if(j != -i) {
                    i = j + 1;
                }
                format = format.substring(0, i) + "{msg}";
            } else {
                format = "{color}{msg}";
            }
            event.setFormat(format);
        }
    }
}
