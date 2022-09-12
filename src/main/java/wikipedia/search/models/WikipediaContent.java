package wikipedia.search.models;

public class WikipediaContent {

	private long pageId;
	private String key;
	private String title;
	private String content;

	private final String indexName = "wikipedia-*";
	private final String aliasName = "wikipedia";
	private final String docType = "text";

	private static final String wikipediaURL = "https://en.wikipedia.org/w/api.php?action=query&format=json";

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getPageId() {
		return pageId;
	}

	public void setPageId(long pageId) {
		this.pageId = pageId;
	}

	public String getWikipediaUrlForText(String titleToSearch) {
		return (wikipediaURL + "&list=search&utf8=1&srsearch=" + titleToSearch + "&srlimit=50").trim();
	}

	public String getWikipediaUrlForPageId(long pageId) {
		return (wikipediaURL + "&pageids=" + pageId + "&prop=extracts&explaintext").trim();
	}

	public String getIndexName() {
		return indexName;
	}

	public String getDocType() {
		return docType;
	}

	public String getAliasName() {
		return aliasName;
	}

}
