package tp2.fpaa.tango.validation;

import tp2.fpaa.tango.board.Board;
import tp2.fpaa.tango.domain.Constraint;
import tp2.fpaa.tango.domain.ConstraintType;
import tp2.fpaa.tango.domain.Symbol;

public final class EqualRule implements Rule {

    @Override
    public boolean check(Board board) {
        for (Constraint constraint : board.getConstraints()) {
            if (constraint.getType() != ConstraintType.EQUAL) continue;

            Symbol first = board.getCellAt(constraint.getFirstIndex()).getSymbol();
            Symbol second = board.getCellAt(constraint.getSecondIndex()).getSymbol();

            if (first != null && second != null && first != second) {
                return false;
            }
        }
        return true;
    }
}
