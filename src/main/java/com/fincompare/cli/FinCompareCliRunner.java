package com.fincompare.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class FinCompareCliRunner implements CommandLineRunner, ExitCodeGenerator {

    private final IFactory factory;
    private final MainCommand mainCommand;
    private int exitCode;

    public FinCompareCliRunner(IFactory factory, MainCommand mainCommand) {
        this.factory = factory;
        this.mainCommand = mainCommand;
    }

    @Override
    public void run(String... args) throws Exception {
        CommandLine commandLine = new CommandLine(mainCommand, factory);

        exitCode = commandLine.execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }
}
