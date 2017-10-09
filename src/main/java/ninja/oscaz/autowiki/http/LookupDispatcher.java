package ninja.oscaz.autowiki.http;

import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LookupDispatcher {

    private static final Map<String, LookupResult> CACHE = new HashMap<>(); // Cache results for instant lookups of previously searched queries.

    private final CommandSender user; // User of the query
    private final String query;

    public LookupDispatcher(CommandSender user, String query) {
        this.user = user;
        this.query = query;
    }

    public void search() {
        String formattedQuery = this.formatQuery(this.query); // Lowercase trimmed version of query for use in cache

        Optional<LookupResult> cachedResult = this.findResultInCache(formattedQuery); // Attempt to find previously cached result

        if (cachedResult.isPresent()) {
            this.respond(cachedResult.get()); // Result was cached, instantly respond with value
        } else {
            this.dispatchNewLookupThread(formattedQuery); // Result not searched during runtime, dispatch new Thread to look up
        }
    }

    private Optional<LookupResult> findResultInCache(String formattedQuery) {
        return Optional.ofNullable(LookupDispatcher.CACHE.get(formattedQuery)); // Optional nullable result from cache Map
    }

    private void dispatchNewLookupThread(String formattedQuery) {
        Thread thread = new LookupThread(this.query, result -> { // Create new LookupThread with specified query, acting on future result
            this.cacheQuery(formattedQuery, result); // Cache the result with lowercase trimmed query
            this.respond(result); // Respond to user with found result
        });

        thread.start(); // Begin Wiki lookup in LookupThread
    }

    private void cacheQuery(String formattedQuery, LookupResult result) {
        LookupDispatcher.CACHE.put(formattedQuery, result); // Store lowercase trimmed query in cache
    }

    private String formatQuery(String query) {
        return query.toLowerCase().trim(); // Make query lowercase and trim beginning and end to ensure accurate Map equality comparisons
    }

    private void respond(LookupResult result) {
        SearchResult searchResult = result.getResult(); // Get the result of the lookup (Success, No description, Failure)
        searchResult.complete(this.user, result); // Use the result's completion consumer to send user result messages
    }

}
