package com.fincompare.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "fincompare",
        description = "Airline Financial Analysis CLI Tool",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        subcommands = {CompareCommand.class, AnalyzeCommand.class}
)
public class MainCommand implements Runnable {

    @Override
    public void run() {
        // If no subcommand is provided, show help
        System.out.println("Airline Financial Analyzer CLI");
        System.out.println();
        System.out.println("Available Commands:");
        System.out.println("  compare  - Compare two airlines side-by-side");
        System.out.println("  analyze  - Detailed analysis of a single airline (alias: detail)");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  fincompare compare <ticker1> <ticker2> [options]");
        System.out.println("  fincompare analyze <ticker> [options]");
        System.out.println();
        System.out.println("Run 'fincompare <command> --help' for more information on each command.");
    }
}
