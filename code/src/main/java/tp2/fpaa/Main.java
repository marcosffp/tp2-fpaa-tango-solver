package tp2.fpaa;

import tp2.fpaa.csp.engine.BacktrackingEngine;
import tp2.fpaa.csp.engine.BruteForceEngine;
import tp2.fpaa.csp.result.SolveResult;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;
import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.heuristic.TangoValueIterator;
import tp2.fpaa.tango.heuristic.TangoVariableSelector;
import tp2.fpaa.tango.io.BoardParser;
import tp2.fpaa.tango.io.BoardPrinter;
import tp2.fpaa.tango.validation.BalanceRule;
import tp2.fpaa.tango.validation.ConsecutiveRule;
import tp2.fpaa.tango.validation.EqualRule;
import tp2.fpaa.tango.validation.OppositionRule;
import tp2.fpaa.tango.validation.TangoConstraintChecker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Uso: Main <caminho-tabuleiro> <algoritmo: bt|bf>");
            return;
        }

        Board board = BoardParser.parse(Path.of(args[0]));
        BoardPrinter.printInitial(board);

        TangoConstraintChecker checker = new TangoConstraintChecker(List.of(
                new ConsecutiveRule(),
                new BalanceRule(),
                new EqualRule(),
                new OppositionRule()
        ));

        TangoBoardAdapter adapter = new TangoBoardAdapter(board);
        SolveResult<TangoBoardAdapter> result = resolveWith(args[1], adapter, checker);

        if (result.hasSolution()) {
            BoardPrinter.printSolution(result.getSolution().getBoard());
            System.out.println("Nos visitados: " + result.getNodesVisited());
            System.out.println("Backtracks: " + result.getBacktracks());
        } else {
            System.out.println("Sem solucao encontrada.");
        }
    }

    private static SolveResult<TangoBoardAdapter> resolveWith(
            String algorithm,
            TangoBoardAdapter adapter,
            TangoConstraintChecker checker) {

        TangoVariableSelector selector = new TangoVariableSelector();
        TangoValueIterator iterator = new TangoValueIterator();

        if (algorithm.equalsIgnoreCase("bt")) {
            return new BacktrackingEngine<>(checker, selector, iterator).solve(adapter);
        }
        return new BruteForceEngine<>(checker, selector, iterator).solve(adapter);
    }
}
