package com.spooky;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * The main entry point for the StringFinderUtility application.
 * <p>
 * This application allows users to search for files within a specified directory
 * that match a given regular expression pattern. Users can provide the directory
 * and regex pattern as command-line arguments or interactively via prompts.
 * The utility validates the inputs and prepares for searching files based on the
 * provided criteria.
 */
public class Main {

  /**
   * Main method that runs the program.
   *
   * @param args Program arguments
   */
  public static void main(String[] args) {
    run(args);
  }

  private static void run(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
      Options options = buildOptions();

      CommandLineParser commandLineParser = new DefaultParser();
      CommandLine commandLine = commandLineParser.parse(options, args);

      String searchSpecification = determineSearchRegex(commandLine, scanner);
      Path dirToSearchPath = determineSearchDirectory(commandLine, scanner);
    } catch (IllegalArgumentException e) {
      System.err.println("ERROR: Something went wrong with pattern syntax");
    } catch (ParseException e) {
      System.err.println("ERROR: couldn't parse arguments");
    }
  }

  private static Options buildOptions() {
    Options options = new Options();
    options.addOption(createOption("d", "dir", "DIR", "Directory To Search", false));
    options.addOption(createOption("r", "regex", "REGEX", "Regex To Search For", false));
    return options;
  }

  private static Path determineSearchDirectory(CommandLine commandLine, Scanner scanner) {
    String dirToSearch;
    if (commandLine.hasOption("d")) {
      dirToSearch = commandLine.getOptionValue("d");

    } else {
      System.out.println("No directory was given to search, please give one:");
      if (scanner.hasNextLine()) {
        dirToSearch = scanner.nextLine().trim();
      } else {
        throw new IllegalArgumentException("No directory input provided");
      }
    }

    Path path = Paths.get(dirToSearch);

    if (!Files.exists(path)) {
      throw new IllegalArgumentException("Invalid directory: " + dirToSearch);
    }

    return path;
  }

  private static String determineSearchRegex(CommandLine commandLine, Scanner scanner) {
    String regexToSearch;
    if (commandLine.hasOption("r")) {
      regexToSearch = commandLine.getOptionValue("r");

    } else {
      System.out.println("No regex was given to search, please give one:");
      if (scanner.hasNextLine()) {
        regexToSearch = scanner.nextLine().trim();
      } else {
        throw new IllegalArgumentException("No regex input provided");
      }
    }

    try {
      java.util.regex.Pattern.compile(regexToSearch);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid regular expression: " + regexToSearch);
    }

    return regexToSearch;
  }


  private static Option createOption(String shortName, String longName, String argName,
                                     String description, boolean required) {
    return Option.builder(shortName)
        .longOpt(longName)
        .argName(argName)
        .desc(description)
        .hasArg()
        .required(required)
        .build();
  }

}