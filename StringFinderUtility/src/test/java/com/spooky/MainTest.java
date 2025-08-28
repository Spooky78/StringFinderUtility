package com.spooky;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testAbsolutePathValid() throws ParseException {
        String absPath = new File(".").getAbsolutePath(); // current working dir
        String[] args = {"-d", absPath};

        Path dir = Main.determineSearchDirectory(args);

        assertEquals(new File(absPath).getPath(), dir.toString());
    }

    @Test
    void testRelativePathValid() throws ParseException {
        String relPath = "."; // relative path to current dir
        String[] args = {"-d", relPath};

        Path dir = Main.determineSearchDirectory(args);

        assertEquals(new File(relPath).getPath(), dir.toString());
    }

    @Test
    void testInvalidPathThrowsError() {
        String invalidPath = "nonexistentDir123456";
        String[] args = {"-d", invalidPath};

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Main.determineSearchDirectory(args)
        );
        assertTrue(ex.getMessage().contains("Invalid directory"));
    }

    @Test
    void testNoPathProvidedPromptsUser() throws Exception {
        InputStream originalIn = System.in; // save the original
        try {
            // Simulate user typing "." (current dir) and pressing enter
            ByteArrayInputStream in = new ByteArrayInputStream(".\n".getBytes(StandardCharsets.UTF_8));
            System.setIn(in);

            String[] args = {}; // no -d
            Path dir = Main.determineSearchDirectory(args);

            assertEquals(new File(".").getPath(), dir.toString());
        } finally {
            System.setIn(originalIn); // restore System.in no matter what
        }
    }

}