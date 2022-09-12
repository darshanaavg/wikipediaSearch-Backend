package wikipedia.search.models;


public class WikipediaMetaData {

	private String title;
	private int count;
	private int success;

	private static final String dbName = "wikipediaCollection";
	private static final String tableName = "wikipediaData";


	public String getTitle() {
		return title;
	}
	
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}


	public String getDbname() {
		return dbName;
	}


	public String getTablename() {
		return tableName;
	}


	public int getSuccess() {
		return success;
	}


	public void setSuccess(int success) {
		this.success = success;
	}

}
