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
            } catch (Exception ignored) {
            }
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

    //region DetermineSearch

    @Test
    void testDetermineSearchStringWithValidString() throws ParseException {
        String testSearchString = "testSearchString";
        var options = new Options();
        var dirOption = Main.createOption("s", "str", "STR", "String to search for", false);
        options.addOption(dirOption);

        String[] args = {"-s", testSearchString};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        SearchSpecification result = Main.determineSearch(cmd, dirOption);
        assertNotNull(result);
        assertEquals(testSearchString, result.getValue());
    }

    @Test
    void testDetermineSearchRegexWithValidRegex() throws ParseException {
        String testSearchString = "test(Search)String";
        var options = new Options();
        var dirOption = Main.createOption("r", "reg", "REG", "Regex to search for", false);
        options.addOption(dirOption);

        String[] args = {"-r", testSearchString};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        SearchSpecification result = Main.determineSearch(cmd, dirOption);
        assertNotNull(result);
        assertEquals(testSearchString, result.getValue());
    }

    @Test
    void testDetermineSearchWithBothFlagsThrows() throws ParseException {
        String testSearchString = "search";
        var options = new Options();
        var searchOption = Main.createOption("s", "str", "STR", "String to search for", false);
        var regexOption = Main.createOption("r", "reg", "REG", "Regex to search for", false);
        options.addOption(searchOption);
        options.addOption(regexOption);

        String[] args = {"-s", testSearchString, "-r", testSearchString};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        assertThrows(IllegalArgumentException.class, () -> Main.determineSearch(cmd, searchOption));
    }

    @Test
    void testDetermineSearchWithNoSearchPromptsUser() {
        // Simulate prompt by setting System.in
        InputStream originalIn = System.in;
        String testSearchString = "promptedString";
        try {
            System.setIn(new ByteArrayInputStream((testSearchString + System.lineSeparator()).getBytes()));
            var options = new Options();
            var searchOption = Main.createOption("s", "str", "STR", "String to search for", false);
            options.addOption(searchOption);

            String[] args = {};
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            SearchSpecification result = Main.determineSearch(cmd, searchOption);
            assertNotNull(result);
            assertEquals(testSearchString, result.getValue());
        } finally {
            System.setIn(originalIn);
        }
    }

    @Test
    void testDetermineSearchWithIgnoreCaseString() throws ParseException {
        String testSearchString = "search";
        var options = new Options();
        var searchOption = Main.createOption("s", "str", "STR", "String to search for", false);
        var ignoreCaseOption = Main.createOption(null, "ignore-case", null, "Ignore case", false);
        options.addOption(searchOption);
        options.addOption(ignoreCaseOption);

        String[] args = {"-s", testSearchString, "--ignore-case"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        SearchSpecification result = Main.determineSearch(cmd, searchOption);
        assertNotNull(result);
        assertFalse(result.isCaseSensitive());
    }

    @Test
    void testDetermineSearchWithIgnoreCaseRegex() throws ParseException {
        String testSearchString = "search.*";
        var options = new Options();
        var regexOption = Main.createOption("r", "reg", "REG", "Regex to search for", false);
        var ignoreCaseOption = Main.createOption(null, "ignore-case", null, "Ignore case", false);
        options.addOption(regexOption);
        options.addOption(ignoreCaseOption);

        String[] args = {"-r", testSearchString, "--ignore-case"};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        SearchSpecification result = Main.determineSearch(cmd, regexOption);
        assertFalse(result.isCaseSensitive());
    }

    @Test
    void testDetermineSearchWithInvalidRegexThrows() throws ParseException {
        String invalidRegex = "[unterminated";
        var options = new Options();
        var regexOption = Main.createOption("r", "reg", "REG", "Regex to search for", false);
        options.addOption(regexOption);

        String[] args = {"-r", invalidRegex};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        assertThrows(IllegalArgumentException.class, () -> Main.determineSearch(cmd, regexOption));
    }

    @Test
    void testDetermineSearchWithEmptyStringThrows() throws ParseException {
        var options = new Options();
        var searchOption = Main.createOption("s", "str", "STR", "String to search for", false);
        options.addOption(searchOption);

        String[] args = {"-s", ""};
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        assertThrows(IllegalArgumentException.class, () -> Main.determineSearch(cmd, searchOption));
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