package org.cb2384.exactalgebra.text;

import java.io.Console;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.cb2384.exactalgebra.text.parse.Command;
import org.cb2384.exactalgebra.text.parse.Command.ReservedNames;
import org.cb2384.exactalgebra.text.parse.Interfacer;
import org.cb2384.exactalgebra.util.MiscUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    
    private static void runLoop() {
        Console console = System.console();
        boolean run = console != null;
        if (run) {
            try (PrintWriter writer = console.writer()) {
                Interfacer instance = new Interfacer(writer);
                writer.println("Welcome!");
                writer.println("\"{\" and \"}\" signify the beginning and end of an argument list; \"\\\""
                        + " is prepended to the actual command.");
                writer.println("Commands can go in front of the arguments, or inside, though the location"
                        + " inside must be first if it is a unary operation like negation, and otherwise second.");
                writer.println("Arguments should be separated by commas");
                writer.println("To instantiate a number object, wrap the value in parentheses: \"(2.5)\""
                        + " creates a new value with that value, and as an exact rational.");
                writer.println("Parsing of values is handled as if through Java's own BigDecimal class.");
                writer.println("Brackets with comma-separated values like \"[6, 0, -3, 2]\""
                        + " create a polynomial with the corresponding coefficients, in descending order.");
                writer.println("The above example converts to 6x³ -3x + 2.\n");
                writer.println("Operations are named as follows, separated by a pipe (\"|\"):");
                writer.println(Identifier.patternOrCompiler(OpNames.ALL_IDENTIFIERS).pattern());
                writer.println("Utilities are named as follows, separated by a pipe (\"|\"):");
                writer.println(Identifier.patternOrCompiler(Utils.ALL_IDENTIFIERS).pattern());
                writer.println("Other important terms are:");
                writer.println(Identifier.patternOrCompiler(ReservedNames.ALL_IDENTIFIERS).pattern());
                writer.println("As well as \"true\" and \"false\" for boolean values and these for rounding modes:");
                writer.println(Identifier.patternOrCompiler(EnumSet.allOf(RoundingMode.class)
                        .stream()
                        .map(RoundingMode::toString)
                        .map(StringUtils::toCamelCase)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet()) ));
                
                while (run) {
                    String rawInput = console.readLine();
                    String result = instance.run(rawInput);
                    if (Command.CLOSE_KEY_STRING.equals(result)) {
                        run = false;
                    }
                    if (result != null) {
                        writer.println(result);
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) {
        runLoop();
    }
}