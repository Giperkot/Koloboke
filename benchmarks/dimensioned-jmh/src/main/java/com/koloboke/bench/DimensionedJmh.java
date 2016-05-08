/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koloboke.bench;

import com.koloboke.collect.map.ObjIntMap;
import com.koloboke.collect.map.ObjObjMapFactory;
import com.koloboke.collect.map.hash.HashObjIntMaps;
import com.koloboke.collect.map.hash.HashObjObjMaps;
import com.koloboke.function.ToLongFunction;
import org.openjdk.jmh.results.*;
import org.openjdk.jmh.runner.format.OutputFormat;
import org.openjdk.jmh.runner.format.OutputFormatFactory;
import org.openjdk.jmh.runner.*;
import org.openjdk.jmh.runner.options.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static com.koloboke.collect.Equivalence.caseInsensitive;
import static com.koloboke.collect.set.hash.HashObjSets.newImmutableSet;


public final class DimensionedJmh {
    private static final BenchmarkList MICRO_BENCHMARK_LIST = BenchmarkList.defaultList();
    private static final OutputFormat NO_OUTPUT =
            OutputFormatFactory.createFormatInstance(System.out, VerboseMode.SILENT);

    private static void fatal(String message) {
        System.err.println(message);
        System.exit(1);
    }

    private static String regexp(Class<?> containerClass) {
        return containerClass.getCanonicalName() + ".*";
    }

    private static Collection<BenchmarkListEntry> getBenchmarks(String regexp) {
        Set<BenchmarkListEntry> benchmarks = MICRO_BENCHMARK_LIST.find(NO_OUTPUT, asList(regexp),
                Collections.<String>emptyList());
        if (benchmarks.isEmpty())
            fatal("No benchmarks found. Wrong container class?");
        return benchmarks;
    }

    private static List<List<String>> makeDimTable(Collection<BenchmarkListEntry> benchmarks) {
        List<List<String>> dimTable = benchmarks.stream()
                .map(b -> dimParts(methodName(b.getUsername())))
                .collect(Collectors.toList());
        dimTable.stream().filter(benchmark -> benchmark.size() != dimTable.get(0).size())
                .forEach(benchmark -> fatal("Not even dimensions"));
        return dimTable;
    }

    private static List<String> dimParts(String methodName) {
        return asList(methodName.split("_"));
    }

    private static String methodName(String qualifiedName) {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }

    private static String lower(String title) {
        return title.substring(0, 1).toLowerCase() + title.substring(1);
    }

    private static final String OPTIONS_DELIMITER = ",";

    private static String joinOptions(Collection<String> options) {
        return String.join(OPTIONS_DELIMITER, options);
    }

    private static List<String> splitOptions(String options) {
        return asList(options.split(Pattern.quote(OPTIONS_DELIMITER)));
    }

    private static final String NOT_DIMENSION = "NOT_DIMENSION";
    private static boolean isDimension(String dim) {
        return !NOT_DIMENSION.equals(dim);
    }

    private final String regexp;
    private List<String> argDimNames = new ArrayList<>();
    private ObjObjMapFactory<String, Object, ?> dimMapsFactory = HashObjObjMaps
            .<String, Object>getDefaultFactory().withKeyEquivalence(caseInsensitive());
    private Map<String, List<String>> argDimOptions = dimMapsFactory.newUpdatableMap();
    private List<String> benchDimNames = new ArrayList<>();
    private Map<String, Collection<String>> benchDimOptions = dimMapsFactory.newUpdatableMap();
    private ObjIntMap<String> maxDimWidths =
            HashObjIntMaps.<String>getDefaultFactory().withKeyEquivalence(caseInsensitive())
            .newUpdatableMap();
    private ToLongFunction<Map<String, String>> getOperationsPerInvocation = null;
    private boolean dynamicOperationsPerIteration = false;
    private boolean headerPrinted = false;

    public DimensionedJmh(Class<?> benchmarksContainerClass) {
        analyzeTable(makeDimTable(getBenchmarks(regexp = regexp(benchmarksContainerClass))));
    }

    private void analyzeTable(List<List<String>> dimTable) {
        for (int col = 0; col < dimTable.get(0).size(); col++) {
            List<String> column = new ArrayList<>();
            for (List<String> benchmark : dimTable) {
                column.add(benchmark.get(col));
            }
            analyzeDimension(col + 1, column);
        }
    }

