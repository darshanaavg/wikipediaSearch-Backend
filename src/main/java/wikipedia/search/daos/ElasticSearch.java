package wikipedia.search.daos;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.threadpool.ThreadPool;

import wikipedia.search.models.WikipediaContent;
import wikipedia.search.models.WikipediaMetaData;

public class ElasticSearch {

	private static RestHighLevelClient client;

	private static final String host = "localhost";
	private static final int portNumber = 9200;
	private static final String scheme = "http";

	public RestHighLevelClient enableESConnection() {

		if (client == null) {
			client = new RestHighLevelClient(RestClient.builder(new HttpHost(host, portNumber, scheme)));
		}

		System.out.println("Connection to ES established");

		return client;
	}

	public void closeConnection() throws IOException {

		client.close();
		client = null;
		System.out.println("Connection disabled");

	}

	public BulkProcessor.Listener getBulkListener(String title) {

		BulkProcessor.Listener listener = new BulkProcessor.Listener() {
			int count = 0;

			@Override
			public void beforeBulk(long l, BulkRequest bulkRequest) {
				try {
					
					count = count + bulkRequest.numberOfActions();

					MicrosoftSql msSql = new MicrosoftSql();
					ElasticsearchCurator curator = new ElasticsearchCurator();

					curator.performRollover();
					msSql.updateTitleAndCount(title, count);

				} catch (SQLException | IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void afterBulk(long l, BulkRequest bulkRequest, BulkResponse bulkResponse) {
				if (bulkResponse.hasFailures()) {
					for (BulkItemResponse bulkItemResponse : bulkResponse) {
						if (bulkItemResponse.isFailed()) {
							System.out.println(bulkItemResponse.getOpType());
							BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
							System.out.println("Error " + failure.toString());
						}
					}
				}
			}

			@Override
			public void afterBulk(long l, BulkRequest bulkRequest, Throwable throwable) {
				System.out.println("Big errors " + throwable.toString());
			}
		};

		return listener;

	}

	public ThreadPool getThreadPool() {

		return new ThreadPool(Settings.builder().put().build());

	}

	public IndexRequest getIndexRequest(WikipediaContent content) {

		Map<String, Object> dataMap = new HashMap<String, Object>();

		dataMap.put("key", content.getKey());
		dataMap.put("title", content.getTitle());
		dataMap.put("content", content.getContent());

		return new IndexRequest(content.getAliasName(), content.getDocType(), content.getPageId() + "").source(dataMap);

	}

	public BulkProcessor initiateBulkProcessing(String text) {

		return new BulkProcessor.Builder(client::bulkAsync, getBulkListener(text), getThreadPool()).setBulkActions(1000)
				.setBulkSize(new ByteSizeValue(50, ByteSizeUnit.KB)).setConcurrentRequests(0)
				.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueSeconds(1L), 3)).build();
	}

	public boolean stopBulkProcessing(BulkProcessor bulkProcessor) {

		try {
			boolean terminated = bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
			if (!terminated) {
				return false;
			} else {
				return true;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return false;

	}

	public SearchResponse searchData(String searchIndex, QueryBuilder query, int from, int size) {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(query);
		sourceBuilder.from(from);
		sourceBuilder.size(size);

		SearchResponse response = null;
		try {
			response = client.search(new SearchRequest(searchIndex).source(sourceBuilder));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;
	}

	public SearchResponse SearchDataByQueryWithHighlighter(String searchIndex, BoolQueryBuilder boolQueryBuilder,
			String hightlightText, int from, int size) {

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		sourceBuilder.query(boolQueryBuilder);
		sourceBuilder.from(from);
		sourceBuilder.size(size);

		sourceBuilder.trackTotalHits(true);

		HighlightBuilder highlightBuilder = new HighlightBuilder();

		HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content").postTags("</span>")
				.preTags("<span style=\"background-color:yellow\">").numOfFragments(0).highlighterType("plain");

		highlightBuilder.field(highlightContent);

		sourceBuilder.highlighter(highlightBuilder);

		SearchResponse response = null;

		try {
			response = client.search(new SearchRequest(searchIndex).source(sourceBuilder));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

}
