package org.cb2384.exactalgebra.text.parse;

import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.cb2384.exactalgebra.objects.numbers.AlgebraNumber;
import org.cb2384.exactalgebra.objects.numbers.integral.AlgebraInteger;
import org.cb2384.exactalgebra.objects.relations.polynomial.Polynomial;
import org.cb2384.exactalgebra.text.Identifier;
import org.cb2384.exactalgebra.text.ReservedSymbols;
import org.cb2384.exactalgebra.text.Utils;
import org.cb2384.exactalgebra.text.opmanagement.AlgebraOp;
import org.cb2384.exactalgebra.text.opmanagement.FunctionRank;
import org.cb2384.exactalgebra.text.opmanagement.NumberRank;
import org.cb2384.exactalgebra.text.OpNames;
import org.cb2384.exactalgebra.text.opmanagement.OpFlag;
import org.cb2384.exactalgebra.text.opmanagement.PolynomialOps;
import org.cb2384.exactalgebra.text.opmanagement.RealFieldOps;
import org.cb2384.exactalgebra.text.parse.Command.ReservedNames;
import org.cb2384.exactalgebra.text.parse.CreationCommand.FunctionCreationCommand;
import org.cb2384.exactalgebra.text.parse.IndexWithDepth.RangeWithDepth;
import org.cb2384.exactalgebra.text.parse.OperativeCommand.BinaryPrimCommand;
import org.cb2384.exactalgebra.text.parse.OperativeCommand.BinaryStandardCommand;
import org.cb2384.exactalgebra.util.corutils.NullnessUtils;
import org.cb2384.exactalgebra.util.corutils.StringUtils;
import org.cb2384.exactalgebra.util.corutils.ternary.Signum;

import org.checkerframework.checker.nullness.qual.*;
import org.checkerframework.common.value.qual.*;
import org.checkerframework.dataflow.qual.*;

/**
 * <p>A processed line from user input; it contains a tree of the commands, ready to be run recursively from the out
 * in using {@link #parse()} to get the first or 'head' command</p>
 *
 * @author Corinne Buxton
 */
public final class InputLine {
    
    private static final Set<OpNames> NON_REV_UNARY = Set.of(OpNames.NEGATED);
    
    private static final Set<OpNames> UNARY_TRUNCATORS = Set.of(
            OpNames.SQRT_WITH_REMAINDER, OpNames.LN_WITH_REMAINDER, OpNames.EXP_WITH_REMAINDER,
            OpNames.SQRT_REMAINDER, OpNames.LN_REMAINDER, OpNames.EXP_REMAINDER
    );
    
    private static final Set<OpNames> BINARY_TRUNCATORS = Set.of(
            OpNames.ROOT_WITH_REMAINDER, OpNames.POWER_WITH_REMAINDER,
            OpNames.LOG_BASE_WITH_REMAINDER, OpNames.ROOT_REMAINDER,
            OpNames.POWER_REMAINDER, OpNames.LOG_BASE_REMAINDER, OpNames.QUOTIENT_WITH_REMAINDER
    );
    
    private static final Set<OpNames> UNARY_AUX_ROUNDERS = Set.of(
            OpNames.SQRT, OpNames.EXP, OpNames.LN
    );
    
    private static final Set<OpNames> BINARY_AUX_ROUNDERS = Set.of(
            OpNames.QUOTIENT, OpNames.ROOT, OpNames.LOG_BASE, OpNames.POWER
    );
    
    private static final Set<ReservedSymbols> NON_COMMAND_IDENTIFIERS = Set.of(
            ReservedSymbols.SPACE, ReservedSymbols.ARG_SEP
    );
    
    private final Interfacer interfaceInstance;
    
    private final String preProcessInput;
    
    private final LineTree postProcessInput;
    
    /**
     * Builds the input line from the actual input line. The second argument is more akin to
     * the {@link Interfacer being an enclosing class}. However, the resulting class file would
     * be much too big, so it is captured as a constructor parameter instead.
     *
     * @param input             the actual input line
     * @param interfaceInstance think of this as being the enclosing class instance if this class were a nested
     *                          class in {@link Interfacer}
     *
     * @throws CommandFormatException   if {@code input} was not properly formatted
     */
    @SideEffectFree
    public InputLine(
            String input,
            Interfacer interfaceInstance
    ) {
        preProcessInput = input;
        this.interfaceInstance = interfaceInstance;
        postProcessInput = new LineTree(input.trim());
    }
    
    /**
     * At construction, a tree of strings is created, with hierarchical depths. This parses each
     * 'node' into a being a {@link Command}, and returns the head node.
     *
     * @return  the node outermost or surface-depth command, the one that all other commands from this line
     * are called by (or called by one that is called by this, etc...)
     *
     * @throws CommandFormatException   if the original input was not properly formatted, but in a way that
     *                                  escaped the validation during construction
     */
    @SideEffectFree
    public Command<?, ?> parse() {
        postProcessInput.forEach((k, v) -> System.out.println(k + ", " + v));
        return recursiveParse(postProcessInput.firstKey());
    }
    
    @SideEffectFree
    private Command<?, ?> recursiveParse(
            IndexWithDepth commandStart
    ) {
        return switch (commandStart.symbolType()) {
            case COMMAND_KEY -> fromNonArgCommand(commandStart);
            case OBJECT_GROUP -> {
                IndexWithDepth numberKey = postProcessInput.higherKey(commandStart);
                String local = postProcessInput.get(commandStart);
                if (numberKey.symbolType() != ReservedSymbols.SPACE) {
                    throw excFact(local);
                }
                String number = postProcessInput.get(numberKey);
                yield CreationCommand.numberCreationCommand(local, interfaceInstance,
                        number, null, null);
            }
            case LIST_GROUP -> {
                Stream<String> coefficients = postProcessInput.valuesSequentialFilteredByDepth(
                        commandStart,
                        false,
                        IndexWithDepth.dummyIndex( ((RangeWithDepth) commandStart).endExclusive() ),
                        false,
                        commandStart.depth() + 1,
                        Signum.POSITIVE,
                        Boolean.FALSE
                );
                yield new FunctionCreationCommand<>(postProcessInput.get(commandStart), coefficients);
            }
            case ARG_GROUP -> {
                RangeWithDepth rangeStart = (RangeWithDepth) commandStart;
                int currentDepth = rangeStart.depth();
                
                Predicate<? super Entry<? extends IndexWithDepth, String>> criterion = e -> {
                    IndexWithDepth key = e.getKey();
                    return (key.symbolType() == ReservedSymbols.COMMAND_KEY) && (key.depth() == currentDepth);
                };
                LineTree subTree = postProcessInput.filterBy(criterion, rangeStart, Boolean.FALSE,
                        postProcessInput.ceilingKey(rangeStart.endExclusive()), null);
                IndexWithDepth actualCommandStart = subTree.keySet()
                        .stream()
                        .findAny()
                        .orElseThrow(this::excFact);
                boolean commandInsideArgs = actualCommandStart.depth() > rangeStart.depth();
                yield fromArg(actualCommandStart, rangeStart, commandInsideArgs);
            }
            default -> throw excFact(postProcessInput.get(commandStart));
        };
    }
    
