package ninja.oscaz.autowiki.http;

final class LookupResult {

    private final SearchResult result;
    private final String pageName;
    private final String description;
    private final String pageURL;

    // POJO to store results of Wiki lookup
    LookupResult(SearchResult result, String pageName, String description, String pageURL) {
        this.result = result;
        this.pageName = pageName;
        this.description = description;
        this.pageURL = pageURL;
    }

    SearchResult getResult() {
        return this.result;
    }

    String getPageName() {
        return this.pageName;
    }

    String getDescription() {
        return this.description;
    }

    String getPageURL() {
        return this.pageURL;
    }

}