    private void analyzeDimension(int col, final List<String> column) {
        if (newImmutableSet(column).size() == 1) {
            benchDimNames.add(NOT_DIMENSION);
            return;
        }
        String commonSuffix = column.get(0);
        for (String dim : column) {
            String[] camelCaseParts = dim.split("(?<!^)(?=[A-Z])");
            for (int i = 0; i < camelCaseParts.length; i++) {
                String suffix = "";
                for (int j = i; j < camelCaseParts.length; j++) {
                    suffix += camelCaseParts[j];
                }
                if (commonSuffix.endsWith(suffix)) {
                    commonSuffix = suffix;
                    break;
                }
                if (i == camelCaseParts.length - 1)
                    fatal("Missed dim name? Column " + col);
            }
        }
        String dimName = commonSuffix;
        benchDimNames.add(dimName);
        int dimNameLen = dimName.length();
        maxDimWidths.merge(dimName, dimNameLen, Math::max);
        Set<String> options = newImmutableSet(setAdd -> {
            for (String dim : column) {
                int optionLen = dim.length() - dimNameLen;
                setAdd.accept(dim.substring(0, optionLen));
                maxDimWidths.merge(dimName, optionLen, Math::max);
            }
        });
        benchDimOptions.put(dimName, options);
    }

    public DimensionedJmh withGetOperationsPerInvocation(
            ToLongFunction<Map<String, String>> getOperationCount) {
        this.getOperationsPerInvocation = getOperationCount;
        return this;
    }

    public DimensionedJmh dynamicOperationsPerIteration() {
        dynamicOperationsPerIteration = true;
        return this;
    }

    public DimensionedJmh addArgDim(String argName, Object... options) {
        maxDimWidths.merge(argName, argName.length(), Math::max);
        argDimNames.add(argName);
        argDimOptions.put(argName, Arrays.stream(options).map(Object::toString)
                .peek(option -> maxDimWidths.merge(argName, option.length(), Math::max))
                .collect(Collectors.toList()));
        return this;
    }

    public void run(String[] args) throws RunnerException, CommandLineOptionException {
        Map<String, Collection<String>> filteredBenchOptions =
                dimMapsFactory.newUpdatableMap(benchDimOptions);
        Map<String, List<String>> filteredArgOptions =
                dimMapsFactory.newUpdatableMap(argDimOptions);
        List<String> filteredArgs = new ArrayList<>(asList(args));
        Iterator<String> argsIt = filteredArgs.iterator();
        while (argsIt.hasNext()) {
            String arg = argsIt.next();
            String[] parts = arg.split("=");
            if (parts.length == 2) {
                String dimName = parts[0];
                List<String> options = splitOptions(parts[1]);
                if (filterDim(filteredBenchOptions, dimName, options, false) ||
                        filterDim(filteredArgOptions, dimName, options, true)) {
                    argsIt.remove();
                }
            }
            if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                return;
            }
        }
        headerPrinted = false;

