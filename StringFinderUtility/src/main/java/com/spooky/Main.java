package com.spooky;

import org.apache.commons.cli.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (ParseException e) {
            System.err.println("Error parsing command line arguments " + e.getMessage());
        }
    }

    static void run(String[] args) throws ParseException {
        Options options = new Options();
        Option dirToSearchArg = createOption("d", "dir", "DIR", "Directory To Search", false);
        options.addOption(dirToSearchArg);

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options, args);

        Path dirToSearchPath = determineSearchDirectory(commandLine, dirToSearchArg);
    }

    static Path determineSearchDirectory(CommandLine commandLine, Option dirToSearchArg) {
        String dirToSearch;
        if (commandLine.hasOption("d")) {
            dirToSearch = commandLine.getOptionValue(dirToSearchArg);

        } else {
            System.out.println("No directory was given to search, please give one:");
            try (Scanner scanner = new Scanner(System.in)) {
                dirToSearch = scanner.nextLine().trim();
            }
        }

        Path path = Paths.get(dirToSearch);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Invalid directory: " + dirToSearch);
        }

        return path;
    }

    public static SearchSpecification determineSearch(CommandLine cmd, Option dirOption) {
        return null;
    }

    static Option createOption(String shortName, String longName, String argName, String description, boolean required) {
        return Option.builder(shortName)
                .longOpt(longName)
                .argName(argName)
                .desc(description)
                .hasArg()
                .required(required)
                .build();
    }

}