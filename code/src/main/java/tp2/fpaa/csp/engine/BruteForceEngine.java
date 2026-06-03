package tp2.fpaa.csp.engine;

import tp2.fpaa.csp.contract.ConstraintChecker;
import tp2.fpaa.csp.contract.State;
import tp2.fpaa.csp.contract.ValueIterator;
import tp2.fpaa.csp.contract.VariableSelector;
import tp2.fpaa.csp.result.SolveResult;

import java.util.Objects;
import java.util.OptionalInt;

public final class BruteForceEngine<S extends State<S>> {

    private final ConstraintChecker<S> checker;
    private final VariableSelector<S> selector;
    private final ValueIterator<S> iterator;

    private long nodesVisited;
    private long backtracks;
    private long attempts;

    public BruteForceEngine(
            ConstraintChecker<S> checker,
            VariableSelector<S> selector,
            ValueIterator<S> iterator) {
        this.checker = Objects.requireNonNull(checker);
        this.selector = Objects.requireNonNull(selector);
        this.iterator = Objects.requireNonNull(iterator);
    }

    public SolveResult<S> solve(S initialState) {
        nodesVisited = 0;
        backtracks   = 0;
        attempts     = 0;
        S workingState = initialState.clone();
        S solution = generate(workingState);
        return new SolveResult<>(solution, nodesVisited, backtracks, attempts);
    }

    private S generate(S state) {
        nodesVisited++;

        if (state.isComplete()) {
            attempts++;
            return checker.isValid(state) ? state : null;
        }

        OptionalInt variableOpt = selector.selectVariable(state);
        if (variableOpt.isEmpty()) return null;

        int variable = variableOpt.getAsInt();

        for (int value : iterator.getValues(state, variable)) {
            state.assign(variable, value);
            S candidate = generate(state);
            if (candidate != null) return candidate;
            backtracks++;
            state.unassign(variable);
        }

        return null;
    }
}
