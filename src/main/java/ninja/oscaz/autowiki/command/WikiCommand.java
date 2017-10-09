package ninja.oscaz.autowiki.command;

import net.jodah.expiringmap.ExpiringMap;
import ninja.oscaz.autowiki.http.LookupDispatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WikiCommand implements CommandExecutor {

    private static final int SECONDS_BETWEEN_SEARCHES = 10; // Amount of seconds player must wait between Wiki lookups

    private final Map<UUID, Instant> timeout = this.generateTimeoutMap(); // Self expiring Map, storing Players' last lookup time

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) { // Incorrect usage of command
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <query>");
            return false;
        }

        if (sender instanceof Player) { // If not console, check for lookups in recent past
            Player player = (Player) sender;

            if (this.mustPlayerWait(player)) { // Player has performed lookup less than SECONDS_BETWEEN_SEARCHES (10) seconds ago
                return false;
            }

            this.startPlayerTimeout(player); // Register the player as performing lookup now
        }

        sender.sendMessage(ChatColor.GREEN + "Searching... Please wait.");
        this.dispatchQuery(sender, args); // Begin dispatch of Wiki lookup using args as query

        return false;
    }

    private boolean mustPlayerWait(Player player) {
        Instant timeUsed = this.timeout.get(player.getUniqueId());

        if (timeUsed != null) { // If latest time used still exists in expiring Map, Player searched Wiki less than SECONDS_BETWEEN_SEARCHES (10) seconds ago
            long cooldown = this.getCooldownRemainingSeconds(timeUsed);
            player.sendMessage(ChatColor.RED + "You must wait " + cooldown + " seconds before trying again.");
            return true;
        }

        return false;
    }

    private void startPlayerTimeout(Player player) {
        this.timeout.put(player.getUniqueId(), Instant.now()); // Store Player's timeout as beginning now
    }

    private void dispatchQuery(CommandSender sender, String[] args) {
        String query = StringUtils.join(args, ' '); // Join args with spaces to allow multi-word queries
        new LookupDispatcher(sender, query).search(); // Searches Wiki for query with new LookupDispatcher instance
    }

    private long getCooldownRemainingSeconds(Instant timeUsed) {
        long secondsSinceUse = Duration.between(timeUsed, Instant.now()).get(ChronoUnit.SECONDS); // Time between the last Wiki lookup and now

        return WikiCommand.SECONDS_BETWEEN_SEARCHES - secondsSinceUse;
    }

    private Map<UUID, Instant> generateTimeoutMap() {
        return ExpiringMap.builder()
                .expiration(WikiCommand.SECONDS_BETWEEN_SEARCHES, TimeUnit.SECONDS)
                .build(); // Map whose keys expire SECONDS_BETWEEN_SEARCHES (10) seconds after initial insertion
    }

}
