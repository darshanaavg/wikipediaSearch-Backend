Tech Stack and the programming languages to use:
Elasticsearch, ms sql, spring boot for api development, ember.js for frontend and java programming language.

Versions used by me:
1. Elasticsearch - 6.0.1
2. Spring boot - 2.7.3
3. Java - 11
4. Ember.js - 3.19.0

Problem statement:
1. https://en.wikipedia.org/w/index.php?search=zebroid&title=Special:Search&profile=advanced&fulltext=1&ns0=1 => When a keyword is entered in the wikipedia search, it lists all the 
  pages related to the keyword. So, we need to use the apis offered by wikipedia to get the contents of each and every pages of the search result in a plain text.
2. The contents obtained from the wikipedia should be moved to the elastic search with a proper schema, so that we can retrive the data by selecting the keywords.
3. Then, the number of pages for each keyword should be updated in the ms sql with a proper schema.
4. Use ember.js as a frontend and lists the keywords with the number of pages in the elastic search should be listed in the home page, when the user types some keyword the contents 
   should be moved to the elasticsearch in a way that the live counts moved should be updated in the UI.
5. When the user clicks the keyword, the pages from ES( elastic search ) should be shown with the title and the content limited with the pagination.
6. Place a search bar so when the word is searched, the hit count should be shown and the searched word should be highlighted in the contents.
7. Then, Limit the size of the index in Elastic search with maximum number of documents. So, when it reaches the limit, new index should be created and next contents should 
  be moved to the new index. But the search should be across the multiple indices. ( This can be easily achieved by the index life cycle management for the ES versions >
  6.2.0. Since I have chosen the older version, I have used the elasticsearch curator for index rollover)
