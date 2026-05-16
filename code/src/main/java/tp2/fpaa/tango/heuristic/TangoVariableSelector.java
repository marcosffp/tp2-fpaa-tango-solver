package tp2.fpaa.tango.heuristic;

import tp2.fpaa.csp.contract.VariableSelector;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;
import tp2.fpaa.tango.board.Board;

import java.util.OptionalInt;

public final class TangoVariableSelector implements VariableSelector<TangoBoardAdapter> {

    @Override
    public OptionalInt selectVariable(TangoBoardAdapter adapter) {
        Board board = adapter.getBoard();
        for (int i = 0; i < board.cellCount(); i++) {
            if (board.getCellAt(i).isEmpty()) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }
}