    @SideEffectFree
    private Command<?, ?> fromNonArgCommand(
            IndexWithDepth commandStart
    ) {
        String commandString = postProcessInput.get(commandStart);
        Identifier<?, ?> id = Identifier.firstMatchingIdentifier(commandString, this::excFact);
        if (id == Utils.PRINT) {
            return new UtilCommand.PrintRetrievedCommand(commandString, interfaceInstance);
        }
        if (id == Utils.EXIT) {
            return new UtilCommand.Close(commandString, interfaceInstance);
        }
        throw excFact();
    }
    
    @SideEffectFree
    private Command<?, ?> fromArg(
            IndexWithDepth commandStart,
            RangeWithDepth rangeStart,
            boolean commandInsideArgs
    ) {
        IndexWithDepth rangeEnd = postProcessInput.lowerKey(rangeStart.endExclusive());
        
        int targetDepth = rangeStart.depth() + 1;
        Predicate<? super Entry<? extends IndexWithDepth, String>> criterion = e -> {
            IndexWithDepth key = e.getKey();
            return (key.symbolType() != ReservedSymbols.COMMAND_KEY) && (key.depth() == targetDepth);
        };
        
        int argCount = postProcessInput.countBy(rangeStart, true,
                rangeEnd, true, criterion);
        
        String commandString = postProcessInput.get(commandStart);
        String source = postProcessInput.get(rangeStart);
        
        IndexWithDepth firstIndex = postProcessInput.higherKey(rangeStart);
        IndexWithDepth firstArgIndex = (firstIndex == commandStart)
                ? postProcessInput.higherKey(firstIndex)
                : firstIndex;
        
        return switch (argCount) {
            case 1 -> switch (Identifier.firstMatchingIdentifier(commandString, this::excFact)) {
                case OpNames commandName -> op1Arg(firstIndex, firstArgIndex, source, commandName);
                case Utils utilName -> util1Arg(firstIndex, firstArgIndex, source, utilName);
                default -> throw excFact(commandString);
            };
            case 2 -> {
                IndexWithDepth secondArgIndex = postProcessInput.lowerKey(rangeEnd);
                
                yield switch (Identifier.firstMatchingIdentifier(commandString, this::excFact)) {
                    case OpNames commandName -> op2Args(firstArgIndex,
                            secondArgIndex, source, commandName);
                    case Utils utilName -> util2Args(firstIndex, firstArgIndex, secondArgIndex, source, utilName);
                    default -> throw excFact(commandString);
                };
            }
            case 3 -> {
                IndexWithDepth thirdArgIndex = postProcessInput.lowerKey(rangeEnd);
                IndexWithDepth secondArgIndex = postProcessInput.lowerKey(thirdArgIndex);
                
                yield switch (Identifier.firstMatchingIdentifier(commandString, this::excFact)) {
                    case OpNames commandName -> op3Args(firstArgIndex, secondArgIndex,
                            thirdArgIndex, source, commandName);
                    case Utils utilName -> util3Args(firstArgIndex, secondArgIndex,
                            thirdArgIndex, source, utilName);
                    default -> throw excFact(commandString);
                };
            }
            case 4 -> {
                IndexWithDepth fourthArgIndex = postProcessInput.lowerKey(rangeEnd);
                IndexWithDepth thirdArgIndex = postProcessInput.lowerKey(fourthArgIndex);
                IndexWithDepth secondArgIndex = postProcessInput.lowerKey(thirdArgIndex);
                
                yield switch (Identifier.firstMatchingIdentifier(commandString, this::excFact)) {
                    case OpNames commandName -> op4Args(firstArgIndex, secondArgIndex,
                            thirdArgIndex, fourthArgIndex, source, commandName);
                    case Utils utilName -> {
                        boolean save = utilName == Utils.SAVE;
                        if (save || (utilName == Utils.WRITE)) {
                            yield writeOrSaveCommand(save, source, firstArgIndex,
                                    secondArgIndex, thirdArgIndex, fourthArgIndex);
                        }
                        throw excFact(commandString);
                    }
                    default -> throw excFact(commandString);
                };
            }
            case 5 -> {
                IndexWithDepth[] args = postProcessInput.descendingKeySet()
                        .tailSet(rangeEnd, false)
                        .stream()
                        .limit(5)
                        .toArray(IndexWithDepth[]::new);
                Identifier<?, ?> id = Identifier.firstMatchingIdentifier(commandString, this::excFact);
                if (id == Utils.SAVE) {
                    yield writeOrSaveCommand(true, commandString, args);
                } else if (id instanceof OpNames op) {
                    yield roundAuxOperation(source, firstArgIndex, op, args);
                }
                throw excFact(commandString);
            }
            default -> throw excFact();
        };
    }
    
