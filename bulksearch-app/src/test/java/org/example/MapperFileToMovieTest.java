package org.example;

import org.example.model.Movie;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.example.Main.mapperFileToMovie;
import static org.junit.jupiter.api.Assertions.*;

class MapperFileToMovieTest {

    @Test
    public void testMapperFileToMovie() throws IOException {
        // Create a temporary file for testing
        File tempDir = new File("./tempTesting");
        tempDir.mkdir();
        File tempFile = new File("./tempTesting/file1.txt");
        tempFile.createNewFile();

        // Write some content to the file
        writeToFile(tempFile, "Lorem Ipsum is simply dummy text of the printing and typesetting industry");

        // Map the file to a Movie object
        Movie movie = mapperFileToMovie(tempFile);

        // Assert that the Movie object has the correct name and content
        assertEquals("file1.txt", movie.getTitle());
        assertEquals("Lorem Ipsum is simply dummy text of the printing and typesetting industry", movie.getContent());

    }

    public void writeToFile(File file, String content) throws IOException {
        // Create a FileWriter and a BufferedWriter for the file
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        // Write the content to the file
        bufferedWriter.write(content);

        // Close the BufferedWriter to save the changes
        bufferedWriter.close();
    }

}