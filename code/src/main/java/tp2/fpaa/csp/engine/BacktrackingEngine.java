package tp2.fpaa.csp.engine;

import tp2.fpaa.csp.contract.ConstraintChecker;
import tp2.fpaa.csp.contract.State;
import tp2.fpaa.csp.contract.ValueIterator;
import tp2.fpaa.csp.contract.VariableSelector;
import tp2.fpaa.csp.result.SolveResult;

import java.util.Objects;
import java.util.OptionalInt;

public final class BacktrackingEngine<S extends State<S>> {

    private final ConstraintChecker<S> checker;
    private final VariableSelector<S> selector;
    private final ValueIterator<S> iterator;

    private long nodesVisited;
    private int backtracks;

    public BacktrackingEngine(
            ConstraintChecker<S> checker,
            VariableSelector<S> selector,
            ValueIterator<S> iterator) {
        this.checker = Objects.requireNonNull(checker);
        this.selector = Objects.requireNonNull(selector);
        this.iterator = Objects.requireNonNull(iterator);
    }

    public SolveResult<S> solve(S initialState) {
        nodesVisited = 0;
        backtracks = 0;
        S workingState = initialState.clone();
        S solution = backtrack(workingState);
        return new SolveResult<>(solution, nodesVisited, backtracks);
    }

    private S backtrack(S state) {
        nodesVisited++;

        if (state.isComplete()) {
            return state;
        }

        OptionalInt variableOpt = selector.selectVariable(state);
        if (variableOpt.isEmpty()) {
            return null;
        }

        int variable = variableOpt.getAsInt();

        for (int value : iterator.getValues(state, variable)) {
            state.assign(variable, value);

            if (checker.isValid(state)) {
                S result = backtrack(state);
                if (result != null) {
                    return result;
                }
            }

            backtracks++;
            state.unassign(variable);
        }

        return null;
    }
}
