package com.fincompare.cli;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
        name = "fincompare",
        description = "Airline Financial Analysis CLI Tool",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        subcommands = {CompareCommand.class}
)
public class MainCommand implements Runnable {

    @Override
    public void run() {
        // If no subcommand is provided, show help
        System.out.println("Airline Financial Analyzer CLI");
        System.out.println("Usage: fincompare compare <ticker1> <ticker2> [options]");
        System.out.println();
        System.out.println("Run 'fincompare compare --help' for more information.");
    }
}
