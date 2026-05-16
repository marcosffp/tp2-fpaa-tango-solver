package tp2.fpaa.tango.validation;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.domain.Symbol;

public final class BalanceRule implements Rule {

    @Override
    public boolean check(Board board) {
        int size = board.getSize();
        int half = size / 2;
        for (int i = 0; i < size; i++) {
            if (!checkLine(board, i, true, half)) return false;
            if (!checkLine(board, i, false, half)) return false;
        }
        return true;
    }

    private boolean checkLine(Board board, int lineIndex, boolean isRow, int half) {
        int size = board.getSize();
        int sunCount = 0;
        int moonCount = 0;

        for (int j = 0; j < size; j++) {
            int row = isRow ? lineIndex : j;
            int col = isRow ? j : lineIndex;
            Symbol symbol = board.getCell(row, col).getSymbol();

            if (symbol == Symbol.SUN) sunCount++;
            else if (symbol == Symbol.MOON) moonCount++;
        }

        return sunCount <= half && moonCount <= half;
    }
}
