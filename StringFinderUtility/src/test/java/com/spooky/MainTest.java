package com.spooky;

import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void testAbsolutePathValid() throws Exception {
        String absPath = new File(".").getAbsolutePath(); // current working dir
        String[] args = {"-d", absPath};

        String dir = Main.parseDirOption(args);

        assertEquals(new File(absPath).getPath(), dir);
    }

    @Test
    void testRelativePathValid() throws Exception {
        String relPath = "."; // relative path to current dir
        String[] args = {"-d", relPath};

        String dir = Main.parseDirOption(args);

        assertEquals(new File(relPath).getPath(), dir);
    }

    @Test
    void testInvalidPathThrowsError() {
        String invalidPath = "nonexistentDir123456";
        String[] args = {"-d", invalidPath};

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Main.parseDirOption(args)
        );
        assertTrue(ex.getMessage().contains("Invalid directory"));
    }

    @Test
    void testNoPathProvidedPromptsUser() throws Exception {
        // Simulate user typing "." (current dir) and pressing enter
        ByteArrayInputStream in = new ByteArrayInputStream(".\n".getBytes(StandardCharsets.UTF_8));
        System.setIn(in);

        String[] args = {}; // no -d

        String dir = Main.parseDirOption(args);

        assertEquals(new File(".").getPath(), dir);
    }

}