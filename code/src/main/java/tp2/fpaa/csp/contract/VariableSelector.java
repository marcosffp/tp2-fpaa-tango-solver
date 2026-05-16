package tp2.fpaa.csp.contract;

import java.util.OptionalInt;

public interface VariableSelector<S> {
    OptionalInt selectVariable(S state);
}