    @SideEffectFree
    private Command<?, ?> util1Arg(
            IndexWithDepth beginIndex,
            IndexWithDepth argIndex,
            String source,
            Utils utilName
    ) {
        String argString = postProcessInput.get(argIndex);
        return switch (utilName) {
            case CREATE -> CreationCommand.numberCreationCommand(source, interfaceInstance, argString,
                    null, null);
            case PRINT -> {
                ReservedSymbols nextArgType = argIndex.symbolType();
                if ((nextArgType == ReservedSymbols.SPACE) || (nextArgType == ReservedSymbols.ARG_SEP)) {
                    yield new UtilCommand.PrintRetrievedCommand(source, interfaceInstance, argString);
                }
                yield new UtilCommand.PrintCommand(source, 10,
                        recursiveParse(argIndex), interfaceInstance);
            }
            case WRITE -> new UtilCommand.WriteCommand(source, argString,
                    null, null, interfaceInstance);
            case SAVE -> new UtilCommand.SaveCommand(source, interfaceInstance, argString);
            case LOAD -> loadCommand(source, beginIndex, argString);
            case GET -> new UtilCommand.RetrieveCommand<>(source, argString, interfaceInstance);
            case DELETE -> new UtilCommand.DeletionCommand(source, argString, interfaceInstance);
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private Command<?, ?> util2Args(
            IndexWithDepth beginIndex,
            IndexWithDepth firstArgIndex,
            IndexWithDepth secondArgIndex,
            String source,
            Utils utilName
    ) {
        String firstArgString = postProcessInput.get(firstArgIndex);
        String secondArdString = postProcessInput.get(secondArgIndex);
        return switch (utilName) {
            case CREATE -> {
                String name;
                Boolean secondArgBool = Command.parseBool(secondArdString);
                if (secondArgBool == null) {
                    name = secondArdString;
                } else {
                    name = null;
                }
                yield CreationCommand.numberCreationCommand(source, interfaceInstance,
                        firstArgString, name, secondArgBool);
            }
            case PRINT -> {
                ReservedSymbols nextArgType = firstArgIndex.symbolType();
                if ((nextArgType == ReservedSymbols.SPACE) || (nextArgType == ReservedSymbols.ARG_SEP)) {
                    yield new UtilCommand.PrintRetrievedCommand(source, interfaceInstance,
                            firstArgString, secondArdString);
                }
                yield new UtilCommand.PrintCommand(source, parseInt(secondArdString, source),
                        recursiveParse(firstArgIndex), interfaceInstance);
            }
            case WRITE -> writeOrSaveCommand(false, source, firstArgIndex, secondArgIndex);
            case SAVE -> writeOrSaveCommand(true, source, firstArgIndex, secondArgIndex);
            case LOAD -> loadCommand(source, beginIndex, firstArgString, secondArdString);
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private Command<?, ?> util3Args(
            IndexWithDepth firstArgIndex,
            IndexWithDepth secondArgIndex,
            IndexWithDepth thirdArgIndex,
            String source,
            Utils utilName
    ) {
        return switch (utilName) {
            case PRINT -> new UtilCommand.PrintRetrievedCommand(
                    source,
                    interfaceInstance,
                    postProcessInput.get(firstArgIndex),
                    postProcessInput.get(secondArgIndex),
                    postProcessInput.get(thirdArgIndex)
            );
            case WRITE -> writeOrSaveCommand(false, source, firstArgIndex, secondArgIndex, thirdArgIndex);
            case SAVE -> writeOrSaveCommand(true, source, firstArgIndex, secondArgIndex, thirdArgIndex);
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> op1Arg(
            IndexWithDepth firstIndex,
            IndexWithDepth argIndex,
            String source,
            OpNames command
    ) {
        if (NON_REV_UNARY.contains(command) && (argIndex != firstIndex)) {
            throw excFact();
        }
        return switch (command) {
            case NEGATED -> unaryOperation(source, argIndex, RealFieldOps.NEGATED, PolynomialOps.NEGATED);
            case MAGNITUDE -> unaryOperation(source, argIndex, RealFieldOps.MAGNITUDE, PolynomialOps.ABSOLUTE_VALUE);
            
            case INVERTED -> unaryOperation(source, argIndex, RealFieldOps.INVERTED, null);
            case SQUARED -> unaryOperation(source, argIndex, RealFieldOps.SQUARED, PolynomialOps.SQUARED);
            
            case SQRT_WITH_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.SQRT_Z_WITH_REMAINDER, null);
            case SQRT -> unaryOperation(source, argIndex, RealFieldOps.SQRT, null);
            case SQRT_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.SQRT_Z_REMAINDER, null);
            
            case EXP_WITH_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.EXP_Z_WITH_REMAINDER, null);
            case EXP -> unaryOperation(source, argIndex, RealFieldOps.EXP, null);
            case EXP_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.EXP_Z_REMAINDER, null);
            
            case LN_WITH_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.LN_Z_WITH_REMAINDER, null);
            case LN -> unaryOperation(source, argIndex, RealFieldOps.LN, null);
            case LN_REMAINDER -> unaryOperation(source, argIndex,
                    RealFieldOps.LN_Z_REMAINDER, null);
            
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> op2Args(
            IndexWithDepth firstArgIndex,
            IndexWithDepth secondArgIndex,
            String source,
            OpNames command
    ) {
        if (UNARY_TRUNCATORS.contains(command)) {
            return truncationOp(source, firstArgIndex, command, null, secondArgIndex, null);
        }
        if (UNARY_AUX_ROUNDERS.contains(command)) {
            return roundAuxOperation(source, firstArgIndex, command, secondArgIndex);
        }
        return switch (command) {
            case SUM -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.SUM, PolynomialOps.SUM);
            case DIFFERENCE -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.DIFFERENCE, PolynomialOps.DIFFERENCE);
            case PRODUCT -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.PRODUCT, PolynomialOps.PRODUCT);
            case QUOTIENT -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.QUOTIENT, null);
            
            case REMAINDER -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.REMAINDER, PolynomialOps.REMAINDER);
            case MAX -> minMaxOperation(source, firstArgIndex, secondArgIndex, true);
            case MIN -> minMaxOperation(source, firstArgIndex, secondArgIndex, false);
            case QUOTIENT_WITH_REMAINDER -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.QUOTIENT_Z_WITH_REMAINDER, PolynomialOps.QUOTIENT_Z_WITH_REMAINDER);
            
            case ROOT -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.ROOT, null);
            case POWER -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.POWER, null);
            case LOG_BASE -> standardBinaryOperation(source, firstArgIndex, secondArgIndex,
                    RealFieldOps.LOG_BASE, null);
            
            case ROUND -> strictRoundOperation(source, firstArgIndex, secondArgIndex,
                    null, null);
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> op3Args(
            IndexWithDepth firstArgIndex,
            IndexWithDepth secondArgIndex,
            IndexWithDepth thirdArgIndex,
            String source,
            OpNames command
    ) {
        if (BINARY_TRUNCATORS.contains(command) || UNARY_TRUNCATORS.contains(command)) {
            return truncationOp(source, firstArgIndex, command, secondArgIndex, thirdArgIndex, null);
        }
        if (BINARY_AUX_ROUNDERS.contains(command) || UNARY_AUX_ROUNDERS.contains(command)) {
            return roundAuxOperation(source, firstArgIndex, command, secondArgIndex, thirdArgIndex);
        }
        throw excFact(source);
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> op4Args(
            IndexWithDepth firstArgIndex,
            IndexWithDepth secondArgIndex,
            IndexWithDepth thirdArgIndex,
            IndexWithDepth fourthArgIndex,
            String source,
            OpNames command
    ) {
        if (BINARY_TRUNCATORS.contains(command)) {
            return truncationOp(source, firstArgIndex, command, secondArgIndex, thirdArgIndex, fourthArgIndex);
        }
        if (BINARY_AUX_ROUNDERS.contains(command) || UNARY_AUX_ROUNDERS.contains(command)) {
            return roundAuxOperation(source, firstArgIndex, command, secondArgIndex, thirdArgIndex, fourthArgIndex);
        }
        throw excFact(source);
    }
    
    @SideEffectFree
    private UtilCommand<?, ?> writeOrSaveCommand(
            boolean save,
            String source,
            IndexWithDepth@ArrayLenRange(from = 2, to = 5)... argIndices
    ) {
        int length = argIndices.length;
        ReservedSymbols[] indexTypes = new ReservedSymbols[length];
        String[] indexStrings = new String[length];
        for (int i = 0; i < length; i++) {
            IndexWithDepth index = argIndices[i];
            ReservedSymbols type = index.symbolType();
            indexTypes[i] = type;
            indexStrings[i] = postProcessInput.get(index);
        }
        
        return switch (length) {
            case 2 -> {
                String name;
                Boolean overwrite;
                Command<?, ?> toWrite;
                if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[0])) {
                    if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[1])) {
                        if (save) {
                            yield new UtilCommand.SaveCommand(source, interfaceInstance, indexStrings);
                        }
                        toWrite = null;
                        overwrite = Command.parseBool(indexStrings[0]);
                        if (overwrite == null) {
                            overwrite = Command.parseBoolPrim(indexStrings[1], () -> excFact(source));
                            name = indexStrings[0];
                        } else {
                            name = indexStrings[1];
                        }
                    } else {
                        toWrite = recursiveParse(argIndices[1]);
                        name = indexStrings[0];
                        overwrite = null;
                    }
                } else {
                    toWrite = recursiveParse(argIndices[0]);
                    name = indexStrings[1];
                    overwrite = null;
                }
                
                if (save) {
                    throw excFact(source);
                }
                yield new UtilCommand.WriteCommand(source, name, toWrite, overwrite, interfaceInstance);
            }
            case 3 -> {
                String name;
                Command<?, ?> toWrite;
                String thirdArg;
                if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[0])) {
                    if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[1])) {
                        if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[2])) {
                            if (save) {
                                yield new UtilCommand.SaveCommand(source, interfaceInstance, indexStrings);
                            }
                            throw excFact(source);
                        } else {
                            toWrite = recursiveParse(argIndices[2]);
                            thirdArg = indexStrings[1];
                        }
                    } else {
                        toWrite = recursiveParse(argIndices[1]);
                        thirdArg = indexStrings[2];
                    }
                    name = indexStrings[0];
                } else {
                    toWrite = recursiveParse(argIndices[0]);
                    name = indexStrings[1];
                    thirdArg = indexStrings[2];
                }
                if (save) {
                    yield new UtilCommand.SaveWriteCommand(source, name, toWrite,
                            interfaceInstance, thirdArg);
                }
                yield new UtilCommand.WriteCommand(source, name, toWrite,
                        Command.parseBoolPrim(thirdArg, () -> excFact(source)), interfaceInstance);
            }
            case 4, 5 -> {
                if (!save) {
                    throw excFact();
                }
                int receiverIndex = 0;
                for (; receiverIndex < length; receiverIndex++) {
                    if (NON_COMMAND_IDENTIFIERS.contains(indexTypes[receiverIndex])) {
                        break;
                    }
                }
                if (receiverIndex == length) {
                    throw excFact(source);
                }
                Command<?, ?> toWrite = recursiveParse(argIndices[receiverIndex]);
                String name = indexStrings[(receiverIndex == 0) ? 1 : 0];
                
                String[] argsNoName = new String[length - 2];
                switch (receiverIndex) {
                    case 0, 1 -> System.arraycopy(indexStrings, 2, argsNoName, 0, length - 2);
                    case 2 -> {
                        argsNoName[0] = indexStrings[1];
                        System.arraycopy(indexStrings, 3, argsNoName, 1, length - 3);
                    }
                    case 3 -> {
                        argsNoName[0] = indexStrings[1];
                        argsNoName[1] = indexStrings[2];
                        if (length == 5) {
                            argsNoName[2] = indexStrings[4];
                        }
                    }
                    case 4 -> System.arraycopy(indexStrings, 1, argsNoName, 0, 3);
                }
                
                yield new UtilCommand.SaveWriteCommand(source, name, toWrite, interfaceInstance, argsNoName);
            }
            default -> throw excFact();
        };
    }
    
    @SideEffectFree
    private UtilCommand<?, ?> loadCommand(
            String source,
            IndexWithDepth beginIndex,
            String@ArrayLen({1, 2})... args
    ) {
        return (beginIndex == postProcessInput.firstKey())
                ? new UtilCommand.LoadCommand(source, interfaceInstance, args)
                : new UtilCommand.LoadAndRetrieveCommand<>(source, interfaceInstance, args);
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> unaryOperation(
            String commandString,
            IndexWithDepth receiverIndex,
            @Nullable AlgebraOp<AlgebraNumber, NumberRank> numericOp,
            @Nullable AlgebraOp<Polynomial<?>, FunctionRank> functionOp
    ) {
        assert (numericOp != null) || (functionOp != null);
        Command<?, ?> receiver = recursiveParse(receiverIndex);
        
        return switch (receiver.getResultRank()) {
            case NumberRank ignored -> new OperativeCommand.UnaryCommand<>(commandString,
                    numericOp, (Command<?, AlgebraNumber>) receiver);
            case FunctionRank ignored -> new OperativeCommand.UnaryCommand<>(commandString,
                    functionOp, (Command<?, Polynomial<?>>) receiver);
            default -> throw excFact(commandString);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> standardBinaryOperation(
            String commandString,
            IndexWithDepth receiverIndex,
            IndexWithDepth secondArgIndex,
            @Nullable AlgebraOp<AlgebraNumber, NumberRank> numericOp,
            @Nullable AlgebraOp<Polynomial<?>, FunctionRank> functionOp
    ) {
        assert (numericOp != null) || (functionOp != null);
        Command<?, ?> receiver = recursiveParse(receiverIndex);
        Command<?, ?> secondary = recursiveParse(secondArgIndex);
        
        return switch (receiver.getResultRank()) {
            case NumberRank ignored -> new OperativeCommand.BinaryStandardCommand<>(commandString, numericOp,
                    (Command<?, AlgebraNumber>) receiver, (Command<?, AlgebraNumber>) secondary);
            case FunctionRank ignored -> {
                if (secondary.getResultRank() instanceof NumberRank) {
                    yield new OperativeCommand.BinaryFunctionAndNumberCommand<>(commandString, functionOp,
                            (Command<?, Polynomial<?>>) receiver, (Command<?, AlgebraNumber>) secondary);
                }
                yield new OperativeCommand.BinaryStandardCommand<>(commandString, functionOp,
                        (Command<?, Polynomial<?>>) receiver, (Command<?, Polynomial<?>>) secondary);
            }
            default -> throw excFact(commandString);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> intOptionBinaryOperation(
            String commandString,
            IndexWithDepth receiverIndex,
            IndexWithDepth secondArgIndex,
            @Nullable AlgebraOp<AlgebraNumber, NumberRank> numericOp,
            @Nullable AlgebraOp<Polynomial<?>, FunctionRank> functionOp
    ) {
        assert (numericOp != null) || (functionOp != null);
        Command<?, ?> receiver = recursiveParse(receiverIndex);
        String secondArgString = postProcessInput.get(secondArgIndex);
        Command<?, ?> secondArg;
        if (StringUtils.isDigitString(secondArgString)) {
            if (secondArgString.length() < BinaryPrimCommand.MAX_INT_STRING_LENGTH) {
                Integer intArg = Integer.valueOf(secondArgString);
                return switch (receiver.getResultRank()) {
                    case NumberRank ignored -> new BinaryPrimCommand<>(commandString, numericOp,
                            (Command<?, AlgebraNumber>) receiver, intArg, int.class);
                    case FunctionRank ignored -> new BinaryPrimCommand<>(commandString, functionOp,
                            (Command<?, Polynomial<?>>) receiver, intArg, int.class);
                    default -> throw excFact();
                };
            }
            secondArg = CreationCommand.numberCreationCommand(commandString, interfaceInstance,
                    secondArgString, null, null);
        } else {
            secondArg = recursiveParse(secondArgIndex);
        }
        
        return switch (receiver.getResultRank()) {
            case NumberRank ignored -> new BinaryStandardCommand<>(commandString, numericOp,
                    (Command<?, AlgebraNumber>) receiver, (Command<?, AlgebraNumber>) secondArg);
            case FunctionRank ignored -> new OperativeCommand.BinaryFunctionAndNumberCommand<>(commandString,
                    functionOp, (Command<?, Polynomial<?>>) receiver, (Command<?, AlgebraNumber>) secondArg);
            default -> throw excFact(commandString);
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> minMaxOperation(
            String commandString,
            IndexWithDepth fistArgIndex,
            IndexWithDepth secondArgIndex,
            boolean useMax
    ) {
        Command<?, ?> receiver;
        String secondString;
        String firstArgString = postProcessInput.get(fistArgIndex);
        String secondArgString = postProcessInput.get(secondArgIndex);
        if (StringUtils.isDigitString(firstArgString)) {
            if (StringUtils.isDigitString(secondArgString)) {
                throw excFact(commandString);
            }
            receiver = recursiveParse(secondArgIndex);
            secondString = firstArgString;
        } else if (StringUtils.isDigitString(secondArgString)) {
            receiver = recursiveParse(fistArgIndex);
            secondString = secondArgString;
        } else {
            return standardBinaryOperation(
                    commandString,
                    fistArgIndex,
                    secondArgIndex,
                    useMax ? RealFieldOps.MAX : RealFieldOps.MIN,
                    useMax ? PolynomialOps.MAX : PolynomialOps.MIN
            );
        }
        
        return switch (receiver.getResultRank()) {
            case NumberRank resultRank -> {
                AlgebraOp<AlgebraNumber, NumberRank> op = useMax ? RealFieldOps.MAX : RealFieldOps.MIN;
                if (resultRank.compareTo(NumberRank.INTEGER) <= 0) {
                    Number secondArgLongOrBI;
                    Class<?> secondArgClass;
                    if (secondArgString.length() < BinaryPrimCommand.MAX_LONG_STRING_LENGTH) {
                        secondArgLongOrBI = Long.valueOf(secondString);
                        secondArgClass = long.class;
                    } else {
                        secondArgLongOrBI = new BigInteger(secondString);
                        secondArgClass = BigInteger.class;
                    }
                    
                    yield new BinaryPrimCommand<>(
                            commandString,
                            op,
                            (Command<? extends AlgebraInteger, AlgebraNumber>) receiver,
                            secondArgLongOrBI,
                            secondArgClass
                    );
                }
                yield new OperativeCommand.BinaryStandardCommand<>(
                        commandString,
                        op,
                        (Command<?, AlgebraNumber>) receiver,
                        (Command<?, AlgebraNumber>) CreationCommand.numberCreationCommand(commandString,
                                interfaceInstance, secondString, null, null)
                );
            }
            case FunctionRank ignored -> new OperativeCommand.BinaryStandardCommand<>(
                    commandString,
                    useMax ? PolynomialOps.MAX : PolynomialOps.MIN,
                    (Command<?, Polynomial<?>>) receiver,
                    new FunctionCreationCommand<>(secondString, Stream.of(secondArgString))
            );
            default -> throw excFact();
        };
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> strictRoundOperation(
            String commandString,
            IndexWithDepth receiverIndex,
            IndexWithDepth roundTypeIndex,
            @Nullable IndexWithDepth secondArgIndex,
            @Nullable IndexWithDepth thirdArgIndex
    ) {
        assert (secondArgIndex != null) || (thirdArgIndex == null);
        Command<?, ?> receiverTemp = recursiveParse(receiverIndex);
        if (!(receiverTemp.getResultRank() instanceof NumberRank)) {
            throw excFact();
        }
        Command<?, AlgebraNumber> receiver = (Command<?, AlgebraNumber>) receiverTemp;
        String secondArgString = postProcessInput.get(roundTypeIndex);
        Identifier<?, ?> secondArgId = Identifier.firstMatchingIdentifier(secondArgString, this::excFact);
        if (thirdArgIndex == null) {
            if (secondArgIndex == null) {
                if (secondArgId == ReservedNames.INT) {
                    return new BinaryPrimCommand<>(commandString, RealFieldOps.ROUND_Z,
                            receiver, null, RoundingMode.class);
                }
                if (secondArgId == ReservedNames.RAT) {
                    return new BinaryPrimCommand<>(commandString, RealFieldOps.ROUND_Q,
                            receiver, null, MathContext.class);
                }
            } else {
                String secondArg = postProcessInput.get(secondArgIndex);
                if (secondArgId == ReservedNames.INT) {
                    RoundingMode roundingMode = getRoundingMode(secondArg, commandString);
                    return new BinaryPrimCommand<>(commandString, RealFieldOps.ROUND_Z,
                            receiver, roundingMode, RoundingMode.class);
                }
                if (secondArgId == ReservedNames.RAT) {
                    Integer precision;
                    RoundingMode roundingMode;
                    
                    if (StringUtils.isDigitString(secondArg)) {
                        precision = Integer.valueOf(secondArg);
                        roundingMode = null;
                    } else {
                        precision = null;
                        roundingMode = getRoundingMode(secondArg, commandString);
                    }
                    return new OperativeCommand.TrinaryDoublePrimCommand<>(
                            commandString,
                            RealFieldOps.ROUND_Q,
                            receiver,
                            precision,
                            Integer.class,
                            roundingMode,
                            RoundingMode.class
                    );
                }
            }
        } else {
            String secondArg = postProcessInput.get(secondArgIndex);
            String thirdArg = postProcessInput.get(thirdArgIndex);
            if (secondArgId == ReservedNames.RAT) {
                return new BinaryPrimCommand<>(
                        commandString,
                        RealFieldOps.ROUND_Q,
                        receiver,
                        getMathContext(secondArg, thirdArg, commandString),
                        MathContext.class
                );
            }
        }
        throw excFact(commandString);
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> roundAuxOperation(
            String commandString,
            IndexWithDepth receiverIndex,
            OpNames opName,
            IndexWithDepth@ArrayLenRange(from = 1, to = 4)... argIndices
    ) {
        Command<?, ?> receiver = recursiveParse(receiverIndex);
        String secondArgString = postProcessInput.get(argIndices[0]);
        if (ReservedNames.ANS.enumReserves(secondArgString)) {
            if (receiver.getResultRank() instanceof NumberRank) {
                boolean roundZ = intRoundTrueRatRoundFalse(secondArgString, commandString);
                RealFieldOps op = switch (opName) {
                    case SQRT -> roundZ ? RealFieldOps.SQRT_ROUND_Z : RealFieldOps.SQRT_ROUND_Q;
                    case EXP -> roundZ ? RealFieldOps.EXP_ROUND_Z : RealFieldOps.EXP_ROUND_Q;
                    case LN -> roundZ ? RealFieldOps.LN_ROUND_Z : RealFieldOps.LN_ROUND_Q;
                    default -> throw excFact(commandString);
                };
                Object roundArg = switch (argIndices.length) {
                    case 1 -> null;
                    case 2 -> {
                        String roundString = postProcessInput.get(argIndices[1]);
                        if (roundZ) {
                            yield getRoundingMode(roundString, commandString);
                        }
                        yield StringUtils.isDigitString(roundString)
                                ? getMathContext(roundString, null, commandString)
                                : getMathContext(null, roundString, commandString);
                    }
                    case 3 -> {
                        if (roundZ) {
                            throw excFact(commandString);
                        }
                        yield getMathContext(
                                postProcessInput.get(argIndices[1]),
                                postProcessInput.get(argIndices[2]),
                                commandString
                        );
                    }
                    default -> throw excFact(commandString);
                };
                return new BinaryPrimCommand<>(
                        commandString,
                        op,
                        (Command<?, AlgebraNumber>) receiver,
                        roundArg,
                        roundZ ? RoundingMode.class : MathContext.class
                );
            }
            throw excFact();
        }
        String roundTypeString = postProcessInput.get(argIndices[1]);
        if (receiver.getResultRank() instanceof NumberRank) {
            boolean roundZ = intRoundTrueRatRoundFalse(roundTypeString, commandString);
            Object roundArg = switch (argIndices.length) {
                case 2 -> null;
                case 3 -> {
                    String roundString = postProcessInput.get(argIndices[2]);
                    if (roundZ) {
                        yield getRoundingMode(roundString, commandString);
                    }
                    yield StringUtils.isDigitString(roundString)
                            ? getMathContext(roundString, null, commandString)
                            : getMathContext(null, roundString, commandString);
                }
                case 4 -> {
                    if (roundZ) {
                        throw excFact(commandString);
                    }
                    yield getMathContext(
                            postProcessInput.get(argIndices[2]),
                            postProcessInput.get(argIndices[3]),
                            commandString
                    );
                }
                default -> throw excFact(commandString);
            };
            
            if ((opName == OpNames.ROOT) && (StringUtils.isDigitString(secondArgString))) {
                Integer rootIndex = parseInt(secondArgString, commandString);
                return new OperativeCommand.TrinaryDoublePrimCommand<>(
                        commandString,
                        roundZ ? RealFieldOps.ROOT_ROUND_Z : RealFieldOps.ROOT_ROUND_Q,
                        (Command<?, AlgebraNumber>) receiver,
                        rootIndex,
                        int.class,
                        roundArg,
                        roundZ ? RoundingMode.class : MathContext.class
                );
            }
            RealFieldOps op;
            switch (opName) {
                case QUOTIENT -> {
                    if (roundZ) {
                        op = RealFieldOps.QUOTIENT_ROUND_Z;
                    } else {
                        Command<?, AlgebraNumber> newCommand = new BinaryStandardCommand<>(
                                commandString,
                                RealFieldOps.QUOTIENT,
                                (Command<?, AlgebraNumber>) receiver,
                                (Command<?, AlgebraNumber>) recursiveParse(argIndices[0])
                        );
                        return new BinaryPrimCommand<>(
                                commandString,
                                RealFieldOps.ROUND_Q,
                                newCommand,
                                roundArg,
                                MathContext.class
                        );
                    }
                }
                case ROOT -> {
                    if (StringUtils.isDigitString(secondArgString)) {
                        return new OperativeCommand.TrinaryDoublePrimCommand<>(
                                commandString,
                                roundZ ? RealFieldOps.ROOT_ROUND_Z : RealFieldOps.ROOT_ROUND_Q,
                                (Command<?, AlgebraNumber>) receiver,
                                parseInt(secondArgString, commandString),
                                int.class,
                                roundArg,
                                roundZ ? RoundingMode.class : MathContext.class
                        );
                    }
                    op = roundZ ? RealFieldOps.ROOT_ROUND_Z : RealFieldOps.ROOT_ROUND_Q;
                }
                case POWER -> op = roundZ ? RealFieldOps.POWER_ROUND_Z : RealFieldOps.POWER_ROUND_Q;
                case LOG_BASE -> op = roundZ ? RealFieldOps.LOG_BASE_ROUND_Z : RealFieldOps.LOG_BASE_ROUND_Q;
                default -> throw excFact(commandString);
            };
            
            return new OperativeCommand.TrinaryCommand<>(
                    commandString,
                    op,
                    (Command<?, AlgebraNumber>) receiver,
                    (Command<?, AlgebraNumber>) recursiveParse(argIndices[0]),
                    roundArg,
                    roundZ ? RoundingMode.class : MathContext.class
            );
        }
        throw excFact(commandString);
    }
    
    @SideEffectFree
    private OperativeCommand<?, ?, ?, ?> truncationOp(
            String commandString,
            IndexWithDepth receiverIndex,
            OpNames opName,
            @Nullable IndexWithDepth secondArgIndex,
            IndexWithDepth truncationTypeIndex,
            @Nullable IndexWithDepth auxArgIndex
    ) {
        Command<?, ?> receiver = recursiveParse(receiverIndex);
        String truncTypeString = postProcessInput.get(truncationTypeIndex);
        if (opName == OpNames.QUOTIENT_WITH_REMAINDER) {
            if (ReservedNames.INT.reserves(truncTypeString)) {
                assert secondArgIndex != null;
                return switch (receiver.getResultRank()) {
                    case NumberRank ignored -> new BinaryStandardCommand<>(
                            commandString,
                            RealFieldOps.QUOTIENT_Z_WITH_REMAINDER,
                            (Command<?, AlgebraNumber>) receiver,
                            (Command<?, AlgebraNumber>) recursiveParse(secondArgIndex)
                    );
                    case FunctionRank ignored -> new BinaryStandardCommand<>(
                            commandString,
                            PolynomialOps.QUOTIENT_Z_WITH_REMAINDER,
                            (Command<?, Polynomial<?>>) receiver,
                            (Command<?, Polynomial<?>>) recursiveParse(secondArgIndex)
                    );
                    default -> throw excFact(commandString);
                };
            }
            throw excFact(commandString);
        }
        
        if (secondArgIndex == null) {
            if (receiver.getResultRank() instanceof NumberRank) {
                if (intRoundTrueRatRoundFalse(truncTypeString, commandString)) {
                    RealFieldOps op = switch (opName) {
                        case SQRT_WITH_REMAINDER -> RealFieldOps.SQRT_Z_WITH_REMAINDER;
                        case SQRT_REMAINDER -> RealFieldOps.SQRT_Z_REMAINDER;
                        case EXP_WITH_REMAINDER -> RealFieldOps.EXP_Z_WITH_REMAINDER;
                        case EXP_REMAINDER -> RealFieldOps.EXP_Z_REMAINDER;
                        case LN_WITH_REMAINDER -> RealFieldOps.LN_Z_WITH_REMAINDER;
                        case LN_REMAINDER -> RealFieldOps.LN_Z_REMAINDER;
                        default -> throw excFact();
                    };
                    return new OperativeCommand.UnaryCommand<>(
                            commandString,
                            op,
                            (Command<?, AlgebraNumber>) receiver
                    );
                }
                
                Integer precision;
                if (auxArgIndex != null) {
                    String precString = postProcessInput.get(auxArgIndex);
                    precision = parseInt(precString, commandString);
                } else {
                    precision = null;
                }
                RealFieldOps op = switch (opName) {
                    case SQRT_WITH_REMAINDER -> RealFieldOps.SQRT_Q_WITH_REMAINDER;
                    case SQRT_REMAINDER -> RealFieldOps.SQRT_Q_REMAINDER;
                    case EXP_WITH_REMAINDER -> RealFieldOps.EXP_Q_WITH_REMAINDER;
                    case EXP_REMAINDER -> RealFieldOps.EXP_Q_REMAINDER;
                    case LN_WITH_REMAINDER -> RealFieldOps.LN_Q_WITH_REMAINDER;
                    case LN_REMAINDER -> RealFieldOps.LN_Q_REMAINDER;
                    default -> throw excFact();
                };
                return new BinaryPrimCommand<>(
                        commandString,
                        op,
                        (Command<?, AlgebraNumber>) receiver,
                        precision,
                        Integer.class
                );
            }
            throw excFact();
        }
        
        String secondArgString = postProcessInput.get(secondArgIndex);
        if (receiver.getResultRank() instanceof NumberRank) {
            if (intRoundTrueRatRoundFalse(truncTypeString, commandString)) {
                RealFieldOps op = switch (opName) {
                    case ROOT_WITH_REMAINDER -> RealFieldOps.ROOT_Z_WITH_REMAINDER;
                    case ROOT_REMAINDER -> RealFieldOps.ROOT_Z_REMAINDER;
                    case POWER_WITH_REMAINDER -> RealFieldOps.POWER_Z_WITH_REMAINDER;
                    case POWER_REMAINDER -> RealFieldOps.POWER_Z_REMAINDER;
                    case LOG_BASE_WITH_REMAINDER -> RealFieldOps.LOG_BASE_Z_WITH_REMAINDER;
                    case LOG_BASE_REMAINDER -> RealFieldOps.LOG_BASE_Z_REMAINDER;
                    default -> throw excFact();
                };
                if (op.flags().contains(OpFlag.SECOND_ARG_PRIM) && StringUtils.isDigitString(secondArgString)) {
                    assert (opName == OpNames.ROOT_REMAINDER) || (opName == OpNames.ROOT_WITH_REMAINDER);
                    return new BinaryPrimCommand<>(
                            commandString,
                            op,
                            (Command<?, AlgebraNumber>) receiver,
                            parseInt(secondArgString, commandString),
                            int.class
                    );
                }
                return new BinaryStandardCommand<>(
                        commandString,
                        op,
                        (Command<?, AlgebraNumber>) receiver,
                        (Command<?, AlgebraNumber>) recursiveParse(secondArgIndex)
                );
            }
            
            Integer precision;
            if (auxArgIndex != null) {
                String precString = postProcessInput.get(auxArgIndex);
                precision = parseInt(precString, commandString);
            } else {
                precision = null;
            }
            RealFieldOps op = switch (opName) {
                case ROOT_WITH_REMAINDER -> RealFieldOps.ROOT_Q_WITH_REMAINDER;
                case ROOT_REMAINDER -> RealFieldOps.ROOT_Q_REMAINDER;
                case POWER_WITH_REMAINDER -> RealFieldOps.POWER_Q_WITH_REMAINDER;
                case POWER_REMAINDER -> RealFieldOps.POWER_Q_REMAINDER;
                case LOG_BASE_WITH_REMAINDER -> RealFieldOps.LOG_BASE_Q_WITH_REMAINDER;
                case LOG_BASE_REMAINDER -> RealFieldOps.LOG_BASE_Q_REMAINDER;
                default -> throw excFact();
            };
            if (op.flags().contains(OpFlag.SECOND_ARG_PRIM) && StringUtils.isDigitString(secondArgString)) {
                assert (opName == OpNames.ROOT_REMAINDER) || (opName == OpNames.ROOT_WITH_REMAINDER);
                return new OperativeCommand.TrinaryDoublePrimCommand<>(
                        commandString,
                        op,
                        (Command<?, AlgebraNumber>) receiver,
                        parseInt(secondArgString, commandString),
                        int.class,
                        precision,
                        Integer.class
                );
            }
            return new OperativeCommand.TrinaryCommand<>(
                    commandString,
                    op,
                    (Command<?, AlgebraNumber>) receiver,
                    (Command<?, AlgebraNumber>) recursiveParse(secondArgIndex),
                    precision,
                    Integer.class
            );
        }
        throw excFact(commandString);
    }
    
    @Pure
    private boolean intRoundTrueRatRoundFalse(
            CharSequence toTest,
            String source
    ) {
        boolean roundZ = ReservedNames.INT.asMatcher(toTest).matches();
        if (!roundZ && !ReservedNames.RAT.asMatcher(toTest).matches()) {
            throw excFact(source);
        }
        return roundZ;
    }
    
    @SideEffectFree
    private MathContext getMathContext(
            @Nullable String precisionString,
            @Nullable String roundingModeString,
            String source
    ) {
        assert (precisionString != null) || (roundingModeString != null);
        RoundingMode roundingMode = NullnessUtils.returnDefaultIfNull(
                roundingModeString,
                rm -> getRoundingMode(rm, source),
                AlgebraNumber.DEFAULT_ROUNDING
        );
        int precision = (precisionString != null)
                ? parseInt(precisionString, source)
                : AlgebraNumber.DEFAULT_PRECISION;
        
        return new MathContext(precision, roundingMode);
    }
    
    @Pure
    private int parseInt(
            String intString,
            String source
    ) {
        assert StringUtils.isDigitString(intString);
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException oldNFE) {
            CommandFormatException newCFE = excFact(source);
            newCFE.initCause(oldNFE);
            throw newCFE;
        }
    }
    
    @SideEffectFree
    private RoundingMode getRoundingMode(
            String roundingModeString,
            String source
    ) {
        if (StringUtils.isDigitString(roundingModeString)) {
            try {
                return RoundingMode.valueOf( Integer.parseInt(roundingModeString) );
            } catch (IllegalArgumentException oldIAE) {
                CommandFormatException newCFE = excFact(source);
                newCFE.initCause(oldIAE);
                throw newCFE;
            }
        }
        return switch (roundingModeString) {
            case "ceiling", "Ceiling", "CEILING" -> RoundingMode.CEILING;
            case "down", "Down", "DOWN" -> RoundingMode.DOWN;
            case "floor", "Floor", "FLOOR" -> RoundingMode.FLOOR;
            case "halfdown", "halfDown", "HalfDown", "Halfdown", "half_down", "half_Down", "Half_Down",
                 "Half_down", "HALF_DOWN", "HALFDOWN" -> RoundingMode.HALF_DOWN;
            case "halfeven", "halfEven", "HalfEven", "Halfeven", "half_even", "half_Even", "Half_Even",
                 "Half_even", "HALF_EVEN", "HALFEVEN", "DEFAULT", "Default", "default",
                 "DEF", "def", "Def", "NULL", "Null", "null" -> RoundingMode.HALF_EVEN;
            case "halfup", "halfUp", "HalfUp", "Halfup", "half_up", "half_Up", "Half_Up",
                 "Half_up", "HALF_UP", "HALFUP" -> RoundingMode.HALF_UP;
            case "unnecessary", "NONE", "None", "none", "Unnecessary", "UNNECESSARY" -> RoundingMode.UNNECESSARY;
            case "up", "Up", "UP" -> RoundingMode.UP;
            default -> throw excFact(source);
        };
    }
    
    @SideEffectFree
    private CommandFormatException excFact(
            String focus
    ) {
        throw CommandFormatException.forInputStringWithFocus(preProcessInput, focus);
    }
    
    @SideEffectFree
    private CommandFormatException excFact() {
        throw CommandFormatException.forInputString(preProcessInput);
    }
    
    /*@SideEffectFree
    private static String prune(
            String prunee,
            ReservedSymbols pruneTarget
    ) {
        Matcher pruneMatcher = pruneTarget.asMatcher(prunee);
        String result = pruneMatcher.find()
                ? prunee.substring(pruneMatcher.end())
                : prunee;
        if (ReservedSymbols.GROUPERS.contains(pruneTarget)) {
            pruneMatcher = pruneTarget.secondaryPattern().matcher(result);
            if (!pruneMatcher.find()) {
                return result;
            }
            
            MatchResult lastResult = pruneMatcher.toMatchResult();
            MatchResult beforeLastResult = null;
            
            while (!pruneMatcher.hitEnd()) {
                pruneMatcher.find();
                beforeLastResult = lastResult;
                lastResult = pruneMatcher.toMatchResult();
            }
            assert beforeLastResult != null;
            result = pruneMatcher.hasMatch()
                    ? result.substring(0, beforeLastResult.end())
                    : result;
        }
        return result.trim();
    }*/
}
