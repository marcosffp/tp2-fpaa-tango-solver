package tp2.fpaa;

import tp2.fpaa.csp.engine.BacktrackingEngine;
import tp2.fpaa.csp.engine.BruteForceEngine;
import tp2.fpaa.csp.result.SolveResult;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;
import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.heuristic.TangoValueIterator;
import tp2.fpaa.tango.heuristic.TangoVariableSelector;
import tp2.fpaa.tango.io.BoardParser;
import tp2.fpaa.tango.io.ResultPrinter;
import tp2.fpaa.tango.io.RunResult;
import tp2.fpaa.tango.validation.BalanceRule;
import tp2.fpaa.tango.validation.ConsecutiveRule;
import tp2.fpaa.tango.validation.EqualRule;
import tp2.fpaa.tango.validation.OppositionRule;
import tp2.fpaa.tango.validation.TangoConstraintChecker;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Map<String, String> BOARDS = new LinkedHashMap<>();
    static {
        BOARDS.put("4x4",        "board_4x4.txt");
        BOARDS.put("6x6-easy",   "board_6x6_easy.txt");
        BOARDS.put("6x6-medium", "board_6x6_medium.txt");
        BOARDS.put("6x6-hard",   "board_6x6_hard.txt");
        BOARDS.put("8x8",        "board_8x8.txt");
        BOARDS.put("16x16",      "board_16x16.txt");
    }

    private static final Map<String, String> ALIASES = new LinkedHashMap<>();
    static {
        ALIASES.put("4x4",     "4x4");
        ALIASES.put("easy",    "6x6-easy");
        ALIASES.put("medium",  "6x6-medium");
        ALIASES.put("hard",    "6x6-hard");
        ALIASES.put("8x8",     "8x8");
        ALIASES.put("16x16",   "16x16");
    }

    public static void main(String[] args) throws IOException {
        ResultPrinter.printHeader();
        List<RunResult> results = new ArrayList<>();
        if (args.length == 0) {
            runAll("both", results);
        } else if (args.length == 1) {
            if (isAlgorithm(args[0])) runAll(args[0], results);
            else { ResultPrinter.printUsage(BOARDS.keySet()); return; }
        } else {
            String boardKey = resolveBoard(args[0]);
            String algorithm = args[1];
            if (boardKey != null) runBoard(boardKey, BOARDS.get(boardKey), algorithm, results);
            else                  runBoardFromPath(args[0], args[0], algorithm, results);
        }
        if (results.size() > 1) ResultPrinter.printSummaryTable(results);
    }

    private static void runAll(String algorithm, List<RunResult> results) throws IOException {
        ResultPrinter.printAllBoardsHeader();
        for (Map.Entry<String, String> entry : BOARDS.entrySet())
            runBoard(entry.getKey(), entry.getValue(), algorithm, results);
    }

    private static void runBoard(String displayName, String resourceName,
                                  String algorithm, List<RunResult> results) throws IOException {
        Path path = resolveResourcePath(resourceName);
        if (path == null) { ResultPrinter.printBoardNotFound(resourceName); return; }
        runBoardFromPath(displayName, path.toString(), algorithm, results);
    }

    private static void runBoardFromPath(String displayName, String path,
                                          String algorithm, List<RunResult> results) throws IOException {
        ResultPrinter.printBoardHeader(displayName);
        Board board = BoardParser.parse(Path.of(path));
        long emptyCells = 0;
        for (int i = 0; i < board.cellCount(); i++)
            if (board.getCellAt(i).isEmpty()) emptyCells++;
        TangoConstraintChecker checker = new TangoConstraintChecker(List.of(
                new ConsecutiveRule(), new BalanceRule(),
                new EqualRule(),       new OppositionRule()
        ));
        boolean bfViable = board.getSize() <= 6;
        if (algorithm.equalsIgnoreCase("bt")) {
            results.add(runAndPrint("bt", board, displayName, emptyCells, checker));
        } else if (algorithm.equalsIgnoreCase("bf")) {
            if (bfViable) results.add(runAndPrint("bf", board, displayName, emptyCells, checker));
            else          ResultPrinter.printBFSkipped(emptyCells);
        } else {
            results.add(runAndPrint("bt", board, displayName, emptyCells, checker));
            if (bfViable) results.add(runAndPrint("bf", board, displayName, emptyCells, checker));
            else          ResultPrinter.printBFSkipped(emptyCells);
        }
    }

    private static RunResult runAndPrint(String algorithm, Board board,
                                          String boardName, long emptyCells,
                                          TangoConstraintChecker checker) {
        boolean isBT = algorithm.equalsIgnoreCase("bt");
        ResultPrinter.printAlgorithmHeader(algorithm);
        TangoBoardAdapter adapter = new TangoBoardAdapter(board);
        long startNs              = System.nanoTime();
        SolveResult<TangoBoardAdapter> solveResult = resolveWith(algorithm, adapter, checker);
        long elapsedNs            = System.nanoTime() - startNs;
        long prunings = isBT ? solveResult.getBacktracks() : 0L;
        RunResult runResult = new RunResult(boardName, board.getSize(), emptyCells, algorithm,
                elapsedNs, solveResult.getNodesVisited(), solveResult.getAttempts(),
                prunings, solveResult.hasSolution());
        ResultPrinter.printRunResult(solveResult, runResult);
        return runResult;
    }

    private static SolveResult<TangoBoardAdapter> resolveWith(String algorithm,
                                                               TangoBoardAdapter adapter,
                                                               TangoConstraintChecker checker) {
        TangoVariableSelector selector = new TangoVariableSelector();
        TangoValueIterator    iterator = new TangoValueIterator();
        if (algorithm.equalsIgnoreCase("bt"))
            return new BacktrackingEngine<>(checker, selector, iterator).solve(adapter);
        return new BruteForceEngine<>(checker, selector, iterator).solve(adapter);
    }

    private static boolean isAlgorithm(String arg) {
        return arg.equalsIgnoreCase("bt")
            || arg.equalsIgnoreCase("bf")
            || arg.equalsIgnoreCase("both");
    }

    private static String resolveBoard(String arg) {
        if (BOARDS.containsKey(arg)) return arg;
        String alias = ALIASES.get(arg.toLowerCase());
        return alias != null ? alias : null;
    }

    private static Path resolveResourcePath(String resourceName) throws IOException {
        URL url = Main.class.getClassLoader().getResource(resourceName);
        if (url == null) return null;
        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException("URI inválida: " + resourceName, e);
        }
    }
}
