package tp2.fpaa.tango.io;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.domain.Constraint;
import tp2.fpaa.tango.domain.ConstraintType;
import tp2.fpaa.tango.domain.Symbol;

public final class BoardPrinter {

    private BoardPrinter() {}

    public static void printInitial(Board board) {
        System.out.println("=== Tabuleiro Inicial ===");
        printGrid(board);
        printConstraints(board);
        System.out.println();
    }

    public static void printSolution(Board board) {
        System.out.println("=== Tabuleiro Resolvido ===");
        printGrid(board);
        System.out.println();
    }

    private static void printGrid(Board board) {
        int size = board.getSize();
        for (int row = 0; row < size; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < size; col++) {
                if (col > 0) line.append(" ");
                Symbol symbol = board.getCell(row, col).getSymbol();
                line.append(symbol == null ? "." : symbol == Symbol.SUN ? "S" : "M");
            }
            System.out.println(line);
        }
    }

    private static void printConstraints(Board board) {
        if (board.getConstraints().isEmpty()) return;
        System.out.println("Restricoes:");
        for (Constraint c : board.getConstraints()) {
            String type = c.getType() == ConstraintType.EQUAL ? "=" : "x";
            System.out.println("  " + type + " [" + c.getFirstIndex() + "] -- [" + c.getSecondIndex() + "]");
        }
    }
}