        if (filteredArgOptions.isEmpty()) {
            runArgOptionCombination(Collections.<String, String>emptyMap(), filteredArgs,
                    filteredBenchOptions);
            return;
        }
        int argOptionCombinations = filteredArgOptions.values().stream()
                .mapToInt(Collection::size).reduce(1, (a, b) -> a * b);
        if (argOptionCombinations == 0) {
            System.err.println("You must pass options for dimensions without predefined options!");
            printHelp();
            return;
        }
        for (int comb = 0; comb < argOptionCombinations; comb++) {
            Map<String, String> combination = dimMapsFactory.newUpdatableMap();
            int r = comb;
            for (String argDimName : argDimNames) {
                List<String> options = filteredArgOptions.get(argDimName);
                combination.put(argDimName, options.get(r % options.size()));
                r /= options.size();
            }
            runArgOptionCombination(combination, filteredArgs, filteredBenchOptions);
        }
    }

    private <T extends Collection<String>> boolean filterDim(Map<String, T> filteredOptions,
            String dimName, T options, boolean allowUnknownOptions) {
        Collection<String> allDimOptions = filteredOptions.get(dimName);
        if (allDimOptions == null)
            return false;
        if (!allowUnknownOptions && !allDimOptions.containsAll(options)) {
            fatal("Wrong option(s) for dim " + dimName +
                    "\nAvailable: " + joinOptions(allDimOptions) + "\nGiven: " + joinOptions(
                    options));
        } else {
            options.forEach(option -> maxDimWidths.merge(dimName, option.length(), Math::max));
        }
        filteredOptions.put(dimName, options);
        return true;
    }

    private void runArgOptionCombination(Map<String, String> combination, List<String> args,
            Map<String, Collection<String>> benchOptions)
            throws RunnerException, CommandLineOptionException {
        printHeaderOnce();
        // Patterns passed via "command line" args, because otherwise JMH hide them with '.*'
        List<String> extraArgs = new ArrayList<>(args);
        extraArgs.add(filterRegexp(benchOptions));
        Options jmhOptions = new OptionsBuilder()
                .parent(new CommandLineOptions(extraArgs.stream().toArray(String[]::new)))
                .jvmArgs(jvmArgs(combination))
                .build();
        Collection<RunResult> results = new Runner(jmhOptions).run();
        results.forEach(result -> formatBenchResult(combination, result));
    }

    private void printHeaderOnce() {
        if (headerPrinted)
            return;
        argDimNames.stream().map(dim -> alignDim(lower(dim))).forEach(System.out::print);
        benchDimNames.stream().filter(DimensionedJmh::isDimension).map(dim -> alignDim(lower(dim)))
                .forEach(System.out::print);
        System.out.printf(": %6s %6s\n", "mean", "err");
        headerPrinted = true;
    }

    private String[] jvmArgs(Map<String, String> combination) {
        return combination.entrySet().stream()
                .map(argOption -> "-D" + argOption.getKey() + "=" + argOption.getValue())
                .toArray(String[]::new);
    }

    private String filterRegexp(Map<String, Collection<String>> benchOptions) {
        return benchDimNames.stream().filter(DimensionedJmh::isDimension)
                .map(dim -> "(" + String.join("|", benchOptions.get(dim)) + ")" + dim + ".*")
                .collect(Collectors.joining("", this.regexp, ""));
    }

    private void formatBenchResult(Map<String, String> combination, RunResult result) {
        argDimNames.stream().map(dim -> align(dim, combination.get(dim)))
                .forEach(System.out::print);
        Map<String, String> benchOptions = dimMapsFactory.newUpdatableMap();
        List<String> dims = dimParts(methodName(result.getParams().getBenchmark()));
        Iterator<String> dimNamesIt = benchDimNames.iterator();
        for (String dim : dims) {
            String dimName = dimNamesIt.next();
            if (!isDimension(dimName))
                continue;
            String option = dim.substring(0, dim.length() - dimName.length());
            benchOptions.put(dimName, option);
            System.out.print(align(dimName, option));
        }
        Result res = getResult(result);
        long operations = operations(combination, benchOptions);
        double mean = res.getScore() / (double) operations;
        double err = res.getScoreError() / (double) operations;
        // Locale.US for dot instead of comma as separator
        System.out.printf(Locale.US, ": %6.2f %6.2f\n", mean, err);
    }

    private Result getResult(RunResult result) {
        Result primaryResult = result.getPrimaryResult();
        if (!(primaryResult instanceof AverageTimeResult))
            fatal("Dynamic operations work only in AverageTime benchmark mode");
        if (!dynamicOperationsPerIteration)
            return primaryResult;
        return result.getSecondaryResults().get("operationsPerIteration");
    }

    private long operations(Map<String, String> argOptions, Map<String, String> benchOptions) {
        return getOperationsPerInvocation != null ?
                getOperationsPerInvocation.applyAsLong(
                        dimMapsFactory.newImmutableMap(argOptions, benchOptions)) :
                1L;
    }

    private String alignDim(String dim) {
        return align(dim, dim);
    }

    private String align(String dim, String s) {
        return String.format("%-" + (maxDimWidths.getInt(dim) + 1) + "s", s);
    }

    private void printHelp() {
        argDimNames.stream()
                .map(dim -> {
                    String options = joinOptions(argDimOptions.get(dim));
                    if (options.isEmpty())
                        options = "<No predefined options, you must pass some from command line>";
                    return lower(dim) + "=" + options;
                })
                .forEach(System.err::println);
        benchDimNames.stream().filter(DimensionedJmh::isDimension)
                .map(dim -> lower(dim) + "=" + joinOptions(benchDimOptions.get(dim)))
                .forEach(System.err::println);
        System.err.println("+ Any JMH options, except including patterns:");
        try {
            new CommandLineOptions().showHelp();
        } catch (IOException | CommandLineOptionException e) {
            throw new RuntimeException(e);
        }
    }
}
