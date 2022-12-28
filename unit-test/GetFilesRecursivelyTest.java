package org.example;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.example.Main.getFilesRecursively;
import static org.junit.jupiter.api.Assertions.*;

class GetFilesRecursivelyTest {

    @Test
    public void testGetFilesRecursively() throws IOException {
        // Create a temporary folder structure for testing
        File tempDir = new File("./tempTesting");
        tempDir.mkdir();
        File subdir1 = new File(tempDir, "subdir1");
        subdir1.mkdir();
        File file1 = new File(subdir1, "file1.txt");
        file1.createNewFile();
        File subdir2 = new File(subdir1, "subdir2");
        subdir2.mkdir();
        File file2 = new File(subdir2, "file2.txt");
        file2.createNewFile();
        File file3 = new File(tempDir, "file3.txt");
        file3.createNewFile();

        // Get the files recursively
        Set<File> files = new HashSet<>();
        files = getFilesRecursively(tempDir, files);

        // Assert that the correct number of files were returned
        assertEquals(3, files.size());

        // Assert that all the expected files were returned
        assertTrue(files.contains(file1));
        assertTrue(files.contains(file2));
        assertTrue(files.contains(file3));

    }
}