package ninja.oscaz.autowiki.http;

import ninja.oscaz.autowiki.AutoWiki;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

final class LookupThread extends Thread {

    private static final String SEARCH_URL_FORMAT = "https://minecraft.gamepedia.com/index.php?search=%s"; // Format to initiate search on Wiki
    private static final String FIRST_RESULT_CSS_SELECTOR = "a[data-serp-pos=\"0\"]"; // CSS Selector to locate first search result element
    private static final String URL_ATTRIBUTE = "href"; // Attribute to find relative page URL from first search result element

    private static final String PAGE_URL_FORMAT = "https://minecraft.gamepedia.com%s"; // Format to convert relative page URL into fully declared URL
    private static final String PAGE_TITLE_CSS_SELECTOR = "#firstHeading"; // CSS Selector to find Wiki page's title
    private static final String FIRST_ELEMENT_CSS_SELECTOR = "body > div > div > div > div > div > p:first-of-type"; // CSS Selector to locate description of Wiki page

    private static final Pattern CITATION_MATCHER = Pattern.compile("\\[\\d+]"); // RegEx Pattern to remove citations like: [0], [4], etc.

    private final String query;
    private final Consumer<LookupResult> resultCallback;

    LookupThread(String query, Consumer<LookupResult> resultCallback) {
        this.query = query;
        this.resultCallback = resultCallback;
    }

    @Override
    public void run() {
        try {
            Document search = this.searchForQuery(); // Perform initial search of given query
            String searchUri = search.baseUri(); // Get redirected uri from search

            if (!this.foundDirectResult(searchUri)) { // Result did not link directly to Wiki page
                Elements searchResult = this.findFirstSearchResult(search); // Find first search result
                String relativeSearchResultUrl = this.getRelativeUrlFromSearchResult(searchResult); // Get URL from first search result (relative to current URI path)

                if (StringUtils.isBlank(relativeSearchResultUrl)) { // No search results exist
                    this.respondWithNoResult(); // Finish - respond with no result
                    return;
                }

                searchUri = this.convertRelativeUrlToFullUri(relativeSearchResultUrl); // Mutate searchUri to be first search result's page (converted to be non-relative)
                search = this.connectToUri(searchUri); // Mutate search document by reconnecting to first search result's Wiki page
            }

            String title = this.getTitleOfWiki(search); // Get title of Wiki page
            String description = this.getDescriptionOfWiki(search); // Get description of Wiki page

            if (StringUtils.isBlank(description)) { // No description in Wiki page
                this.respondWithoutDescription(title, searchUri); // Finish - respond with no description
                return;
            }

            this.respond(SearchResult.SUCCESS, title, description, searchUri); // Finish - respond with full success

        } catch (IOException exception) {
            throw new RuntimeException(exception); // Rethrow checked exception as unchecked RuntimeException
        }
    }

    private Document searchForQuery() throws IOException {
        return this.connectToUri(String.format(LookupThread.SEARCH_URL_FORMAT, this.query)); // Connect to SEARCH_URL_FORMAT with query formatted
    }

    private Document connectToUri(String uri) throws IOException {
        return Jsoup.connect(uri).get(); // Use GET to connect to given URI
    }

    private boolean foundDirectResult(String searchUri) {
        return !searchUri.contains("index.php?search="); // If uri contains "index.php?search=", result is search results rather than direct Wiki page
    }

    private Elements findFirstSearchResult(Document search) {
        return search.select(LookupThread.FIRST_RESULT_CSS_SELECTOR); // Use CSS Selector to locate first search result
    }

    private String getRelativeUrlFromSearchResult(Elements searchResult) {
        return searchResult.attr(LookupThread.URL_ATTRIBUTE); // Extract relative URL from href of search result element
    }

    private String convertRelativeUrlToFullUri(String relativeUrl) {
        return String.format(LookupThread.PAGE_URL_FORMAT, relativeUrl); // Use PAGE_URL_FORMAT to convert relativeUrl to fully declared URI
    }

    private String getTitleOfWiki(Document page) {
        Elements firstHeading = page.select(LookupThread.PAGE_TITLE_CSS_SELECTOR); // Find page title element using CSS Selector
        return firstHeading.text(); // Return text contained within element
    }

    private String getDescriptionOfWiki(Document page) {
        Elements firstDescription = page.select(LookupThread.FIRST_ELEMENT_CSS_SELECTOR); // Find first description element using CSS Selector
        return this.removeCitations(firstDescription.text()); // Return text contained within element without citations
    }

    private String removeCitations(String string) {
        return LookupThread.CITATION_MATCHER.matcher(string).replaceAll(""); // Replace all matched citations with empty string
    }

    private void respondWithNoResult() {
        this.respond(SearchResult.FAILED_NO_RESULT, this.query, "", ""); // Respond with failure due to no search results found
    }

    private void respondWithoutDescription(String title, String uri) {
        this.respond(SearchResult.FAILED_NO_DESCRIPTION, title,"", uri); // Respond with failure due to lack of description of Wiki page
    }

    private void respond(SearchResult searchResult, String pageName, String description, String pageURL) {
        LookupResult result = new LookupResult(searchResult, pageName, description, pageURL); // Create new LookupResult POJO with information about search

        Bukkit.getScheduler().runTask( // Execute on main Thread as calling Bukkit methods async can cause race conditions
                JavaPlugin.getPlugin(AutoWiki.class), // Attain main class instance with getPlugin helper method
                () -> this.resultCallback.accept(result) // Execute given callback on main Thread with created result POJO
        );
    }

}
