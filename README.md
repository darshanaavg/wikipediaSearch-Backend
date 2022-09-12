Tech Stack and the programming languages to use:
Elasticsearch, ms sql, spring boot for api development, ember.js for frontend and java programming language.

Versions used by me:
1. Elasticsearch - 6.0.1
2. Spring boot - 2.7.3
3. Java - 11
4. Ember.js - 3.19.0

Problem statement:
1. https://en.wikipedia.org/w/index.php?search=zebroid&title=Special:Search&profile=advanced&fulltext=1&ns0=1 => When a keyword is entered in the wikipedia search, it lists all the pages related to the keyword. So, we need to use the apis offered by wikipedia to get the contents of each and every pages of the search result in a plain text.
2. The contents obtained from the wikipedia should be moved to the elastic search with a proper schema, so that we can retrive the data by selecting the keywords.
3. Then, the number of pages for each keyword should be updated in the ms sql with a proper schema.
4. Use ember.js as a frontend and lists the keywords with the number of pages in the elastic search should be listed in the home page, when the user types some keyword the contents 
   should be moved to the elasticsearch in a way that the live counts moved should be updated in the UI.
5. When the user clicks the keyword, the pages from ES( elastic search ) should be shown with the title and the content limited with the pagination.
6. Place a search bar so when the word is searched, the hit count should be shown and the searched word should be highlighted in the contents.
7. Then, Limit the size of the index in Elastic search with maximum number of documents. So, when it reaches the limit, new index should be created and next contents should 
  be moved to the new index. But the search should be across the multiple indices. ( This can be easily achieved by the index life cycle management for the ES versions >
  6.2.0. Since I have chosen the older version, I have used the elasticsearch curator for index rollover)
  
 Solution:
 --> This repo gives only the backend functionalities and the frontend is in the wikipediaSearch-Frontend repo.

 --> Initially, create a spring boot maven project using the spring initailizr.
 
 --> Then, establish the connection to ES using the RestHighLevelClient and MS SQL using the jdbc driver.
 
 --> Since the ember.js's localhost is in 4200 and the ES port is 9200, we need define the CorsFilter in main function and the CrosOrigin in the API controllers.
 
 --> Design the models for ES ( wikipedia contents ) and ms sql ( data about the wikipedia )
 
 --> Model - WikipediaContent
      1. pageId - Uniquely finds the page in the wikipedia
      2. key - the Keyword given to move the contents from wikipedia to ES
	   3. title - Title of the wikipedia page
	   4. content - content of the wikipedia page
      
  --> Model - wikipediaMetaData
   1. title - keyword given to move the contents form wiki to ES
   2. count - No. of wiki pages loaded from wiki to ES
   3. success - 0/1 ( checks whether all the pages from wiki are loaded to ES. Initially will be 0, once all the pages content were moved will be updated to 1 )


 
