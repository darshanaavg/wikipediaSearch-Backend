package wikipedia.search.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import wikipedia.search.models.WikipediaContent;
import wikipedia.search.models.searchRequest;
import wikipedia.search.models.WikipediaMetaData;
import wikipedia.search.services.wikipediaContentService;

@RestController
@CrossOrigin
public class APIControllers {

	@RequestMapping(value = "/wiki/lists", method = RequestMethod.GET)
	public ResponseEntity<List<WikipediaMetaData>> getWikiPagesCount() {

		wikipediaContentService w = new wikipediaContentService();

		List<WikipediaMetaData> wikiMetaData = w.getWikipediaPagesCount();

		return new ResponseEntity<List<WikipediaMetaData>>(wikiMetaData, HttpStatus.OK);

	}

	@RequestMapping(value = "/wiki/move", method = RequestMethod.POST)
	public ResponseEntity<String> loadWikipediaContentsToES(@RequestBody String text) {

		wikipediaContentService wikiService = new wikipediaContentService();

		String message = wikiService.loadWikipediaContentsToES(text);

		return new ResponseEntity<String>(message, HttpStatus.OK);

	}

	@RequestMapping(value = "wiki/contents", method = RequestMethod.GET)
	public ResponseEntity<Map> getWikiContent(@RequestParam String text, @RequestParam int from,
			@RequestParam int size, @RequestParam(name = "searchText", defaultValue = "") String searchText) {

		wikipediaContentService wikiService = new wikipediaContentService();

		System.out.println(searchText);
		Map wikiContents = wikiService.getWikipediaContentByKey(text, from, size, searchText);

		return new ResponseEntity<Map>(wikiContents, HttpStatus.OK);

	}

	@RequestMapping(value = "wiki/search", method = RequestMethod.GET)
	public ResponseEntity<Map> getWikiContentForSearch(@RequestParam String text,
			@RequestParam int from, @RequestParam int size,
			@RequestParam(name = "searchText", defaultValue = "") String searchText) {

		wikipediaContentService wikiService = new wikipediaContentService();

		Map wikiContents = wikiService.getWikipediaContentByKey(text, from, size, searchText);

		return new ResponseEntity<Map>(wikiContents, HttpStatus.OK);

	}

	@RequestMapping(value = "wiki/status", method = RequestMethod.GET)
	public ResponseEntity<WikipediaMetaData> getSuccessStatus(@RequestParam String searchText) {

		wikipediaContentService wikiService = new wikipediaContentService();

		WikipediaMetaData wikiContents = wikiService.getSuccessStatus(searchText);

		return new ResponseEntity<WikipediaMetaData>(wikiContents, HttpStatus.OK);

	}

}
