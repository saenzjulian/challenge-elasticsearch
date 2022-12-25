package org.example;

import org.elasticsearch.client.RestHighLevelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.example.model.Movie;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    static RestHighLevelClient hlrc = new RestHighLevelClientBuilder(
        RestClient.builder(
            new HttpHost("localhost", 9200)
        ).build()
    ).setApiCompatibilityMode(true).build();

    public static void main(String[] args) throws IOException {
        /**
         * TODO
         * 1. get command line arguments
         * - operation (upload, search)
         * - index-name
         * - between quotes, (path or words)
         */
        search();

        File path = new File("../data");
        listFilesForFolder(path, "demo-dos");
    }

    public static Movie mapperFileToMovie(File path) throws IOException {
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferReader = new BufferedReader(fileReader);
        String line;
        String content = "";
        while((line = bufferReader.readLine()) != null){
            content += line;
        }
        return new Movie(path.getName(), content);
    }

    public static void bulkToELK(Movie movie, String index) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);

        //indexRequest.id(String.valueOf(movie.hashCode())); let elk put the id
        indexRequest.source(new ObjectMapper().writeValueAsString(movie), XContentType.JSON);

        hlrc.index(indexRequest, RequestOptions.DEFAULT);
    }

    public static void listFilesForFolder(File folder, String index) throws IOException {
        for (File entry : folder.listFiles()) {
            if (entry.isDirectory()) {
                listFilesForFolder(entry, index);
            } else {
                bulkToELK(
                    mapperFileToMovie(entry),
                    index
                );
            }
        }
    }

    public static void search() throws IOException {
        // Create the query
        MatchQueryBuilder queryELK = QueryBuilders
                .matchQuery("content", "walt disney")
                .operator(Operator.AND);

        // Build the search request
        SearchRequest request = new SearchRequest("demo-dos");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryELK);
        sourceBuilder.sort("title.keyword", SortOrder.ASC);
        //sourceBuilder.fetchSource(new String[]{"title"}, null);
 
        SearchResponse response = hlrc.search(request, RequestOptions.DEFAULT);

        for (SearchHit hit : response.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }

        hlrc.close();
    }

}