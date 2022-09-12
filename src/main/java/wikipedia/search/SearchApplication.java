package wikipedia.search;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import wikipedia.search.daos.ElasticSearch;
import wikipedia.search.daos.MicrosoftSql;

@SpringBootApplication
public class SearchApplication {

	public static void main(String[] args) throws SQLException {

		ElasticSearch es = new ElasticSearch();
		MicrosoftSql msSql = new MicrosoftSql();

		SpringApplication.run(SearchApplication.class, args);

		es.enableESConnection();
		msSql.enableSqlCConnection();

	}
	
	@Bean
	public CorsFilter corsFilter() {
	    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    final CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOrigins(Collections.singletonList("http://localhost:4200")); // Provide list of origins if you want multiple origins
//	    config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept"));
	    config.setAllowedMethods(Arrays.asList("GET", "POST"));
	    config.setAllowCredentials(true);
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}

}