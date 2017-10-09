package ninja.oscaz.autowiki.http;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

final class LookupCompletions {

    static void success(CommandSender user, LookupResult result) { // Successful lookup
        user.sendMessage("");
        user.sendMessage(ChatColor.GREEN + "=== SUCCESS ===");
        user.sendMessage(ChatColor.AQUA + result.getPageName() + ":");
        user.sendMessage(ChatColor.AQUA + "=> " + result.getDescription());
        user.sendMessage("");
        user.sendMessage(ChatColor.AQUA + "URL: " + result.getPageURL());
        user.sendMessage(ChatColor.GREEN + "===============");
        user.sendMessage("");
    }

    static void failedNoDescription(CommandSender user, LookupResult result) { // Found page on Wiki, but no description available
        user.sendMessage("");
        user.sendMessage(ChatColor.RED + "=== FAILURE ===");
        user.sendMessage(ChatColor.AQUA + result.getPageName() + ":");
        user.sendMessage(ChatColor.RED + "No description available!");
        user.sendMessage("");
        user.sendMessage(ChatColor.AQUA + "URL: " + result.getPageURL());
        user.sendMessage(ChatColor.RED + "===============");
        user.sendMessage("");
    }

    static void failedNoResult(CommandSender user, LookupResult result) { // No results found whatsoever
        user.sendMessage("");
        user.sendMessage(ChatColor.RED + "=== FAILURE ===");
        user.sendMessage(ChatColor.RED + "No results found!");
        user.sendMessage(ChatColor.RED + "===============");
        user.sendMessage("");
    }

    private LookupCompletions() { // Utility classes are statically bound, cannot be instantiated.
        throw new IllegalStateException("Cannot be instantiated.");
    }

}
