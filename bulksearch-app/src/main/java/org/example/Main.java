package org.example;

import org.elasticsearch.client.RestHighLevelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.example.model.Movie;
import org.example.model.Response;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main {

    static RestHighLevelClient hlrc = new RestHighLevelClientBuilder(
        RestClient.builder(
            new HttpHost("localhost", 9200)
        ).build()
    ).setApiCompatibilityMode(true).build();

    public static void main(String[] args) throws IOException {
        //search("demo", "walt disney");
        //nativeSearch("demo","walt disney");

        // Getting parameters from command line
        String operation = args[0] == "" ? "index" : args[0];
        String indexName = args[1];
        String thirdParameter = args[2];

        // Treatment of parameters
        Response response = new Response();
        switch (operation) {
            case "upload":
                listFilesForFolder(new File(thirdParameter), indexName);
                break;
            case "search":
                response = search(indexName, thirdParameter);
                break;
            case "native-search":
                response = nativeSearch(indexName, thirdParameter);
                break;
            default:
                System.out.println("Invalid command");
        }

        // Getting report
        if (response.getMovies().size() > 0 && (operation.equals("search") || operation.equals("native-search"))) {
            String message = "Search [" + response.getTarget()
                    + "] took [" + response.getMilliseconds()
                    + "] ms and found [" + response.getMovies().size() + "] results:";

            System.out.println(message);

            for (String movie : response.getMovies()) {
                System.out.println(movie);
            }
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

    public static Response search(String indexName, String words) throws IOException {
        // Create the query
        MatchQueryBuilder queryELK = QueryBuilders
                .matchQuery("content", words)
                .operator(Operator.AND);

        // Create the request
        SearchRequest request = new SearchRequest(indexName);

        // Build the request
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryELK);
        sourceBuilder.sort("title.keyword", SortOrder.ASC);
        sourceBuilder.trackTotalHits(true);
        sourceBuilder.size(10000);
        sourceBuilder.fetchSource(new String[]{"title"}, null);

        // Prepare the request
        request.source(sourceBuilder);

        // Execute the request
        SearchResponse response = hlrc.search(request, RequestOptions.DEFAULT);

        // Map results
        ObjectMapper om = new ObjectMapper();
        List<String> movies = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            movies.add(om.readValue(hit.getSourceAsString(), Movie.class).getTitle());
        }

        // Close the connection
        hlrc.close();

        return new Response(words, response.getTook().getMillis(), movies);
    }

    private static Response nativeSearch(String indexName, String words) throws IOException {
        String payload = "{\"_source\": [\"title\"], \"query\": {\"match\": {\"content\": {\"query\": \"" + words + "\",\"operator\": \"and\"}}},\"sort\": [{\"title.keyword\": {\"order\": \"asc\"}}]}";

        // Create url with endpoint
        URL url = new URL("http://127.0.0.1:9200/" + indexName + "/_search?size=10000");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        // Create GET request
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true); // Configure the connection to write the payload on GET request

        // Write the payload on request
        OutputStream os = con.getOutputStream();
        os.write(payload.getBytes());
        os.flush();
        os.close();

        // Retrieve the response body
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Map<Object, Object> mapping = new ObjectMapper().readValue(response.toString(), HashMap.class);

        ArrayList<LinkedHashMap<Object, Object>> title = (ArrayList<LinkedHashMap<Object, Object>>) ((Map) mapping.get("hits")).get("hits");

        List<String> movies = new ArrayList<>();
        for (var x : title) {
            movies.add( ((Map) x.get("_source")).get("title").toString() );
        }
        Long took = ((Number) mapping.get("took")).longValue();
        return new Response(words, took, movies);
    }

}