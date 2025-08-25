package com.spooky;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        Option dirToSearchArg = createOption("d", "dir", "DIR", "Directory To Search", false);
        options.addOption(dirToSearchArg);

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options, args);

        String dirToSearch = "ERROR";
        if (commandLine.hasOption("d")) {
            dirToSearch = commandLine.getOptionValue(dirToSearchArg);
        } else {
            dirToSearch = "TODO ASK USER";
        }
    }

    static String parseDirOption(String[] args) {
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