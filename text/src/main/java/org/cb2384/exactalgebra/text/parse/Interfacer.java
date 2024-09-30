package org.cb2384.exactalgebra.text.parse;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.AlgebraObject;
import org.cb2384.exactalgebra.objects.relations.polynomial.PolyRat;
import org.cb2384.exactalgebra.text.Identifier;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank;
import org.cb2384.exactalgebra.text.opmanagement.NumberRank;
import org.cb2384.exactalgebra.text.opmanagement.Rank;
import org.cb2384.exactalgebra.text.parse.Command.ExecutionResult;
import org.cb2384.exactalgebra.text.parse.Command.ReservedNames;
import org.cb2384.exactalgebra.util.corutils.StringUtils;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

public final class Interfacer {
    
    private static final SequencedMap<String, SavedAO<?, ?, ?>> SAVED =
            new LinkedHashMap<>(32, 0.75f, true);
    
    private static final @Nullable Path DEFAULT_LOCATION;
    
    static {
        String defaultPath = System.getProperty("user.dir");
        if (defaultPath == null) {
            defaultPath = System.getProperty("user.home");
            if (defaultPath == null) {
                defaultPath = System.getProperty("java.io.tmpdir");
            }
        }
        if (defaultPath != null) {
            DEFAULT_LOCATION = Path.of(defaultPath);
        } else {
            DEFAULT_LOCATION = null;
        }
    }
    
    private @Nullable Path location = DEFAULT_LOCATION;
    
    private final Consumer<String> writer;
    
    private final Supplier<String> reader;
    
    private SavedAO<?, ?, ?> lastResult;
    
    private Command<?, ?> lastCommand;
    
    public Interfacer(
            Consumer<String> writer,
            Supplier<String> reader
    ) {
        this.reader = reader;
        this.writer = writer;
    }
    
    public boolean namePresent(
            String name
    ) {
        return SAVED.containsKey(name);
    }
    
    public @Nullable String run(
            String input
    ) {
        Command<?, ?> head = (lastCommand = new InputLine(input, this).parse());
        Object result = head.get();
        if (ExecutionResult.SUCCESS.equals(result)) {
            return null;
        }
        if (ExecutionResult.CLOSE.equals(result)) {
            return Command.CLOSE_KEY_STRING;
        }
        if (result instanceof AlgebraObject<?> object) {
            lastResult = SavedAO.asSavedAO(object);
            return null;
        }
        return result.toString();
    }
    
    @Pure
    Command<?, ?> getLastCommand() {
        return lastCommand;
    }
    
    @Deterministic
    void add(
            String name,
            SavedAO<?, ?, ?> value
    ) {
        SAVED.put(name, value);
    }
    
    @Pure
    @Nullable SavedAO<?, ?, ?> getLast() {
        return lastResult;
    }
    
    @Pure
    @Nullable SavedAO<?, ?, ?> retrieve(
            String name
    ) {
        return SAVED.get(name);
    }
    
    
    ExecutionResult delete(
            @Nullable String target
    ) {
        if (target == null) {
            SAVED.clear();
            return ExecutionResult.SUCCESS;
        }
        return (SAVED.remove(target) == null)
                ? ExecutionResult.fromMsg("No target by name " + target + " found to delete!")
                : ExecutionResult.SUCCESS;
    }
    
    Supplier<ExecutionResult> getSaver(
            String@ArrayLen({1, 2, 3})[] args,
            String source
    ) {
        boolean overwrite;
        Path path;
        switch (args.length) {
            case 3 -> {
                overwrite = Command.parseBoolPrim(args[2], () -> CommandFormatException.forInputString(source));
                path = parsePath(source, Arrays.copyOf(args, 2));
            }
            case 2 -> {
                Boolean overwriteTry = Command.parseBool(args[1]);
                if (overwriteTry == null) {
                    overwrite = false;
                    path = parsePath(source, new String[]{args[0]});
                } else {
                    overwrite = overwriteTry;
                    path = parsePath(source, args);
                }
            }
            case 1 -> {
                if (location == null) {
                    path = parsePath(source, args);
                    overwrite = false;
                } else {
                    Boolean overwriteTry = Command.parseBool(args[0]);
                    if (overwriteTry == null) {
                        path = parsePath(source, args);
                        overwrite = false;
                    } else {
                        path = location;
                        overwrite = overwriteTry;
                    }
                }
            }
            default -> throw new RuntimeException();
        }
        
        if (!overwrite && path.toFile().exists()) {
            throw new CommandStateException("File already exists and overwriting not allowed!");
        }
        
        return () -> {
            Exception e = save(path);
            return (e == null)
                    ? ExecutionResult.SUCCESS
                    : ExecutionResult.fromMsg(e.getLocalizedMessage());
        };
    }
    
