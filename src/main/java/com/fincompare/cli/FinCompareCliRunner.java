package com.fincompare.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class FinCompareCliRunner implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final CompareCommand compareCommand;
    private int exitCode;

    public FinCompareCliRunner(IFactory factory, CompareCommand compareCommand) {
        this.factory = factory;
        this.compareCommand = compareCommand;
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLine commandLine = new CommandLine(compareCommand, factory);

        commandLine.setCommandName("fincompare");

        exitCode = commandLine.execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
