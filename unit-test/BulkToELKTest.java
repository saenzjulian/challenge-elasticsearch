package org.example;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.example.model.Movie;
import org.junit.After;
import org.junit.Before;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.example.Main.bulkToELK;
import static org.junit.jupiter.api.Assertions.*;

public class BulkToELKTest {
    private static final String INDEX_NAME = "test-index";
    private static final String MOVIE_NAME = "test-movie.txt";
    private static final String MOVIE_CONTENT = "Lorem Ipsum is simply dummy text of the printing and typesetting industry";

    // Create a RestHighLevelClient for testing
    private RestHighLevelClient hlrc = new RestHighLevelClientBuilder(
            RestClient.builder(
                    new HttpHost("localhost", 9200)
            ).build()
    ).setApiCompatibilityMode(true).build();

    @Before
    public void beforeTest() throws IOException {
        // Create the index for testing
        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        CreateIndexResponse response = hlrc.indices().create(request, RequestOptions.DEFAULT);
        assertTrue(response.isAcknowledged());
    }

    @After
    public void afterTest() throws IOException {
        // Delete the index after testing
        DeleteIndexRequest request = new DeleteIndexRequest(INDEX_NAME);

        IndicesClient indicesClient = hlrc.indices();
        AcknowledgedResponse deleteIndexResponse = indicesClient.delete(request, RequestOptions.DEFAULT);

        assertTrue(deleteIndexResponse.isAcknowledged());

        // Close the RestHighLevelClient
        hlrc.close();
    }

    @Test
    public void testBulkToELK() throws IOException {
        // Create a Movie object for testing
        Movie movie = new Movie(MOVIE_NAME, MOVIE_CONTENT);

        // Bulk the Movie object to ELK
        bulkToELK(movie, INDEX_NAME);

        // Search for the Movie object in ELK
        MatchQueryBuilder queryELK = QueryBuilders
                .matchQuery("content", MOVIE_CONTENT)
                .operator(Operator.AND);

        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryELK);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = hlrc.search(searchRequest, RequestOptions.DEFAULT);

        // Assert that the Movie object was found
        assertEquals(1, searchResponse.getHits().getTotalHits().value);

    }
}