    @SideEffectFree
    Path parsePath(
            String source,
            String@ArrayLen({1, 2})[] args
    ) {
        try {
            return  (args.length == 2)
                    ? Path.of(args[0])
                    : Path.of(args[0], args[1]);
        } catch (InvalidPathException ipe) {
            throw CommandFormatException.forInputStringWithCause(source, ipe);
        }
    }
    
    private @Nullable Exception save(
            Path saveLocation
    ) {
        try {
            FileOutputStream fos = new FileOutputStream(saveLocation.toFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(SAVED);
            oos.flush();
            oos.close();
            location = saveLocation;
            return null;
        } catch (IOException oldIOE) {
            CommandStateException newCSE = new CommandStateException(Command.IO_EXC_MSG);
            return (Exception) newCSE.initCause(oldIOE);
        } catch (Exception oldE) {
            return oldE;
        }
    }
    
    @Nullable Exception load(
            Path location
    ) {
        try {
            FileInputStream fis = new FileInputStream(location.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            if (obj == null) {
                throw new EmptyStreamExc();
            }
            while (obj != null) {
                switch (obj) {
                    case Map<?, ?> map -> map.forEach((k, v) -> {
                        if ((k instanceof String kS) && (v instanceof SavedAO<?, ?, ?> vSAO)) {
                            SAVED.put(kS, vSAO);
                        } else {
                            throw new ClassCastException("Loaded map is of incorrect type!");
                        }
                    });
                    case Entry<?, ?> entry -> {
                        if ((entry.getKey() instanceof String keyS)
                                && (entry.getValue() instanceof SavedAO<?, ?, ?> valueSAO)) {
                            SAVED.put(keyS, valueSAO);
                        } else {
                            throw new ClassCastException("Loaded entry is of incorrect type!");
                        }
                    }
                    default -> throw new ClassCastException("Loaded Object does not appear to be a saved value!");
                }
                obj = ois.readObject();
            }
            
            this.location = location;
            return null;
        } catch (IOException oldIOE) {
            CommandStateException newCSE = new CommandStateException(Command.IO_EXC_MSG);
            return (Exception) newCSE.initCause(oldIOE);
        } catch (Exception oldE) {
            return oldE;
        }
    }
    
    SavedAO<?, ?, ?> directLoad(
            Path location
    ) throws IOException {
         try {
             FileInputStream fis = new FileInputStream(location.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis);
             Object obj = ois.readObject();
             return switch (obj) {
                 case null -> throw new EmptyStreamExc();
                 case Map<?, ?> map -> {
                     if (map.size() == 1) {
                         Entry<?, ?> entry = map.entrySet().iterator().next();
                         if ((entry.getKey() instanceof String)
                                 && (entry.getValue() instanceof SavedAO<?, ?, ?> val)) {
                             yield val;
                         }
                     }
                     throw new ClassCastException("Loaded map is of incorrect type!");
                 }
                 case Entry<?, ?> entry -> {
                     if ((entry.getKey() instanceof String)
                             && (entry.getValue() instanceof SavedAO<?, ?, ?> valueSAO)) {
                         yield valueSAO;
                     }
                     throw new ClassCastException("Loaded entry is of incorrect type!");
                 }
                 case SavedAO<?, ?, ?> savedObject -> savedObject;
                 case AlgebraObject<?> object -> SavedAO.asSavedAO(object);
                 default -> throw new ClassCastException("Loaded Object does not appear to be a saved value!");
             };
         } catch (ClassNotFoundException oldCNFE) {
             CommandStateException newCSE = new CommandStateException(oldCNFE.getLocalizedMessage());
             throw (CommandStateException) newCSE.initCause(oldCNFE);
         }
    }
    
    @Nullable ExecutionResult printExternal(
            AlgebraObject<?> toPrint,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix
    ) {
        print(toPrint.toString(radix));
        return null;
    }
    
    Supplier<ExecutionResult> getPrinter(
            String@ArrayLenRange(from = 1, to = 3)[] args,
            String source
    ) {
        Rank<?, ?> type;
        int radix;
        boolean alphabetize;
        int length = args.length;
        switch (length) {
            case 1:
                String arg = args[0];
                if (SAVED.containsKey(arg)) {
                    return () -> printExternal(SAVED.get(arg), 10);
                }
                if (Command.ReservedNames.ALL.reserves(arg)) {
                    switch ((ReservedNames) Identifier.firstMatchingIdentifier(arg, RuntimeException::new)) {
                        case ALL -> type = null;
                        case INT -> type = NumberRank.INTEGER;
                        case RAT -> type = NumberRank.RATIONAL;
                        case ANS -> {
                            return () -> printExternal(getLast().value(), 10);
                        }
                        default -> type = FunctionRank.rankOf(PolyRat.ONE);
                    }
                    alphabetize = false;
                    radix = 10;
                } else if (StringUtils.isDigitString(arg)) {
                    radix = Command.getRadix(arg, () -> CommandFormatException.forInputString(source));
                    type = null;
                    alphabetize = false;
                } else {
                    type = null;
                    alphabetize = Command.parseBoolPrim(arg, () -> CommandFormatException.forInputString(source));
                    radix = 10;
                }
                break;
                
            case 2:
                String firstArg = args[0];
                String secondArg = args[0];
                if (SAVED.containsKey(firstArg)) {
                    radix = Command.getRadix(secondArg, () -> CommandFormatException.forInputString(source));
                    return () -> printExternal(SAVED.get(firstArg), radix);
                }
                if (SAVED.containsKey(secondArg)) {
                    radix = Command.getRadix(firstArg, () -> CommandFormatException.forInputString(source));
                    return () -> printExternal(SAVED.get(secondArg), radix);
                }
                if (Command.ReservedNames.ALL.reserves(firstArg)) {
                    switch ((ReservedNames) Identifier.firstMatchingIdentifier(firstArg, RuntimeException::new)) {
                        case ALL -> type = null;
                        case INT -> type = NumberRank.INTEGER;
                        case RAT -> type = NumberRank.RATIONAL;
                        case ANS -> {
                            radix = Command.getRadix(secondArg, () -> CommandFormatException.forInputString(source));
                            return () -> printExternal(getLast().value(), radix);
                        }
                        case POLY -> type = FunctionRank.rankOf(PolyRat.ONE);
                        default -> throw new RuntimeException();
                    }
                    Boolean tryAlpha = Command.parseBool(secondArg);
                    if (tryAlpha == null) {
                        radix = Command.getRadix(secondArg, () -> CommandFormatException.forInputString(source));
                        alphabetize = false;
                    } else {
                        radix = 10;
                        alphabetize = tryAlpha;
                    }
                } else if (Command.ReservedNames.ALL.reserves(secondArg)) {
                    switch ((ReservedNames) Identifier.firstMatchingIdentifier(secondArg, RuntimeException::new)) {
                        case ALL -> type = null;
                        case INT -> type = NumberRank.INTEGER;
                        case RAT -> type = NumberRank.RATIONAL;
                        case ANS -> {
                            radix = Command.getRadix(firstArg, () -> CommandFormatException.forInputString(source));
                            return () -> printExternal(getLast().value(), radix);
                        }
                        case POLY -> type = FunctionRank.rankOf(PolyRat.ONE);
                        default -> throw new RuntimeException();
                    }
                    Boolean tryAlpha = Command.parseBool(firstArg);
                    if (tryAlpha == null) {
                        radix = Command.getRadix(firstArg, () -> CommandFormatException.forInputString(source));
                        alphabetize = false;
                    } else {
                        radix = 10;
                        alphabetize = tryAlpha;
                    }
                } else {
                    type = null;
                    Boolean tryAlpha = Command.parseBool(firstArg);
                    if (tryAlpha == null) {
                        radix = Command.getRadix(firstArg, () -> CommandFormatException.forInputString(source));
                        alphabetize = Command.parseBoolPrim(secondArg,
                                () -> CommandFormatException.forInputString(source) );
                    } else {
                        radix = Command.getRadix(secondArg, () -> CommandFormatException.forInputString(source));
                        alphabetize = tryAlpha;
                    }
                }
                break;
                
            case 3, 4:
                int i;
                for (i = 0; i < length; i++) {
                    String argS = args[i];
                    if (Command.ReservedNames.ALL.reserves(argS)) {
                        break;
                    }
                }
                if (i != length) {
                    int descriptorIndex = i;
                    for (i = 0; i < length; i++) {
                        if (i != descriptorIndex) {
                            Boolean tryAlpha = Command.parseBool(args[i]);
                            if (tryAlpha != null) {
                                break;
                            }
                        }
                    }
                    if (i != length) {
                        int alphaIndex = i;
                        for (i = 0; i < length; i++) {
                            if ((i != descriptorIndex) && (i != alphaIndex) && StringUtils.isDigitString(args[i])) {
                                break;
                            }
                        }
                        if (i != length) {
                            radix = Command.getRadix(args[i], () -> CommandFormatException.forInputString(source));
                            alphabetize = Command.parseBool(args[alphaIndex]);
                            String descS = args[descriptorIndex];
                            switch ((ReservedNames) Identifier.firstMatchingIdentifier(descS,
                                    RuntimeException::new)) {
                                case ALL -> type = null;
                                case INT -> type = NumberRank.INTEGER;
                                case RAT -> type = NumberRank.RATIONAL;
                                case ANS -> {
                                    return () -> printExternal(getLast().value(), radix);
                                }
                                case POLY -> type = FunctionRank.rankOf(PolyRat.ONE);
                                default -> throw new RuntimeException();
                            }
                            break;
                        }
                    }
                }
            // fall through if fail
            default:
                throw CommandFormatException.forInputString(source);
        }
        return () -> printInternal(type, radix, alphabetize);
    }
    
    @Nullable ExecutionResult printInternal(
            @Nullable Rank<?, ?> type,
            @IntRange(from = Character.MIN_RADIX, to = Character.MAX_RADIX) int radix,
            boolean alphabetize
    ) {
        int tabSize = 4;
        int maxName = SAVED.keySet()
                .parallelStream()
                .mapToInt(String::length)
                .reduce(tabSize, Integer::max);
        
        int separate = ((maxName / tabSize) + 1) * tabSize;
        System.out.println("Identifier:" + " ".repeat(separate - 5) + "Value:");
        
        Stream<Entry<String, SavedAO<?, ?, ?>>> entryStream = SAVED.sequencedEntrySet().stream();
        
        if (type != null) {
            entryStream = entryStream.filter(e -> type.equals(e.getValue().type()));
        }
        
        if (alphabetize) {
            entryStream = entryStream.sorted(Entry.comparingByKey());
        }
        try {
            entryStream.forEachOrdered(e -> print(e.getValue().print(e.getKey(), separate, radix)));
        } catch (RuntimeException exc) {
            return ExecutionResult.fromMsg(exc.getLocalizedMessage());
        }
        return ExecutionResult.SUCCESS;
    }
    
    private void print(
            String toPrint
    ) {
        try {
            writer.accept(toPrint);
        } catch (Exception suppressed) {
            throw new RuntimeException(suppressed);
        }
    }
    
    /**
     * Factory method for making a {@code NumberFormatException}
     * given the specified input which caused the error.
     */
    CommandStateException forLastState() {
        return new CommandStateException("Command: \"" + lastResult + "\""
                + "is not formatted properly");
    }
    
    private static final class EmptyStreamExc extends ObjectStreamException {
        EmptyStreamExc() {
            super("Loaded stream is empty/corrupted" + (char) 0x203D);
        }
    }
    
    public void writeLine(
            String line
    ) {
        writer.accept(line);
    }
    
    public String readLine() {
        return reader.get();
    }
}
