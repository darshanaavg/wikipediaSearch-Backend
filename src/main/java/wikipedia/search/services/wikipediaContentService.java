package wikipedia.search.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import wikipedia.search.daos.ElasticSearch;
import wikipedia.search.daos.MicrosoftSql;
import wikipedia.search.exceptions.InvalidPayloadException;
import wikipedia.search.models.WikipediaContent;
import wikipedia.search.models.WikipediaMetaData;

public class wikipediaContentService {

	public String getHttpResponseForUrl(String url) {

		HttpResponse<String> response = null;

		try {

			HttpClient httpClient = HttpClient.newHttpClient();

			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
					.headers("Content-Type", "application/json;charset=UTF-8").GET().build();

			response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

			return response.body().toString();

		} catch (IOException | InterruptedException e) {

			e.printStackTrace();
		}

		return null;

	}

	public List<Long> getPageIdsFromResponse(String response) throws ParseException {

		JSONParser jsonParser = new JSONParser();

		JSONObject jsonObject = (JSONObject) jsonParser.parse(response);

		JSONObject queryJson = (JSONObject) jsonObject.get("query");

		JSONArray search = (JSONArray) queryJson.get("search");

		Iterator<?> itr = search.iterator();

		List<Long> pageIds = new ArrayList();

		while (itr.hasNext()) {

			JSONObject searchObj = (JSONObject) itr.next();

			long pageId = (long) searchObj.get("pageid");

			pageIds.add(pageId);
		}

		return pageIds;

	}

	public String loadWikipediaContentsToES(String text) {

		try {

			if (text.trim().length() == 0 || text == null) {

				throw new InvalidPayloadException("Search title missing. Invalid Payload");
			}

			ElasticSearch es = new ElasticSearch();

			int totalProcessedCount = 0;

			WikipediaContent wiki = new WikipediaContent();

			updateTitleInSql(text);

			BulkProcessor bulkProcessor = es.initiateBulkProcessing(text);

			String titleToSearch = text.trim().replaceAll(" ", "_");

			String url = wiki.getWikipediaUrlForText(titleToSearch);

			String pageIdResponse = getHttpResponseForUrl(url);

			List<Long> pageIds = getPageIdsFromResponse(pageIdResponse);

			for (long pageId : pageIds) {

				String pageUrl = wiki.getWikipediaUrlForPageId(pageId);

				String contentResponse = getHttpResponseForUrl(pageUrl);

				JSONParser jsonParser = new JSONParser();
				JSONObject responseJson = (JSONObject) jsonParser.parse(contentResponse);

				JSONObject queryJson = (JSONObject) responseJson.get("query");

				JSONObject pages = (JSONObject) queryJson.get("pages");

				JSONObject pageIdJson = (JSONObject) pages.get(pageId + "");

				if (!pageIdJson.containsKey("extract")) {
					break;
				}

				String content = (String) pageIdJson.get("extract");

				String title = (String) pageIdJson.get("title");

				WikipediaContent wikiContent = new WikipediaContent();

				wikiContent.setPageId(pageId);
				wikiContent.setTitle(title);
				wikiContent.setKey(text);
				wikiContent.setContent(content);

				IndexRequest indexRequest = es.getIndexRequest(wikiContent);

				bulkProcessor.add(indexRequest);

				System.out.println("Fetching the contents of " + title + " from wikipedia ");

				totalProcessedCount++;

			}

			es.stopBulkProcessing(bulkProcessor);

			int result = updateSuccessInSql(text, totalProcessedCount);

			if (result == 0) {
				return "Error in updating the details in MS SQL";
			} else {
				return "Successfull";
			}

		} catch (SQLException | ParseException e) {

			e.printStackTrace();
		}
		return "Exception occurred";

	}

	public Map getWikipediaContentByKey(String text, int from, int size, String searchText) {

		if (text == null || text.length() == 0) {

			throw new InvalidPayloadException("Invalid Payload");

		}

		Map<String, Object> responseMap = new HashMap();

		ElasticSearch es = new ElasticSearch();

		WikipediaContent wiki = new WikipediaContent();

		SearchResponse response = null;

		if (searchText == null || searchText.length() == 0) {

			QueryBuilder query = QueryBuilders.termQuery("key", text);

			response = es.searchData(wiki.getIndexName(), query, from, size);

		} else {

			BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

			boolQueryBuilder.must(QueryBuilders.termQuery("key", text))
					.must(QueryBuilders.matchPhraseQuery("content", searchText));

			response = es.SearchDataByQueryWithHighlighter(wiki.getIndexName(), boolQueryBuilder, "content", from,
					size);

		}

		List<WikipediaContent> wikiContents = getContentsFromSearchResponse(response);

		Long hitCount = response.getHits().getTotalHits();

		System.out.println(hitCount);
		
		responseMap.put("response", wikiContents);
		
		responseMap.put("hitCount", hitCount);

		return responseMap;

	}

