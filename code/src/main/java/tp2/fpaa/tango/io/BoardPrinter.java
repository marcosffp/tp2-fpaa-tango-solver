package tp2.fpaa.tango.io;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.domain.Constraint;
import tp2.fpaa.tango.domain.ConstraintType;
import tp2.fpaa.tango.domain.Symbol;

import java.util.HashMap;
import java.util.Map;

public final class BoardPrinter {

    private static final String RESET  = "\033[0m";
    private static final String BOLD   = "\033[1m";
    private static final String DIM    = "\033[2m";
    private static final String YELLOW = "\033[33m";
    private static final String CYAN   = "\033[36m";

    private static final String SUN_ICON  = YELLOW + BOLD + "☀" + RESET;
    private static final String MOON_ICON = CYAN   + BOLD + "☽" + RESET;
    private static final String EMPTY_DOT = DIM    + "·"  + RESET;

    private BoardPrinter() {}

    public static void printInitial(Board board) {
        System.out.println("─── Tabuleiro Inicial ────────────────────");
        printGrid(board);
        System.out.println("  Legenda: " + SUN_ICON + " Sol  " + MOON_ICON + " Lua  "
                + EMPTY_DOT + " Vazio  = Igual  × Oposto");
        System.out.println();
    }

    public static void printSolution(Board board) {
        System.out.println("─── Solução ──────────────────────────────");
        printGrid(board);
        System.out.println();
    }

    private static void printGrid(Board board) {
        int size = board.getSize();
        Map<String, ConstraintType> constraintMap = buildConstraintMap(board);

        printTopBorder(size);

        for (int row = 0; row < size; row++) {
            printContentRow(board, row, size, constraintMap);
            if (row < size - 1) {
                printSeparatorRow(row, size, constraintMap);
            }
        }

        printBottomBorder(size);
    }

    private static void printTopBorder(int size) {
        StringBuilder sb = new StringBuilder("  ┌");
        for (int col = 0; col < size; col++) {
            sb.append("───");
            sb.append(col < size - 1 ? "┬" : "┐");
        }
        System.out.println(sb);
    }

    private static void printContentRow(Board board, int row, int size,
                                        Map<String, ConstraintType> constraintMap) {
        StringBuilder line = new StringBuilder("  │");
        for (int col = 0; col < size; col++) {
            Symbol sym = board.getCell(row, col).getSymbol();
            line.append(" ").append(cellIcon(sym)).append(" ");
            if (col < size - 1) {
                line.append(horizontalSeparator(row * size + col, row * size + col + 1, constraintMap));
            }
        }
        line.append("│");
        System.out.println(line);
    }

    private static void printSeparatorRow(int row, int size, Map<String, ConstraintType> constraintMap) {
        StringBuilder sb = new StringBuilder("  ├");
        for (int col = 0; col < size; col++) {
            ConstraintType ct = getConstraint(row * size + col, (row + 1) * size + col, constraintMap);
            sb.append(ct == null ? "───" : ct == ConstraintType.EQUAL ? "─=─" : "─×─");
            sb.append(col < size - 1 ? "┼" : "┤");
        }
        System.out.println(sb);
    }

    private static void printBottomBorder(int size) {
        StringBuilder sb = new StringBuilder("  └");
        for (int col = 0; col < size; col++) {
            sb.append("───");
            sb.append(col < size - 1 ? "┴" : "┘");
        }
        System.out.println(sb);
    }

    private static String horizontalSeparator(int idx1, int idx2, Map<String, ConstraintType> constraintMap) {
        ConstraintType ct = getConstraint(idx1, idx2, constraintMap);
        if (ct == null) return "│";
        return ct == ConstraintType.EQUAL ? "=" : "×";
    }

    private static String cellIcon(Symbol sym) {
        if (sym == null) return EMPTY_DOT;
        return sym == Symbol.SUN ? SUN_ICON : MOON_ICON;
    }

    private static Map<String, ConstraintType> buildConstraintMap(Board board) {
        Map<String, ConstraintType> map = new HashMap<>();
        for (Constraint c : board.getConstraints()) {
            int a = Math.min(c.getFirstIndex(), c.getSecondIndex());
            int b = Math.max(c.getFirstIndex(), c.getSecondIndex());
            map.put(a + "-" + b, c.getType());
        }
        return map;
    }

    private static ConstraintType getConstraint(int idx1, int idx2, Map<String, ConstraintType> map) {
        int a = Math.min(idx1, idx2);
        int b = Math.max(idx1, idx2);
        return map.get(a + "-" + b);
    }
}
