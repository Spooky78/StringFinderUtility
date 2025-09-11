package com.spooky;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Scanner;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

  private final PrintStream originalErr = System.err;
  private final PrintStream originalOut = System.out;
  private final InputStream originalIn = System.in;
  @TempDir
  Path tempDir;
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
    System.setIn(originalIn);
  }

  //region Main

  @Test
  void testMainClassInstantiation() {
    new Main(); // ensures class is loaded and default constructor is covered
  }

  //endregion

  //region Main.main

  @Test
  void testMainSuccessfulPath() {
    String[] args = {"-d", tempDir.toString(), "-r", "testString"};
    assertDoesNotThrow(() -> Main.main(args));
    assertFalse(errContent.toString().contains("Error parsing command line arguments"));
  }

  //endregion

  //region Main.run

  @Test
  void testRunPrintsErrorMessageToSystemErr() throws Exception {
    // Redirect System.err to capture output
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    PrintStream originalErr = System.err;
    System.setErr(new PrintStream(errContent));

    try {
      // Use arguments that will cause an exception in run, e.g., unknown option
      String[] args = {"--unknownOption"};
      Method runMethod = Main.class.getDeclaredMethod("run", String[].class);
      runMethod.setAccessible(true);

      // Call the private run method
      runMethod.invoke(null, (Object) args);

      // Check that the error message was written to System.err
      String errOutput = errContent.toString();
      assertTrue(errOutput.contains("ERROR: Something went wrong"),
          "Expected error message was not found in System.err output");
    } finally {
      // Restore System.err
      System.setErr(originalErr);
    }
  }

  //endregion

  //region Main.determineSearchDirectory

  @Test
  void testDetermineSearchDirectory_WithValidArgumentsPath__ReturnsDirectory() throws Exception {
    var options = new Options();
    options.addOption(reflectCreateOption("d", "dir", "DIR", "Directory To Search", false));

    String[] args = {"-d", tempDir.toString()};
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]))) {
      Path result = reflectDetermineSearchDirectory(cmd, scanner);
      assertEquals(tempDir, result);
    }
  }

  @Test
  void testDetermineSearchDirectory_NoArgumentsAndValidUserInputInvalidDirectory_ThrowsIllegalArgumentException()
      throws Exception {
    String input = tempDir.resolve("does_not_exist")
        + System.lineSeparator()
        + System.lineSeparator();
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    var options = new Options();
    String[] args = {};
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(System.in)) {
      Throwable thrown = assertThrows(InvocationTargetException.class,
          () -> reflectDetermineSearchDirectory(cmd, scanner));

      assertInstanceOf(IllegalArgumentException.class, thrown.getCause());
      assertTrue(outContent.toString().contains("No directory was given to search"));
    }
  }

  @Test
  void testDetermineSearchDirectory_NoArgumentsAndInvalidUserInput_ThrowsIllegalArgumentException()
      throws Exception {
    System.setIn(new ByteArrayInputStream(new byte[0])); // no input at all

    var options = new Options();
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, new String[0]);

    try (Scanner scanner = new Scanner(System.in)) {
      IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        try {
          reflectDetermineSearchDirectory(cmd, scanner);
        } catch (InvocationTargetException e) {
          throw (e.getCause() instanceof Exception ex) ? ex : new RuntimeException(e.getCause());
        }
      });

      assertEquals("No directory input provided", thrown.getMessage());
    }
  }

  //endregion

  //region Main.determineSearchRegex

  @Test
  void testDetermineSearchRegex_ValidArgumentsRegex_ReturnsRegex() throws Exception {
    String testSearchString = "test(Search)String";
    var options = new Options();
    options.addOption(reflectCreateOption("r", "regex", "REGEX", "Regex pattern to search", false));

    String[] args = {"-r", testSearchString};
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]))) {
      String result = reflectDetermineSearchRegex(cmd, scanner);
      assertEquals(testSearchString, result);
    }
  }

  @Test
  void testDetermineSearchRegex_InvalidArgumentsRegex_ReturnsRegex() throws Exception {
    String testSearchString = "test(SearchString";
    var options = new Options();
    options.addOption(reflectCreateOption("r", "regex", "REGEX", "Regex pattern to search", false));

    String[] args = {"-r", testSearchString};
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]))) {
      IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        try {
          reflectDetermineSearchRegex(cmd, scanner);
        } catch (InvocationTargetException e) {
          throw (e.getCause() instanceof Exception ex) ? ex : new RuntimeException(e.getCause());
        }
      });
      assertEquals("Invalid regular expression: test(SearchString", thrown.getMessage());
    }
  }

  @Test
  void testDetermineSearchRegex_NoArgumentsAndValidUserInput_ReturnsRegex() throws Exception {
    String testSearchString = "promptedString";
    String input = testSearchString + System.lineSeparator();
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    var options = new Options();
    String[] args = {};
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(System.in)) {
      String result = reflectDetermineSearchRegex(cmd, scanner);
      assertEquals(testSearchString, result);
    }
  }

  @Test
  void testDetermineSearchRegex_NoArgumentsAndInvalidUserInput_ThrowsIllegalArgumentException()
      throws Exception {
    var options = new Options();
    options.addOption(reflectCreateOption("r", "regex", "REGEX", "Regex pattern to search", false));

    String[] args = {}; // no -r option provided
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    try (Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]))) {
      IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
        try {
          reflectDetermineSearchRegex(cmd, scanner);
        } catch (InvocationTargetException e) {
          throw (e.getCause() instanceof Exception ex) ? ex : new RuntimeException(e.getCause());
        }
      });

      assertEquals("No regex input provided", thrown.getMessage());
    }
  }

  //endregion

  //region Reflection utilities

  private static Option reflectCreateOption(String shortName, String longName, String argName,
                                            String description, boolean required) throws Exception {
    Method m = Main.class.getDeclaredMethod("createOption",
        String.class, String.class, String.class, String.class, boolean.class);
    m.setAccessible(true);
    return (Option) m.invoke(null, shortName, longName, argName, description, required);
  }

  private static Path reflectDetermineSearchDirectory(CommandLine cmd, Scanner scanner)
      throws Exception {
    Method m =
        Main.class.getDeclaredMethod("determineSearchDirectory", CommandLine.class, Scanner.class);
    m.setAccessible(true);
    return (Path) m.invoke(null, cmd, scanner);
  }

  //region Main.main

  private static String reflectDetermineSearchRegex(CommandLine cmd, Scanner scanner)
      throws Exception {
    Method m =
        Main.class.getDeclaredMethod("determineSearchRegex", CommandLine.class, Scanner.class);
    m.setAccessible(true);
    return (String) m.invoke(null, cmd, scanner);
  }

  //endregion
}