	public List<WikipediaContent> getContentsFromSearchResponse(SearchResponse response) {

		List<WikipediaContent> wikiData = new ArrayList<WikipediaContent>();

		try {

			JSONParser jsonParser = new JSONParser();
			JSONObject responseJson = (JSONObject) jsonParser.parse(response.toString());

			JSONObject hitsJson = (JSONObject) responseJson.get("hits");

			JSONArray hitsArray = (JSONArray) hitsJson.get("hits");

			Iterator<?> hitsItr = hitsArray.iterator();

			while (hitsItr.hasNext()) {

				WikipediaContent w = new WikipediaContent();

				JSONObject hitJson = (JSONObject) hitsItr.next();

				JSONObject sourceJson = (JSONObject) hitJson.get("_source");

				String pageId = (String) hitJson.get("_id");
				w.setPageId(Long.parseLong(pageId));

				String title = (String) sourceJson.get("title");
				w.setTitle(title);

				String key = (String) sourceJson.get("key");
				w.setKey(key);

				String content = (String) sourceJson.get("content");

				if (hitJson.containsKey("highlight")) {

					JSONObject highlightJson = (JSONObject) hitJson.get("highlight");

					ArrayList highlightArray = (ArrayList) highlightJson.get("content");

					String highlightContent = (String) highlightArray.get(0);

					content = highlightContent;

				}

				w.setContent(content);

				wikiData.add(w);
			}

		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		return wikiData;

	}

	public int updateTitleInSql(String title) throws SQLException {

		MicrosoftSql msSql = new MicrosoftSql();
		WikipediaMetaData w = new WikipediaMetaData();

		String dbName = w.getDbname();
		String tableName = w.getTablename();

		String selectQuery = "SELECT * FROM [" + dbName + "].[dbo].[" + tableName + "] where title='" + title + "'";

		ResultSet result = msSql.selectData(selectQuery);

		String query = null;

		if (!result.next()) {

			query = "INSERT INTO [" + dbName + "].[dbo].[" + tableName + "] VALUES ('" + title + "',0,0);";

		} else {

			query = "UPDATE [" + dbName + "].[dbo].[" + tableName + "] SET count=0,success=0 WHERE title='" + title
					+ "'";

		}
		return msSql.insertData(query);

	}

	public int updateSuccessInSql(String title, int count) throws SQLException {

		MicrosoftSql msSql = new MicrosoftSql();
		WikipediaMetaData w = new WikipediaMetaData();

		String dbName = w.getDbname();
		String tableName = w.getTablename();

		String query = "UPDATE [" + dbName + "].[dbo].[" + tableName + "] SET success=1,count=" + count
				+ "WHERE title='" + title + "'";

		return msSql.insertData(query);

	}

	public List<WikipediaMetaData> getWikipediaPagesCount() {

		List<WikipediaMetaData> wikiData = new ArrayList<WikipediaMetaData>();

		try {
			MicrosoftSql msSql = new MicrosoftSql();

			WikipediaMetaData w = new WikipediaMetaData();

			String dbName = w.getDbname();
			String tableName = w.getTablename();

			String query = "SELECT * FROM [" + dbName + "].[dbo].[" + tableName + "]";

			ResultSet result = msSql.selectData(query);

			while (result.next()) {

				WikipediaMetaData wiki = new WikipediaMetaData();
				wiki.setTitle(result.getString("title"));
				wiki.setCount(result.getInt("count"));
				wiki.setSuccess(result.getInt("success"));
				wikiData.add(wiki);

			}

			result.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return wikiData;

	}

	public WikipediaMetaData getSuccessStatus(String title) {

		WikipediaMetaData wikiData = new WikipediaMetaData();

		try {
			MicrosoftSql msSql = new MicrosoftSql();

			WikipediaMetaData w = new WikipediaMetaData();

			String dbName = w.getDbname();
			String tableName = w.getTablename();

			String query = "SELECT * FROM [" + dbName + "].[dbo].[" + tableName + "] where title='" + title + "'";

			ResultSet result = msSql.selectData(query);

			while (result.next()) {

				wikiData.setTitle(result.getString("title"));
				wikiData.setCount(result.getInt("count"));
				wikiData.setSuccess(result.getInt("success"));

			}

			result.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return wikiData;

	}
}
