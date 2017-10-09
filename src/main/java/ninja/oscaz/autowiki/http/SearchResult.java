package ninja.oscaz.autowiki.http;

import org.bukkit.command.CommandSender;

import java.util.function.BiConsumer;

public enum SearchResult {

    SUCCESS(LookupCompletions::success), // Uses LookupCompletions#success
    FAILED_NO_DESCRIPTION(LookupCompletions::failedNoDescription), // Uses LookupCompletions#failedNoDescription
    FAILED_NO_RESULT(LookupCompletions::failedNoResult); // Uses LookupCompletions#failedNoResult

    // Consumer of user and result to send user messages of the result
    private final BiConsumer<CommandSender, LookupResult> completer;

    SearchResult(BiConsumer<CommandSender, LookupResult> completer) {
        this.completer = completer;
    }

    // Pass user and result to enum value's completer
    public void complete(CommandSender sender, LookupResult result) {
        this.completer.accept(sender, result);
    }

}
