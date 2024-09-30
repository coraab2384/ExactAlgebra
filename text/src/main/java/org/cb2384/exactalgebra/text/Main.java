package org.cb2384.exactalgebra.text;

import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.cb2384.exactalgebra.text.parse.Command;
import org.cb2384.exactalgebra.text.parse.Command.ReservedNames;
import org.cb2384.exactalgebra.text.parse.Interfacer;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    static void runLoop(
            Interfacer instance
    ) {
        instance.writeLine("Welcome!");
        instance.writeLine("\"{\" and \"}\" signify the beginning and end of an argument list; \"\\\""
                + " is prepended to the actual command.");
        instance.writeLine("Commands can go in front of the arguments, or inside, though the location"
                + " inside must be first if it is a unary operation like negation, and otherwise second.");
        instance.writeLine("Arguments should be separated by commas");
        instance.writeLine("To instantiate a number object, wrap the value in parentheses: \"(2.5)\""
                + " creates a new value with that value, and as an exact rational.");
        instance.writeLine("Parsing of values is handled as if through Java's own BigDecimal class.");
        instance.writeLine("Brackets with comma-separated values like \"[6, 0, -3, 2]\""
                + " create a polynomial with the corresponding coefficients, in descending order.");
        instance.writeLine("The above example converts to 6x" + (char) 0xB3
                + " - 3x + 2." + System.lineSeparator());
        instance.writeLine("Operations are named as follows, separated by a pipe (\"|\")"
                + " with synonyms grouped in parentheses");
        instance.writeLine(Identifier.groupedPatternCompiler(Arrays.asList(OpNames.values())).pattern());
        instance.writeLine("Utilities are named as follows, separated by a pipe (\"|\")"
                + " with synonyms grouped in parentheses");
        instance.writeLine(Identifier.groupedPatternCompiler(Arrays.asList(Utils.values())).pattern());
        instance.writeLine("Other important terms are:");
        instance.writeLine(Identifier.groupedPatternCompiler(Arrays.asList(ReservedNames.values())).pattern());
        instance.writeLine("As well as \"true\" and \"false\" for boolean values and these for rounding modes:");
        instance.writeLine(Identifier.patternOrCompiler(Arrays.stream(RoundingMode.values())
                .map(RoundingMode::toString)
                .map(StringUtils::toCamelCase)
                .map(String::toLowerCase)
                .collect(Collectors.toSet())).pattern());
        
        while (true) {
            String rawInput = instance.readLine();
            String result = instance.run(rawInput);
            if (Command.CLOSE_KEY_STRING.equals(result)) {
                break;
            }
            if (result != null) {
                instance.writeLine(result);
            }
        }
    }
    
    private static @Nullable String backupReader() {
        try {
            return new String(System.in.readAllBytes());
        } catch (IOException ioe) {
            System.out.println("IO Exception:");
            System.out.print(ioe.fillInStackTrace().getLocalizedMessage());
            return null;
        }
    }
    
    public static void main(
            String[] args
    ) {
        Console console = System.console();
        Interfacer instance;
        if (console != null) {
            PrintWriter writer = console.writer();
            instance = new Interfacer(writer::println, console::readLine);
        } else {
            instance = new Interfacer(System.out::println, Main::backupReader);
        }
        runLoop(instance);
    }
}