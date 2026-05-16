package tp2.fpaa.tango.adapter;

import tp2.fpaa.csp.contract.State;
import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.board.BoardFactory;
import tp2.fpaa.tango.domain.Symbol;

public final class TangoBoardAdapter implements State<TangoBoardAdapter> {

    private final Board board;

    public TangoBoardAdapter(Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public TangoBoardAdapter clone() {
        return new TangoBoardAdapter(BoardFactory.deepCopy(board));
    }

    @Override
    public void assign(int variable, int value) {
        board.getCellAt(variable).setSymbol(Symbol.values()[value]);
    }

    @Override
    public void unassign(int variable) {
        board.getCellAt(variable).setSymbol(null);
    }

    @Override
    public boolean isComplete() {
        for (int i = 0; i < board.cellCount(); i++) {
            if (board.getCellAt(i).isEmpty()) return false;
        }
        return true;
    }
}
