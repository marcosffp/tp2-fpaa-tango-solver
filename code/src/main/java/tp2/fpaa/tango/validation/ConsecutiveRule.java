package tp2.fpaa.tango.validation;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.domain.Symbol;

public final class ConsecutiveRule implements Rule {

    private static final int MAX_CONSECUTIVE = 2;

    @Override
    public boolean check(Board board) {
        int size = board.getSize();
        for (int i = 0; i < size; i++) {
            if (!checkLine(board, i, true)) return false;
            if (!checkLine(board, i, false)) return false;
        }
        return true;
    }

    private boolean checkLine(Board board, int lineIndex, boolean isRow) {
        int size = board.getSize();
        Symbol previous = null;
        int consecutive = 1;

        for (int j = 0; j < size; j++) {
            int row = isRow ? lineIndex : j;
            int col = isRow ? j : lineIndex;
            Symbol current = board.getCell(row, col).getSymbol();

            if (current == null) {
                previous = null;
                consecutive = 1;
                continue;
            }

            if (current == previous) {
                consecutive++;
                if (consecutive > MAX_CONSECUTIVE) return false;
            } else {
                previous = current;
                consecutive = 1;
            }
        }
        return true;
    }
}
