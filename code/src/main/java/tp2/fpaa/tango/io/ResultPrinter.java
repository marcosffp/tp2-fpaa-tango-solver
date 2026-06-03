package tp2.fpaa.tango.io;

import tp2.fpaa.csp.result.SolveResult;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResultPrinter {

    private static final String RESET   = "\033[0m";
    private static final String BOLD    = "\033[1m";
    private static final String GREEN   = "\033[32m";
    private static final String YELLOW  = "\033[33m";
    private static final String CYAN    = "\033[36m";
    private static final String RED     = "\033[31m";
    private static final String MAGENTA = "\033[35m";
    private static final String WHITE   = "\033[37m";

    private ResultPrinter() {}

    public static void printHeader() {
        String title = "  ☀  TANGO PUZZLE SOLVER  ☽  ";
        String border = "═".repeat(title.length());
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + border + "╗" + RESET);
        System.out.println(CYAN + BOLD + "║" + title + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + border + "╝" + RESET);
        System.out.println();
    }

    public static void printBoardHeader(String name) {
        int pad = Math.max(0, 34 - name.length());
        System.out.println(WHITE + BOLD + "┌─ Board: " + name + " " + "─".repeat(pad) + "┐" + RESET);
        System.out.println();
    }

    public static void printAllBoardsHeader() {
        System.out.println(YELLOW + BOLD + "  Executando todos os boards..." + RESET);
        System.out.println();
    }

    public static void printBoardNotFound(String resourceName) {
        System.out.println(RED + "  ✗ Board não encontrado: " + resourceName + RESET);
    }

    public static void printAlgorithmHeader(String algorithm) {
        boolean isBT = algorithm.equalsIgnoreCase("bt");
        String name  = isBT ? "Backtracking" : "Força Bruta";
        String color = isBT ? CYAN : MAGENTA;
        System.out.println(color + BOLD + "══ " + name + " " + "═".repeat(32 - name.length()) + RESET);
        System.out.println();
    }

    public static void printRunResult(SolveResult<TangoBoardAdapter> solveResult, RunResult runResult) {
        System.out.println();
        if (solveResult.hasSolution()) {
            BoardPrinter.printSolution(solveResult.getSolution().getBoard());
            System.out.println(GREEN + BOLD + "  ✓ Solução encontrada!" + RESET);
        } else {
            System.out.println(RED + BOLD + "  ✗ Sem solução encontrada." + RESET);
        }
        System.out.printf("    Tempo                    : %s%n",  formatTime(runResult.getTimeNs()));
        System.out.printf("    Nós visitados            : %,d%n", runResult.getNodesVisited());
        System.out.printf("    Verificações de restrição: %,d%n", runResult.getConstraintChecks());
        System.out.printf("    Podas (restrição violada): %,d%n", runResult.getBacktracks());
        System.out.println();
    }

    public static void printBFSkipped(long emptyCells) {
        System.out.println(MAGENTA + BOLD + "══ Força Bruta ═════════════════════" + RESET);
        System.out.println();
        System.out.println(YELLOW + "  ⚠ Ignorado — 2^" + emptyCells
                + " combinações inviáveis para força bruta." + RESET);
        System.out.println();
    }

    public static void printUsage(Collection<String> boardKeys) {
        System.out.println("  Uso:");
        System.out.println("    Main                     → todos os boards com bt + bf");
        System.out.println("    Main <algoritmo>         → todos com bt|bf|both");
        System.out.println("    Main <board> <algoritmo> → board específico");
        System.out.println();
        System.out.println(YELLOW + "  Boards: "  + String.join(", ", boardKeys) + RESET);
        System.out.println(YELLOW + "  Aliases: 4x4, easy, medium, hard, 8x8, 16x16" + RESET);
        System.out.println();
    }

    public static void printSummaryTable(List<RunResult> results) {
        String bar = "═".repeat(80);
        System.out.println();
        System.out.println(CYAN + BOLD + "╔" + bar + "╗" + RESET);
        System.out.println(CYAN + BOLD + "║" + center("TABELA DE RESULTADOS", 80) + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + bar + "╝" + RESET);
        System.out.println();
        printTable1(results);
        printTable2(results);
        printTable3(results);
    }

    private static void printTable1(List<RunResult> results) {
        System.out.println(WHITE + BOLD + "  Tabela 1 — Desempenho por Execução" + RESET);
        System.out.println();
        String fmt = "  %-15s %-14s %-13s %-18s %-16s %-15s%n";
        String sep = "  " + "─".repeat(95);
        System.out.printf(fmt, "Board", "Algoritmo", "Tempo", "Nós Visitados", "% Espaço Expl.", "Nós/seg");
        System.out.println(sep);
        String lastBoard = "";
        for (RunResult r : results) {
            if (!r.getBoardName().equals(lastBoard) && !lastBoard.isEmpty()) System.out.println(sep);
            lastBoard = r.getBoardName();
            System.out.printf(fmt, r.getBoardName(),
                    r.getAlgorithm().equals("bt") ? "Backtracking" : "Força Bruta",
                    formatTime(r.getTimeNs()),
                    String.format("%,d", r.getNodesVisited()),
                    formatPercent(r.percentExplored()),
                    formatLargeNum(r.nodesPerSecond()));
        }
        System.out.println(sep);
        System.out.println();
    }

    private static void printTable2(List<RunResult> results) {
        Map<String, RunResult> btMap = new LinkedHashMap<>();
        Map<String, RunResult> bfMap = new LinkedHashMap<>();
        for (RunResult r : results) {
            if (r.getAlgorithm().equals("bt")) btMap.put(r.getBoardName(), r);
            else                               bfMap.put(r.getBoardName(), r);
        }
        List<String> common = commonBoards(btMap, bfMap);
        if (common.isEmpty()) return;
        System.out.println(WHITE + BOLD + "  Tabela 2 — Backtracking vs Força Bruta" + RESET);
        System.out.println();
        String fmt = "  %-15s %-14s %-18s %-18s %-16s %-14s%n";
        String sep = "  " + "─".repeat(99);
        System.out.printf(fmt, "Board", "Speedup BT", "Nós BT", "Nós BF", "Redução Nós", "Ef. Poda BT");
        System.out.println(sep);
        for (String board : common) {
            RunResult bt = btMap.get(board), bf = bfMap.get(board);
            double speedup = bf.getTimeNs() > 0 ? (double) bf.getTimeNs() / bt.getTimeNs() : 0;
            double nodeRed = bf.getNodesVisited() > 0
                    ? (1.0 - (double) bt.getNodesVisited() / bf.getNodesVisited()) * 100.0 : 0;
            System.out.printf(fmt, board, String.format("%.0f×", speedup),
                    String.format("%,d", bt.getNodesVisited()), String.format("%,d", bf.getNodesVisited()),
                    String.format("%.4f%%", nodeRed), String.format("%.2f%%", bt.pruningRate()));
        }
        System.out.println(sep);
        System.out.println();
    }

    private static void printTable3(List<RunResult> results) {
        Map<String, RunResult> btMap = new LinkedHashMap<>();
        Map<String, RunResult> bfMap = new LinkedHashMap<>();
        for (RunResult r : results) {
            if (r.getAlgorithm().equals("bt")) btMap.put(r.getBoardName(), r);
            else                               bfMap.put(r.getBoardName(), r);
        }
        List<String> common = commonBoards(btMap, bfMap);
        if (common.isEmpty()) return;
        System.out.println(WHITE + BOLD + "  Tabela 3 — Verificações de Restrição" + RESET);
        System.out.println();
        String fmt = "  %-15s %-18s %-18s %-16s %-14s%n";
        String sep = "  " + "─".repeat(85);
        System.out.printf(fmt, "Board", "Verif. BT", "Verif. BF", "Razão BF/BT", "Podas BT");
        System.out.println(sep);
        for (String board : common) {
            RunResult bt = btMap.get(board), bf = bfMap.get(board);
            double ratio = bt.getConstraintChecks() > 0
                    ? (double) bf.getConstraintChecks() / bt.getConstraintChecks() : 0;
            System.out.printf(fmt, board,
                    String.format("%,d", bt.getConstraintChecks()),
                    String.format("%,d", bf.getConstraintChecks()),
                    String.format("%.0f×", ratio), String.format("%,d", bt.getBacktracks()));
        }
        System.out.println(sep);
        System.out.println();
    }

    private static List<String> commonBoards(Map<String, RunResult> btMap, Map<String, RunResult> bfMap) {
        List<String> common = new ArrayList<>();
        for (String board : btMap.keySet()) if (bfMap.containsKey(board)) common.add(board);
        return common;
    }

    private static String formatTime(long nanos) {
        if (nanos < 1_000_000L)     return String.format("%,d µs", nanos / 1_000);
        if (nanos < 1_000_000_000L) return String.format("%.2f ms", nanos / 1_000_000.0);
        return                             String.format("%.2f s",  nanos / 1_000_000_000.0);
    }

    private static String formatPercent(double pct) {
        if (pct < 0.000001) return String.format("%.2e%%", pct);
        if (pct < 0.01)     return String.format("%.6f%%", pct);
        return                     String.format("%.4f%%", pct);
    }

    private static String formatLargeNum(double n) {
        if (n >= 1_000_000_000) return String.format("%.1fB", n / 1_000_000_000);
        if (n >= 1_000_000)     return String.format("%.1fM", n / 1_000_000);
        if (n >= 1_000)         return String.format("%.1fK", n / 1_000);
        return                         String.format("%.0f", n);
    }

    private static String center(String s, int width) {
        int pad = Math.max(0, width - s.length());
        int left = pad / 2, right = pad - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }
}
