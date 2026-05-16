package tp2.fpaa.tango.validation;

import tp2.fpaa.csp.contract.ConstraintChecker;
import tp2.fpaa.tango.adapter.TangoBoardAdapter;
import tp2.fpaa.tango.board.Board;

import java.util.List;

public final class TangoConstraintChecker implements ConstraintChecker<TangoBoardAdapter> {

    private final List<Rule> rules;

    public TangoConstraintChecker(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public boolean isValid(TangoBoardAdapter adapter) {
        Board board = adapter.getBoard();
        return rules.stream().allMatch(r -> r.check(board));
    }
}
