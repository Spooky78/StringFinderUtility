package com.spooky;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @TempDir
    Path tempDir;

    //region Setup/Teardown
    private final PrintStream originalErr = System.err;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream errContent;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void setupStreams() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setErr(originalErr);
        System.setOut(originalOut);
    }
    //endregion

    //region Main Method Tests

    @Test
    void testMainSuccessfulPath() {
        String[] args = {"-d", tempDir.toString()};
        assertDoesNotThrow(() -> Main.main(args));
        assertTrue(errContent.toString().isEmpty());
    }

    @Test
    void testMainWithInvalidParseException() {
        String[] args = {"-unknown", "foo"};
        assertAll(() -> {
            try {
                Main.main(args);
            } catch (Exception ignored) {}
            assertTrue(errContent.toString().contains("Error parsing command line arguments"));
        });
    }

    @Test
    void testDefaultConstructorCoverage() {
        new Main();
    }

    //endregion

    //region Run Method Tests

    @Test
    void testRunWithValidDirectoryArgument() {
        String[] args = {"-d", tempDir.toString()};
        assertDoesNotThrow(() -> Main.run(args));
        assertTrue(errContent.toString().isEmpty());
    }

    @Test
    void testRunWithInvalidDirectoryArgument() {
        String invalidDir = tempDir.resolve("does_not_exist").toString();
        String[] args = {"-d", invalidDir};

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            Main.run(args);
        });

        assertTrue(thrown.getMessage().contains("Invalid directory"));
    }

    @Test
    void testRunWithNoArgumentsAndValidUserInput() {
        InputStream originalIn = System.in;
        try {
            String input = tempDir.toString() + System.lineSeparator();
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            String[] args = {};
            assertDoesNotThrow(() -> Main.run(args));
            assertTrue(outContent.toString().contains("No directory was given to search"));
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    void testRunWithNoArgumentsAndInvalidUserInput() {
        InputStream originalIn = System.in;
        try {
            String input = tempDir.resolve("does_not_exist").toString() + System.lineSeparator();
            System.setIn(new ByteArrayInputStream(input.getBytes()));
            String[] args = {};
            assertThrows(IllegalArgumentException.class, () -> Main.run(args));
            assertTrue(outContent.toString().contains("No directory was given to search"));
        } finally {
            System.setIn(originalIn);
        }
    }

    //endregion

    //region DetermineSearchDirectory Tests

    @Test
    void testDetermineSearchDirectoryWithValidPath() throws ParseException {
        var options = new Options();
        var dirOption = Main.createOption("d", "dir", "DIR", "Directory To Search", false);
        options.addOption(dirOption);

        String[] args = {"-d", tempDir.toString()};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        Path result = Main.determineSearchDirectory(cmd, dirOption);
        assertEquals(tempDir, result);
    }

    @Test
    void testDetermineSearchDirectoryWithInvalidPath() throws ParseException {
        var options = new Options();
        var dirOption = Main.createOption("d", "dir", "DIR", "Directory To Search", false);
        options.addOption(dirOption);

        String invalidDir = tempDir.resolve("does_not_exist").toString();
        String[] args = {"-d", invalidDir};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        assertThrows(IllegalArgumentException.class, () -> Main.determineSearchDirectory(cmd, dirOption));
    }

    //endregion

    //region Utility Method Tests

    @Test
    void testCreateOptionAndAddOptionCoverage() {
        var options = new org.apache.commons.cli.Options();
        var dirToSearchArg = Main.createOption("d", "dir", "DIR", "Directory To Search", false);
        assertNotNull(dirToSearchArg);
        assertEquals("d", dirToSearchArg.getOpt());
        assertEquals("dir", dirToSearchArg.getLongOpt());
        assertEquals("Directory To Search", dirToSearchArg.getDescription());
        assertFalse(dirToSearchArg.isRequired());
        options.addOption(dirToSearchArg);
        assertTrue(options.hasOption("d"));
    }

    //endregion
}