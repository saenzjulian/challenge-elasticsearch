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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    static RestHighLevelClient hlrc = new RestHighLevelClientBuilder(
        RestClient.builder(
            new HttpHost("localhost", 9200)
        ).build()
    ).setApiCompatibilityMode(true).build();

    public static void main(String[] args) throws IOException {
        demo();
        String operation = args[0] == "" ? "index" : args[0];
        String indexName = args[1];
        String thirdParameter = args[2];

        switch (operation) {
            case "upload":
                listFilesForFolder(new File(thirdParameter), indexName);
                break;
            case "search":
                search(indexName, thirdParameter);
                break;
            default:
                System.out.println("Invalid command");
        }

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
        hlrc.close();
    }

    public static void search(String indexName, String words) throws IOException {
        // Create the query
        MatchQueryBuilder queryELK = QueryBuilders
                .matchQuery("content", words)
                .operator(Operator.AND);

        // Build the search request
        SearchRequest request = new SearchRequest(indexName);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryELK);
        sourceBuilder.sort("title.keyword", SortOrder.ASC);
        sourceBuilder.trackTotalHits(true);
        //sourceBuilder.size(Integer.MAX_VALUE);
        sourceBuilder.size(10000);
        sourceBuilder.fetchSource(new String[]{"title"}, null);

        request.source(sourceBuilder);

        long start = System.currentTimeMillis();
        SearchResponse response = hlrc.search(request, RequestOptions.DEFAULT);
        long finish = System.currentTimeMillis();

        System.out.println("your search appear [" + response.getHits().getTotalHits().value + "] times");

        ObjectMapper om = new ObjectMapper();
        for (SearchHit hit : response.getHits().getHits()) {
            //movie = om.readValue(hit.getSourceAsString(), Movie.class);
            System.out.println(om.readValue(hit.getSourceAsString(), Movie.class).getTitle());
        }

        System.out.println("Time elapsed: " + (finish - start) + " ms");

        hlrc.close();
    }

    public static void demo() throws IOException {
        // Define the payload as a string
        String payload = "{\"_source\": [\"title\"], \"query\": {\"match\": {\"content\": {\"query\": \"disney walt\",\"operator\": \"and\"}}},\"sort\": [{\"title.keyword\": {\"order\": \"asc\"}}]}";

        // Create a new URL object with the desired URL
        URL url = new URL("http://127.0.0.1:9200/demo/_search?size=100");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Set the request method to "GET" and the content type to "application/json"
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");

        // Set the output stream to true to enable writing the payload to the request
        con.setDoOutput(true);

        // Write the payload to the request
        long start = System.currentTimeMillis();
        OutputStream os = con.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();
        long end = System.currentTimeMillis();
        // Retrieve the response code and response body
        int responseCode = con.getResponseCode();
        System.out.println("Response code: " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
        System.out.println("Time elapsed: " + (end - start) + " ms");

    }


}