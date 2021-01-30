package net.easymfne.dice;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.types.Channel;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.ListenerPriority;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Emote;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DiscordChatListener {

    private final Dice plugin;
    Emote errorEmote = null;
    private final Random random = new Random();

    public DiscordChatListener(Dice plugin) {
        this.plugin = plugin;
    }

    @Subscribe(priority = ListenerPriority.LOW)
    public void onDiscordGuildMessagePreProcessEvent(DiscordGuildMessagePreProcessEvent event) {
        final String msg = event.getMessage().getContentRaw();
        if (msg.startsWith("/roll")) {
            event.setCancelled(true);
            int i = msg.indexOf(' ');
            String args[] = i == -1 ? new String[0] : msg.substring(i + 1).split(" ");

            int count = plugin.getPluginConfig().getDefaultCount();
            int sides = plugin.getPluginConfig().getDefaultSides();

            /* Check for arguments representing dice count */
            if (args.length > 0) {
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

            /* Check for arguments representing dice sides */
            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.matches("^.*d[0-9]+$")) {
                        int d = arg.indexOf('d');
                        sides = Integer.parseInt(arg.substring(d + 1));
                        break;
                    }
                }
            }

            /* Check the loaded or parsed values against the defined maximums. */
            if (count > plugin.getPluginConfig().getMaximumCount()
                    || sides > plugin.getPluginConfig().getMaximumSides()) {
                if (errorEmote == null) {
                    errorEmote = event.getGuild().getEmotesByName("spikewhoops", true).get(0);
                }
                event.getMessage().addReaction("❗").queue();
                event.getMessage().addReaction(errorEmote).queue();
                return;
            }

            Integer[] result = new Integer[count];
            for (i = 0; i < count; ++i) {
                result[i] = random.nextInt(sides) + 1;
            }

            String nick = event.getGuild().getMember(event.getAuthor()).getNickname();
            if(nick == null) {
                nick = event.getAuthor().getName();
            }
            String finalOut = plugin.rollCommand.formatString(nick, result, sides);

            // send out a custom event
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> Bukkit.getServer().getPluginManager().callEvent(new DiceRolled(finalOut, result)));

            // broadcast to linked channel
            String channelName = DiscordSRV.getPlugin().getDestinationGameChannelNameForTextChannel(event.getChannel());
            if (plugin.getPluginConfig().useLegendChat) {
                Channel chatChannel = getChannelByCaseInsensitiveName(channelName);
                if (chatChannel == null) {
                    return; // no suitable channel found
                }
                ChatColor color2 = ChatColor.WHITE;

                final String colorStr = chatChannel.getStringColor().toUpperCase();
                if (plugin.rollCommand.colors.contains(colorStr)) {
                    color2 = ChatColor.valueOf(colorStr);
                }

                chatChannel.sendMessage(finalOut.replace("{CHANNEL}", color2.toString()));
            } else {
                Bukkit.broadcastMessage(finalOut.replace("{CHANNEL}", ""));
            }
            event.getChannel().sendMessage(finalOut.replace("{CHANNEL}", "").replaceAll("\u00a7[0-9a-fA-F]", "")).queue();
        }
    }

    private static Channel getChannelByCaseInsensitiveName(String name) {
        for (Channel channel : Legendchat.getChannelManager().getChannels()) {
            if (channel.getName().equalsIgnoreCase(name)) {
                return channel;
            }
        }
        return null;
    }
}
